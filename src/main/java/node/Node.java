package node;

import node.blockchain.*;
import node.communication.*;
import node.communication.utils.Hashing;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;

import static node.communication.utils.DSA.*;
import static node.communication.utils.Hashing.getBlockHash;
import static node.communication.utils.Hashing.getSHAString;
import static node.communication.utils.Utils.*;


/**
 * A Node represents a peer, a cooperating member within a network following this Quorum-based blockchain protocol
 * as implemented here.
 *
 * This node participates in a distributed and decentralized network protocol, and achieves this by using some of
 * the following architecture features:
 *
 *      Quorum Consensus
 *      DSA authentication
 *      Blockchain using SHA-256
 *      Multithreading
 *      Servant Model
 *      Stateful Model
 *      TCP/IP communication
 *
 *
 * Beware, any methods below are a WIP
 */
public class Node  {

    /**
     * Node constructor creates node and begins server socket to accept connections
     *
     * @param port               Port
     * @param maxPeers           Maximum amount of peer connections to maintain
     * @param initialConnections How many nodes we want to attempt to connect to on start
     */
    public Node(int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int startingPort, int debugLevel) {

        /* Configurations */
        MIN_CONNECTIONS = initialConnections;
        MAX_PEERS = maxPeers;
        NUM_NODES = numNodes;
        QUORUM_SIZE = quorumSize;
        STARTING_PORT = startingPort;
        DEBUG_LEVEL = debugLevel;

        /* Locks for Multithreading */
        lock =  new Object();
        quorumLock = new Object();
        quorumReadyVotesLock = new Object();
        memPoolRoundsLock = new Object();
        sigRoundsLock = new Object();
        memPoolLock = new Object();
        blockLock = new Object();

        /* Multithreaded Counters for Stateful Servant */
        memPoolRounds = 0;
        quorumReadyVotes = 0;
        sigRounds = 0;
        state = 0;

        InetAddress ip;

        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String host = ip.getHostAddress();

        /* Other Data for Stateful Servant */
        myAddress = new Address(port, host);
        localPeers = new ArrayList<>();
        mempool = new HashMap<>();

        /* Public-Private (DSA) Keys*/
        KeyPair keys = generateDSAKeyPair();
        privateKey = keys.getPrivate();
        writePubKeyToRegistry(myAddress, keys.getPublic());

        /* Begin Server Socket */
        try {
            ss = new ServerSocket(port);
            Acceptor acceptor = new Acceptor(this);
            acceptor.start();
            System.out.println("Node up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /* A collection of getters */
    public int getMaxPeers(){return this.MAX_PEERS;}
    public int getMinConnections(){return this.MIN_CONNECTIONS;}
    public Address getAddress(){return this.myAddress;}
    public ArrayList<Address> getLocalPeers(){return this.localPeers;}
    public HashMap<String, Transaction> getMempool(){return this.mempool;}
    public ArrayList<Block> getBlockchain(){return blockchain;}

    /**
     * Initializes blockchain
     */
    public void initializeBlockchain(){
        blockchain = new ArrayList<Block>();
        addBlock(new Block(new HashMap<String, Transaction>(), "000000", 0));
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
        synchronized (lock){
            localPeers.add(address);
        }
    }

    /**
     * Iterate through a list of peers and attempt to establish a mutual connection
     * with a specified amount of nodes
     * @param globalPeers
     */
    public void requestConnections(ArrayList<Address> globalPeers){
        try {
            this.globalPeers = globalPeers;
            if(globalPeers.size() > 0){
                /* Begin seeking connections */
                ClientConnection connect = new ClientConnection(this, globalPeers);
                connect.start();

                /* Begin heartbeat monitor */
                Thread.sleep(10000);
                HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor(this);
                heartBeatMonitor.start();

                /* Begin protocol */
                initializeBlockchain();
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
        synchronized (lock){
            for (Address existingAddress : localPeers) {
                if (existingAddress.equals(address)) {
                    localPeers.remove(address);
                    return address;
                }
            }
            return null;
        }
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
                    System.out.println("Node " + myAddress.getPort() + ": gossipTransaction: Received IO Exception from node " + address.getPort());
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
            System.out.println("Node " + myAddress.getPort() + ": sendOneWayMessage: Received IO Exception from node " + address.getPort());
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
            System.out.println("Node " + myAddress.getPort() + ": sendTwoWayMessage: Received IO Exception from node " + address.getPort());
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
                if(DEBUG_LEVEL == 1){
                    System.out.println("Node " + myAddress.getPort() + ": mempool :" + mempool.values());
                }
            }
        }
    }

    public void blockCatchUp(){}

    //Reconcile blocks
    public void sendQuorumReady(){
        state = 1;
        quorumSigs = new ArrayList<>();
        if(DEBUG_LEVEL == 1){
            System.out.println("Node " + myAddress.getPort() + " sent quorum is ready");
        }
        Block currentBlock = blockchain.get(blockchain.size() - 1);
        ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);

        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                try {
                    Thread.sleep(2000);
                    Socket s = new Socket(quorumAddress.getHost(), quorumAddress.getPort());
                    InputStream in = s.getInputStream();
                    ObjectInputStream oin = new ObjectInputStream(in);
                    OutputStream out = s.getOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    oout.writeObject(new Message(Message.Request.QUORUM_READY));
                    oout.flush();
                    Message messageReceived = (Message) oin.readObject();
                    Message reply = new Message(Message.Request.PING);;
                    if(messageReceived.getRequest().name().equals("RECONCILE_BLOCK")){
                        Object[] blockData = (Object[]) messageReceived.getMetadata();
                        int blockId = (Integer) blockData[0];
                        String blockHash = (String) blockData[1];

                        if(blockId == currentBlock.getBlockId()){

                        }else if (blockId < currentBlock.getBlockId()){
                            // tell them they are behind
                            reply = new Message(Message.Request.RECONCILE_BLOCK, currentBlock.getBlockId());
                            if(DEBUG_LEVEL == 1) {
                                System.out.println("Node " + myAddress.getPort() + ": sendQuorumReady RECONCILE");
                            }
                        }else if (blockId > currentBlock.getBlockId()){
                            // we are behind, quorum already happened / failed
                            reply = new Message(Message.Request.PING);
                            blockCatchUp();
                        }

                        oout.writeObject(reply);
                        oout.flush();

                    }

                    s.close();
                } catch (IOException e) {
                    System.out.println("Node " + myAddress.getPort() + ": sendQuorumReady Received IO Exception from node " + quorumAddress.getPort());
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
            if(state > 1) return;


            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveQuorumReady invoked");
            }
            Block currentBlock = blockchain.get(blockchain.size() - 1);
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);

            try {
                if(!inQuorum()){
                    if(DEBUG_LEVEL == 1) {
                        System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress);
                    }
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
                System.out.println("Node " + myAddress.getPort() + ": receiveQuorumReady EOF");
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMempoolHashes() {
        state = 2;
        if(DEBUG_LEVEL == 1) {
            System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes invoked");
        }
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
            if(state > 2) return;

            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked");
            }
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
                    Message message = (Message) oin.readObject();
                    ArrayList<Transaction> transactionsReturned = (ArrayList<Transaction>) message.getMetadata();
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

    private Block quorumBlock;
    public void constructBlock(){
        synchronized (memPoolLock){
            state = 3;

            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": constructBlock invoked");
            }
            HashMap<String, Transaction> blockTransactions = deepCloneHashmap(mempool);
            mempool = new HashMap<>();
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

    public void sendSigOfBlockHash(){
        if(DEBUG_LEVEL == 1) {
            System.out.println("Node " + myAddress.getPort() + ": sendSigOfBlockHash invoked");
        }
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


            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveQuorumSignature invoked");
            }

            
            //if(state > 3) return;

            if(!inQuorum()) return;

            while(state != 3){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            quorumSigs.add(signature);
            int blockId = blockchain.size() - 1;
            //sigRounds++;
            ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);
            if(quorumSigs.size() == quorum.size() - 1){
                if(!inQuorum()){
                    if(DEBUG_LEVEL == 1) {
                        System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress);
                    }
                    System.out.println("Node " + myAddress.getPort() + ": rQs: not in quorum? q: " + quorum + " my addr: " + myAddress + " block: " + blockId);
                    return;
                }
                tallyQuorumSigs();
            }
        }
    }

    public void tallyQuorumSigs(){
        synchronized (blockLock) {
            state = 4;
            System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs invoked");
            if (DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs invoked");
            }
            ArrayList<Address> quorum = deriveQuorum(blockchain.get(blockchain.size() - 1), 0);

            if(!inQuorum()){
                System.out.println("Node " + myAddress.getPort() + ": tQs: not in quorum? q: " + quorum + " my addr: " + myAddress);

                // if(DEBUG_LEVEL == 1) {
                //     System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress);
                // }
                return;
            }

            if(!containsAddress(quorum, myAddress)){
                return;
            }

            HashMap<String, Integer> hashVotes = new HashMap<>();
            String quorumBlockHash;
            int block = blockchain.size() - 1;
            try {
                System.out.println("Node " + myAddress.getPort() + ": q: " + quorum + " my addr: " + myAddress + " block: " + block);
                
                if(quorumBlock == null){
                    System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs quorum null");
                }

                quorumBlockHash = getBlockHash(quorumBlock, 0);
                hashVotes.put(quorumBlockHash, 1);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            for (BlockSignature sig : quorumSigs) {
                if (verifySignature(sig.getHash(), sig.getSignature(), sig.getAddress())) {
                    if (hashVotes.containsKey(sig.getHash())) {
                        int votes = hashVotes.get(sig.getHash());
                        votes++;
                        hashVotes.put(sig.getHash(), votes);
                    } else {
                        hashVotes.put(sig.getHash(), 0);
                    }
                } else {
                    /* Signature has failed. Authenticity or integrity compromised */
                }


            }

            String winningHash = quorumSigs.get(0).getHash();

            for (BlockSignature blockSignature : quorumSigs) {
                String hash = blockSignature.getHash();
                if (hashVotes.get(hash) != null && (hashVotes.get(hash) > hashVotes.get(winningHash))) {
                    winningHash = hash;
                }
            }
            if (DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs: Winning hash votes = " + hashVotes.get(winningHash));
            }
            if (hashVotes.get(winningHash) == quorum.size()) {
                if (quorumBlockHash.equals(winningHash)) {
                    addBlock(quorumBlock);
                    if(quorumBlock == null){
                        System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs quorum null");

                    }
                    sendSkeleton();

                } else {

                }
            } else {
                // failed
            }
        }
    }

    public void sendSkeleton(){
        synchronized (lock){
            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": sendSkeleton invoked");
            }
            BlockSkeleton skeleton = null;
            try {
                if(quorumBlock == null){
                    System.out.println("Node " + myAddress.getPort() + ": sendSkeleton quorum null");

                }
                skeleton = new BlockSkeleton(quorumBlock.getBlockId(),
                        new ArrayList(quorumBlock.getTxList().keySet()), quorumSigs, getBlockHash(quorumBlock, 0));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            for(Address address : localPeers){
                sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton));
            }
        }
    }

    public void sendSkeleton(BlockSkeleton skeleton){
        synchronized (lock){
            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": sendSkeleton(local) invoked");
            }
            for(Address address : localPeers){
                if(!address.equals(myAddress)){
                    sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton));
                }
            }
        }
    }

    public void receiveSkeleton(BlockSkeleton blockSkeleton){
        synchronized (blockLock){
            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) invoked");
            }
            Block currentBlock = blockchain.get(blockchain.size() - 1);
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);
            int verifiedSignatures = 0;
            String hash = blockSkeleton.getHash();

            if(currentBlock.getBlockId() + 1 == blockSkeleton.getBlockId()){
                //System.out.println("Node " + myAddress.getPort() + ": currentBlock.getBlockId() + 1 != blockSkeleton.getBlockId()");
                //return;
            }else{
                if(blockchain.size() < 2){
                    System.out.println("Node " + myAddress.getPort() + ": Failed check. Current block id: " + currentBlock.getBlockId() + " skeletonID: " + blockSkeleton.getBlockId() + " quorum: " + quorum);
                }
                return;
            }

            for(BlockSignature blockSignature : blockSkeleton.getSignatures()){
                Address address = blockSignature.getAddress();
                if(containsAddress(quorum, address)){
                    if(verifySignature(hash, blockSignature.getSignature(), address)){
                        verifiedSignatures++;
                    }
                }else{
                    System.out.println("gg");
                }
            }

            if(verifiedSignatures != quorum.size() - 1){
                System.out.println("Node " + myAddress.getPort() + ": sigs not verified for block " + blockSkeleton.getBlockId() + 
                ". Verified sigs: " + verifiedSignatures + ". Needed: " + quorum.size() + " - 1.");
                return;
            }

            Block newBlock = constructBlockWithSkeleton(blockSkeleton);
            addBlock(newBlock);
            sendSkeleton(blockSkeleton);

        }
    }

    public Block constructBlockWithSkeleton(BlockSkeleton skeleton){
        synchronized (memPoolLock){
            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": constructBlockWithSkeleton(local) invoked");
            }
            ArrayList<String> keys = skeleton.getKeys();
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
        if(DEBUG_LEVEL == 1) {
            System.out.println("Node + " + myAddress.getPort() + ": Added block " + block.getBlockId());
        }
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
                blockHash = Hashing.getBlockHash(block, nonce);
                BigInteger bigInt = new BigInteger(blockHash, 16);
                bigInt = bigInt.mod(BigInteger.valueOf(NUM_NODES));
                int seed = bigInt.intValue();
                Random random = new Random(seed);
                int quorumNodeIndex;
                Address quorumNode;
                while(quorum.size() < QUORUM_SIZE){
                    quorumNodeIndex = random.nextInt(NUM_NODES);
                    quorumNode = globalPeers.get(quorumNodeIndex);
                    if(!containsAddress(quorum, quorumNode)){
                        quorum.add(globalPeers.get(quorumNodeIndex));
                    }
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
                Address address;
                for(int i = 0; i < localPeers.size(); i++){
                    address = localPeers.get(i);
                    try {
                        Thread.sleep(10000);
                        Socket s = new Socket(address.getHost(), address.getPort());
                        InputStream in = s.getInputStream();
                        ObjectInputStream oin = new ObjectInputStream(in);
                        OutputStream out = s.getOutputStream();
                        ObjectOutputStream oout = new ObjectOutputStream(out);
                        Message message = new Message(Message.Request.PING);
                        oout.writeObject(message);
                        oout.flush();
                        Message messageReceived = (Message) oin.readObject();
                        s.close();
                        if(blockchain.size() < 2){
                            //System.out.println("Node " + node.getAddress().getPort() + ": Peers: " + localPeers);
                        }else{
                            System.out.println("Node " + node.getAddress().getPort() + ": " + chainString(blockchain));
                        }
                    } catch (IOException e) {
                        System.out.println("Node " + myAddress.getPort() + ": HeartBeatMonitor: Received IO Exception from node " + address.getPort());
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
                        System.out.println(e);
                        break;
                    } catch (IndexOutOfBoundsException e){
                        System.out.println(e);
                    }
                }
            }
        }
    }

    private final int MAX_PEERS, NUM_NODES, QUORUM_SIZE, STARTING_PORT, MIN_CONNECTIONS, DEBUG_LEVEL;
    private final Object lock, quorumLock, memPoolLock, quorumReadyVotesLock, memPoolRoundsLock, sigRoundsLock, blockLock;
    private int quorumReadyVotes, memPoolRounds, sigRounds;
    private ArrayList<Address> globalPeers;
    private ArrayList<Address> localPeers;
    private HashMap<String, Transaction> mempool;
    private ArrayList<BlockSignature> quorumSigs;
    private ArrayList<Block> blockchain;
    private final Address myAddress;
    private ServerSocket ss;
    private PrivateKey privateKey;
    private int state;

}

