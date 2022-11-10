package node;

import node.blockchain.*;
import node.communication.*;
import node.communication.utils.Hashing;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;

import static node.communication.utils.DSA.*;
import static node.communication.utils.Hashing.getBlockHash;
import static node.communication.utils.Hashing.getSHAString;
import static node.communication.utils.Utils.deepCloneHashmap;

/**
 * Node represents a peer, a cooperating member within the network
 * Beware, any methods below are a WIP
 */
public class Node  {

    private final int MAX_PEERS, NUM_NODES, QUORUM_SIZE, STARTING_PORT, MIN_CONNECTIONS, MIN_TRANSACTIONS_PER_BLOCK;
    private final Object lock, quorumLock, memPoolLock, quorumReadyVotesLock, memPoolRoundsLock, sigRoundsLock, blockLock;
    private int quorumReadyVotes, memPoolRounds, sigRounds;
    private ArrayList<Address> localPeers;
    private HashMap<String, Transaction> mempool;
    private ArrayList<BlockSignature> quorumSigs;
    private ArrayList<Block> blockchain;
    private final Address myAddress;
    private ServerSocket ss;
    private PrivateKey privateKey;


    /* A collection of getters */
    public int getMaxPeers(){return this.MAX_PEERS;}
    public int getMinConnections(){return this.MIN_CONNECTIONS;}
    public Address getAddress(){return this.myAddress;}
    public ArrayList<Address> getLocalPeers(){return this.localPeers;}
    public HashMap<String, Transaction> getMempool(){return this.mempool;}


    /**
     * Node constructor creates node and begins server socket to accept connections
     *
     * @param port               Port
     * @param maxPeers           Maximum amount of peer connections to maintain
     * @param initialConnections How many nodes we want to attempt to connect to on start
     */
    public Node(int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int startingPort, int minTransactionsPerBlock) {

        /* Initialize global variables */
        lock =  new Object();
        quorumLock = new Object();
        quorumReadyVotesLock = new Object();
        memPoolRoundsLock = new Object();
        sigRoundsLock = new Object();
        myAddress = new Address(port, "localhost");
        localPeers = new ArrayList<>();
        quorumSigs = new ArrayList<>();
        MIN_CONNECTIONS = initialConnections;
        MAX_PEERS = maxPeers;
        NUM_NODES = numNodes;
        QUORUM_SIZE = quorumSize;
        STARTING_PORT = startingPort;
        MIN_TRANSACTIONS_PER_BLOCK = minTransactionsPerBlock;
        mempool = new HashMap<>();
        memPoolLock = new Object();
        blockLock = new Object();
        memPoolRounds = 0;
        quorumReadyVotes = 0;
        sigRounds = 0;


        KeyPair keys = generateDSAKeyPair();
        privateKey = keys.getPrivate();
        writePubKeyToRegistry(myAddress, keys.getPublic());

        try {
            ss = new ServerSocket(port);
            Acceptor acceptor = new Acceptor(this);
            acceptor.start();
            System.out.println("Node up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }



    /**
     * Initializes blockchain
     */
    public void initializeBlockchain(){
        blockchain = new ArrayList<Block>();
        addBlock(new Block(new HashMap<String, Transaction>(), "", 0));
    }

    /**
     * Determines if a connection is eligible
     * @param address Address to verify
     * @param connectIfEligible Connect to address if it is eligible
     * @return True if eligible, otherwise false
     */
    public boolean eligibleConnection(Address address, boolean connectIfEligible){
        synchronized(lock) {
            if (localPeers.size() < MAX_PEERS - 1 && (!address.equals(this.getAddress()) && !this.containsAddress(localPeers, address))) {
                if(connectIfEligible){
                    establishConnection(address);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Add a connection to our dynamic list of peers to speak with
     * @param address
     */
    public void establishConnection(Address address){
        localPeers.add(address);
        //System.out.println("Node " + this.getAddress().getPort() + ": Added peer: " + address.getPort());
    }

    /**
     * Iterate through a list of peers and attempt to establish a mutual connection
     * with a specified amount of nodes
     * @param globalPeers
     */
    public void requestConnections(ArrayList<Address> globalPeers){
        try {
            initializeBlockchain();

            if(globalPeers.size() > 0){
                /* Begin seeking connections */
                ClientConnection connect = new ClientConnection(this, globalPeers);
                connect.start();

                /* Begin heartbeat monitor */
                Thread.sleep(10000);
                HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor(this);
                heartBeatMonitor.start();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the provided address is in the list, otherwise false
     * @param list
     * @param address
     * @return
     */
    public boolean containsAddress(ArrayList<Address> list, Address address){
        for (Address existingAddress : list) {
            if (existingAddress.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsTransaction(Transaction transaction){
        Set<Transaction> values = new HashSet<>();
        for(Map.Entry<String, Transaction> entry : mempool.entrySet()){
            if (entry.getValue().equals(transaction)) {
                return true;
            }
        }
        return false;
    }

    public Address removeAddress(Address address){
        for (Address existingAddress : localPeers) {
            if (existingAddress.equals(address)) {
                localPeers.remove(address);
                return address;
            }
        }
        return null;
    }

    public void gossipTransaction(Transaction transaction){
        synchronized (lock){
            for(Address address : localPeers){
                try {
                    Socket s = new Socket(address.getHost(), address.getPort());
                    InputStream in = s.getInputStream();
                    ObjectInputStream oin = new ObjectInputStream(in);
                    OutputStream out = s.getOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    Message message = new Message(Message.Request.ADD_TRANSACTION, transaction);
                    oout.writeObject(message);
                    oout.flush();
                    s.close();
                } catch (IOException e) {
                    System.out.println("Received IO Exception from node " + address.getPort());
                    //removeAddress(address);
                } catch (ConcurrentModificationException e){
                    break;
                }
            }
        }
    }


    public void sendOneWayMessage(Address address, Message message) {
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(message);
            oout.flush();
            s.close();
        } catch (IOException e) {
            System.out.println("Received IO Exception from node " + address.getPort());
            //removeAddress(address);
        }
    }

    public Message sendTwoWayMessage(Address address, Message message) {
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            s.close();
            return messageReceived;
        } catch (IOException e) {
            System.out.println("Received IO Exception from node " + address.getPort());
            //removeAddress(address);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void addTransaction(Transaction transaction){
        synchronized (memPoolLock){
            if(!containsTransaction(transaction)){
                try {
                    mempool.put(getSHAString(transaction.getData()), transaction);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                gossipTransaction(transaction);
                System.out.println("Node " + myAddress.getPort() + ": mempool :" + mempool.values());
            }
        }
    }

    public void blockCatchUp(){

    }

    public void resolveBlock(){

    }

    //Reconcile blocks
    public void sendQuorumReady(){
        System.out.println("Node " + myAddress.getPort() + " sent quorum is ready");
        Block currentBlock = blockchain.get(blockchain.size() - 1);
        ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);
        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                try {
                    Socket s = new Socket(quorumAddress.getHost(), quorumAddress.getPort());
                    InputStream in = s.getInputStream();
                    ObjectInputStream oin = new ObjectInputStream(in);
                    OutputStream out = s.getOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    oout.writeObject(new Message(Message.Request.QUORUM_READY));
                    oout.flush();
                    Message messageReceived = (Message) oin.readObject();
                    Message reply;
                    if(messageReceived.getRequest().name().equals("RECONCILE_BLOCK")){
                        Object[] blockData = (Object[]) messageReceived.getMetadata();
                        int blockId = (Integer) blockData[0];
                        int blockHash = (Integer) blockData[1];

                        if(blockId == currentBlock.getBlockId()){

                        }else if (blockId < currentBlock.getBlockId()){
                            // tell them they are behind
                            reply = new Message(Message.Request.RECONCILE_BLOCK, currentBlock.getBlockId());
                        }else if (blockId > currentBlock.getBlockId()){
                            // we are behind, quorum already happened / failed
                            reply = new Message(Message.Request.PING);
                            blockCatchUp();
                        }

                    }

                    s.close();
                } catch (IOException e) {
                    System.out.println("Received IO Exception from node " + quorumAddress.getPort());
                    //removeAddress(address);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //Reconcile blocks
    public void receiveQuorumReady(ObjectOutputStream oout, ObjectInputStream oin){
        synchronized (quorumReadyVotesLock){
            System.out.println("Node " + myAddress.getPort() + ": receiveQuorumReady invoked");
            Block currentBlock = blockchain.get(blockchain.size() - 1);
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);
            try {
                if(!inQuorum()){
                    oout.writeObject(new Message(Message.Request.RECONCILE_BLOCK, new Object[]{currentBlock.getBlockId(), getBlockHash(currentBlock, 0)}));
                    oout.flush();
                    Message reply = (Message) oin.readObject();

                    if(reply.getRequest().equals("RECONCILE_BLOCK")){
                        blockCatchUp();
                    }
                }else{
                    oout.writeObject(new Message(Message.Request.PING));
                    oout.flush();

                    quorumReadyVotes++;
                    if(quorumReadyVotes == quorum.size() - 1){
                        quorumReadyVotes = 0;
                        sendMempoolHashes();
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMempoolHashes() {
        System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes invoked");

        HashSet<String> keys = new HashSet(mempool.keySet());
        ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);

        for (Address quorumAddress : quorum) {
            if (!myAddress.equals(quorumAddress)) {
                Socket s = null;
                try {
                    s = new Socket(quorumAddress.getHost(), quorumAddress.getPort());
                    InputStream in = s.getInputStream();
                    ObjectInputStream oin = new ObjectInputStream(in);
                    OutputStream out = s.getOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    oout.writeObject(new Message(Message.Request.RECEIVE_MEMPOOL, keys));
                    oout.flush();
                    Message messageReceived = (Message) oin.readObject();
                    if(messageReceived.getRequest().name().equals("REQUEST_TRANSACTION")){
                        ArrayList<String> hashesRequested = (ArrayList<String>) messageReceived.getMetadata();
                        ArrayList<Transaction> transactionsToSend = new ArrayList<>();
                        for(String hash : keys){
                            if(mempool.containsKey(hash)){
                                transactionsToSend.add(mempool.get(hash));
                            }else{
                                s.close();
                                throw new Exception();
                                // something is wrong
                            }
                        }
                        oout.writeObject(new Message(Message.Request.RECEIVE_MEMPOOL, transactionsToSend));
                    }else{
                    }
                    s.close();
                } catch (IOException e) {
                    System.out.println(e);
                    //throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    //throw new RuntimeException(e);
                } catch (Exception e){

                }
            }
        }
    }

    public void receiveMempool(Set<String> keys, ObjectOutputStream oout, ObjectInputStream oin) {
        synchronized (memPoolLock) {
            System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked");
            ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
            ArrayList<String> keysAbsent = new ArrayList<>();
            for (String key : keys) {
                if (!mempool.containsKey(key)) {
                    keysAbsent.add(key);
                }
            }
            try {
                if (keysAbsent.isEmpty()) {
                    oout.writeObject(new Message(Message.Request.PING));
                    oout.flush();
                } else {
                    oout.writeObject(new Message(Message.Request.REQUEST_TRANSACTION, keysAbsent));
                    oout.flush();
                    ArrayList<Transaction> transactionsReturned = (ArrayList<Transaction>) oin.readObject();
                    for(Transaction transaction : transactionsReturned){
                        try {
                            mempool.put(getSHAString(transaction.getData()), transaction);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

            memPoolRounds++;
            int i = quorum.size() - 1;
            if(memPoolRounds == quorum.size() - 1){
                memPoolRounds = 0;
                constructBlock();
            }
        }
    }

    public void constructBlock(){
        synchronized (memPoolLock){
            System.out.println("Node " + myAddress.getPort() + ": constructBlock invoked");

            HashMap<String, Transaction> blockTransactions = deepCloneHashmap(mempool);
            try {
                quorumBlock = new Block(blockTransactions,
                        getBlockHash(blockchain.get(blockchain.size() - 1), 0),
                                blockchain.size());
                sendSigOfBlockHash();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private Block quorumBlock;
    public void sendSigOfBlockHash(){
        System.out.println("Node " + myAddress.getPort() + ": sendSigOfBlockHash invoked");
        ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
        String blockHash;
        byte[] sig;

        try {blockHash = getBlockHash(quorumBlock, 0);
            sig = signBlockHash(blockHash, privateKey);
        } catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}

        BlockSignature blockSignature = new BlockSignature(sig, blockHash, myAddress);
        sendOneWayMessageQuorum(new Message(Message.Request.RECEIVE_SIGNATURE, blockSignature));
    }

    public void receiveQuorumSignature(BlockSignature signature){
        synchronized (sigRoundsLock){
            System.out.println("Node " + myAddress.getPort() + ": receiveQuorumSignature invoked");
            quorumSigs.add(signature);
            //sigRounds++;
            ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
            if(quorumSigs.size() == quorum.size() - 1){
                tallyQuorumSigs();
            }
        }
    }



    public void tallyQuorumSigs(){
        System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs invoked");
        ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
        HashMap<String, Integer> hashVotes = new HashMap<>();
        String quorumBlockHash;
        try {
            quorumBlockHash = getBlockHash(quorumBlock, 0);
            hashVotes.put(quorumBlockHash, 0);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        for(BlockSignature sig : quorumSigs){
            if(verifySignature(sig.getHash(), sig.getSignature(), sig.getAddress())){
                if(hashVotes.containsKey(sig.getHash())){
                    int votes = hashVotes.get(sig.getHash());
                    votes++;
                    hashVotes.put(sig.getHash(), votes);
                }else{
                    hashVotes.put(sig.getHash(), 0);
                }
            }else{
                /* Signature has failed. Authenticity or integrity compromised */
            }


        }

        String winningHash = quorumSigs.get(0).getHash();

        for(BlockSignature blockSignature : quorumSigs){
            String hash = blockSignature.getHash();
            if(hashVotes.get(hash) != null && (hashVotes.get(hash) > hashVotes.get(winningHash))){
                winningHash = hash;
            }
        }

        if(hashVotes.get(winningHash) == quorum.size()){
            if(quorumBlockHash.equals(winningHash)){
                sendSkeleton();

            }else{

            }
        }else{
            // failed
        }
    }

    public void sendSkeleton(){
        System.out.println("Node " + myAddress.getPort() + ": sendSkeleton invoked");
        BlockSkeleton skeleton = null;
        try {
            skeleton = new BlockSkeleton(quorumBlock.getBlockId(),
                    quorumBlock.getTxList().keySet(), quorumSigs, getBlockHash(quorumBlock, 0));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        for(Address address : localPeers){
            sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton));
        }
    }

    public void sendSkeleton(BlockSkeleton skeleton){
        System.out.println("Node " + myAddress.getPort() + ": sendSkeleton(local) invoked");
        for(Address address : localPeers){
            if(!address.equals(myAddress)){
                sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton));
            }
        }
    }

    public void receiveSkeleton(BlockSkeleton blockSkeleton){
        synchronized (blockLock){
            System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) invoked");
            Block currentBlock = blockchain.get(blockchain.size() - 1);
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);
            int verifiedSignatures = 0;
            String hash = blockSkeleton.getHash();

            if(currentBlock.getBlockId() + 1 != blockSkeleton.getBlockId()){
                return;
            }

            for(BlockSignature blockSignature : blockSkeleton.getSignatures()){
                Address address = blockSignature.getAddress();
                if(containsAddress(quorum, address)){
                    if(verifySignature(hash, blockSignature.getSignature(), address)){
                        verifiedSignatures++;
                    }
                }
            }

            if(verifiedSignatures == quorum.size() - 1){

            }

            Block newBlock = constructBlockWithSkeleton(blockSkeleton);
            blockchain.add(newBlock);
            sendSkeleton(blockSkeleton);
        }
    }

    public Block constructBlockWithSkeleton(BlockSkeleton skeleton){
        synchronized (memPoolLock){
            System.out.println("Node " + myAddress.getPort() + ": constructBlockWithSkeleton(local) invoked");
            HashSet<String> keys = (HashSet<String>) skeleton.getKeys();
            HashMap<String, Transaction> blockTransactions = new HashMap<>();
            for(String key : keys){
                if(mempool.containsKey(key)){
                    blockTransactions.put(key, mempool.get(key));
                    mempool.remove(key);
                }else{
                    // need to ask for trans
                }
            }

            Block newBlock;

            try {
                newBlock = new Block(blockTransactions,
                        getBlockHash(blockchain.get(blockchain.size() - 1), 0),
                        blockchain.size());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            return newBlock;
        }
    }








    /**
     * Adds a block
     * @param block Block to add
     */
    public void addBlock(Block block){
        blockchain.add(block);
        System.out.println("Added block " + block.getBlockId());
        if(inQuorum()){
            sendQuorumReady();
        }
    }

    public void sendOneWayMessageQuorum(Message message){
        ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                sendOneWayMessage(quorumAddress, message);
            }
        }
    }

    public boolean inQuorum(){
        synchronized (quorumLock){
            ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
            //System.out.println("Node " + myAddress.getPort() + " quorum: " + quorum);
            Boolean quorumMember = false;
            for(Address quorumAddress : quorum){
                if(myAddress.equals(quorumAddress)) {
                    quorumMember = true;
                }
            }
            return quorumMember;
        }
    }

    public ArrayList<Address> deriveQuorum(Block block, int nonce){
        String blockHash;
        if(block != null && block.getPrevBlockHash() != null){
            try {
                ArrayList<Address> quorum = new ArrayList<>();
                ArrayList<Integer> portsAdded = new ArrayList<>();

                blockHash = Hashing.getBlockHash(block, nonce);
                BigInteger bigInt = new BigInteger(blockHash, 16);
                bigInt = bigInt.mod(BigInteger.valueOf(NUM_NODES));
                int seed = bigInt.intValue();
                Random random = new Random(seed);
                for(int i = 0; i < QUORUM_SIZE; i++){
                    int port = STARTING_PORT + random.nextInt(NUM_NODES);
                    while(portsAdded.contains(port)){
                        port = STARTING_PORT + random.nextInt(NUM_NODES);
                    }
                    portsAdded.add(port);
                    quorum.add(new Address(port, "localhost"));
                }
                return quorum;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    /**
     * Acceptor is a thread responsible for maintaining the server socket by
     * accepting incoming connection requests, and starting a new ServerConnection
     * thread for each request. Requests terminate in a finite amount of steps, so
     * threads return upon completion.
     */
    class Acceptor extends Thread {
        Node node;

        Acceptor(Node node){
            this.node = node;
        }

        public void run() {
            Socket client;
            while (true) {
                try {
                    client = ss.accept();
                    new ServerConnection(client, node).start();
                } catch (IOException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * HeartBeatMonitor is a thread which will periodically 'ping' nodes which this node is connected to.
     * It expects a 'ping' back. Upon receiving the expected reply the other node is deemed healthy.
     *
     */
    class HeartBeatMonitor extends Thread {
        Node node;

        HeartBeatMonitor(Node node){
            this.node = node;
        }

        public void run() {
            while (true) {
                for(Address address : localPeers){
                    try {
                        Thread.sleep(30000);
                        Socket s = new Socket(address.getHost(), address.getPort());
                        InputStream in = s.getInputStream();
                        ObjectInputStream oin = new ObjectInputStream(in);
                        OutputStream out = s.getOutputStream();
                        ObjectOutputStream oout = new ObjectOutputStream(out);
                        Message message = new Message(Message.Request.PING);
                        oout.writeObject(message);
                        oout.flush();
                        Message messageReceived = (Message) oin.readObject();
//                        if(messageReceived.getRequest().name().equals("PING")){
//                            System.out.println("Node " + node.getAddress().getPort() + ": Node " + localPeers.get(0).getPort() + " pinged back");
//                        }else{
//                            System.out.println("Node " + node.getAddress().getPort() + ": Node " + localPeers.get(0).getPort() + " idk :(");
//                        }
                        s.close();
                        System.out.println("Node " + node.getAddress().getPort() + ": Node " + localPeers.get(0).getPort() + " mempool: " + mempool.values());
                    } catch (IOException e) {
                        System.out.println("Received IO Exception from node " + address.getPort());
                        //removeAddress(address);
                        System.out.println("Removing address");
                        System.out.println(e);
                        break;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        System.out.println("Received Interrupted Exception from node " + address.getPort());
                        throw new RuntimeException(e);
                    } catch (ConcurrentModificationException e){
                        break;
                    }
                }
            }
        }
    }
}

