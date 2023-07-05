package node;

import node.blockchain.*;
import node.blockchain.healthcare.*;
import node.blockchain.defi.DefiBlock;
import node.blockchain.defi.DefiTransaction;
import node.blockchain.defi.DefiTransactionValidator;
import node.blockchain.merkletree.MerkleTree;
import node.blockchain.prescription.ptBlock;
import node.blockchain.prescription.ptTransactionValidator;
import node.communication.*;
import node.communication.messaging.Message;
import node.communication.messaging.Messager;
import node.communication.messaging.MessagerPack;
import node.communication.utils.Hashing;
import node.communication.utils.Utils;

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
public abstract class Node  {

    /**
     * Node constructor creates node and begins server socket to accept connections
     *
     * @param port               Port
     * @param maxPeers           Maximum amount of peer connections to maintain
     * @param initialConnections How many nodes we want to attempt to connect to on start
     */
    public Node(String use, int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int minimumTransaction, int debugLevel) {

        /* Configurations */
        USE = use;
        MIN_CONNECTIONS = initialConnections;
        MAX_PEERS = maxPeers;
        NUM_NODES = numNodes;
        QUORUM_SIZE = quorumSize;
        DEBUG_LEVEL = debugLevel;
        MINIMUM_TRANSACTIONS = minimumTransaction;

        /* Locks for Multithreading */
        lock =  new Object();
        quorumLock = new Object();
        quorumReadyVotesLock = new Object();
        memPoolRoundsLock = new Object();
        sigRoundsLock = new Object();
        accountsLock = new Object();
        memPoolLock = new Object();
        blockLock = new Object();

        /* Multithreaded Counters for Stateful Servant */
        memPoolRounds = 0;
        quorumReadyVotes = 0;
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
        accountsToAlert = new HashMap<>();

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
    public LinkedList<Block> getBlockchain(){return blockchain;}

    /**
     * Initializes blockchain
     */
    public void initializeBlockchain(){
        blockchain = new LinkedList<Block>();

        if(USE.equals("Defi")){
            accounts = new HashMap<>();
            // DefiTransaction genesisTransaction = new DefiTransaction("Bob", "Alice", 100, "0");
            // HashMap<String, Transaction> genesisTransactions = new HashMap<String, Transaction>();
            // String hashOfTransaction = "";
            // hashOfTransaction = getSHAString(genesisTransaction.toString());
            // genesisTransactions.put(hashOfTransaction, genesisTransaction);
            addBlock(new DefiBlock(new HashMap<String, Transaction>(), "000000", 0));
        }else if(USE.equals("Prescription")){
            addBlock(new ptBlock(new HashMap<String, Transaction>(), "000000", 0));
        }
    }

    /**
     * Determines if a connection is eligible
     * @param address Address to verify
     * @param connectIfEligible Connect to address if it is eligible
     * @return True if eligible, otherwise false
     */
    public boolean eligibleConnection(Address address, boolean connectIfEligible){
        synchronized(lock) {
            if (localPeers.size() < MAX_PEERS - 1 && (!address.equals(this.getAddress()) && !containsAddress(localPeers, address))) {
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
                Messager.sendOneWayMessage(address, new Message(Message.Request.ADD_TRANSACTION, transaction), myAddress);
            }
        }
    }

    public void addTransaction(Transaction transaction){
        while(state != 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        verifyTransaction(transaction);
    }


    public void verifyTransaction(Transaction transaction){
        synchronized(memPoolLock){
            if(Utils.containsTransactionInMap(transaction, mempool)) return;

            if(DEBUG_LEVEL == 1){System.out.println("Node " + myAddress.getPort() + ": verifyTransaction: " + 

            transaction.getUID() + ", blockchain size: " + blockchain.size());}
            LinkedList<Block> clonedBlockchain = new LinkedList<>();


            clonedBlockchain.addAll(blockchain);
            for(Block block : clonedBlockchain){
                if(block.getTxList().containsKey(getSHAString(transaction.getUID()))){
                    // We have this transaction in a block
                    if(DEBUG_LEVEL == 1){System.out.println("Node " + myAddress.getPort() + ": trans :" + transaction.getUID() + " found in prev block " + block.getBlockId());}
                    return;
                }
            }

            TransactionValidator tv;
            Object[] validatorObjects = new Object[3];

            if(USE.equals("Defi")){
                tv = new DefiTransactionValidator();
            
                validatorObjects[0] = transaction;
                validatorObjects[1] = accounts;
                validatorObjects[2] = mempool;

            }else{
                tv = new HCTransactionValidator(); // To be changed to another use case in the future
            }

            if(!tv.validate(validatorObjects)){
                if(DEBUG_LEVEL == 1){System.out.println("Node " + myAddress.getPort() + "Transaction not valid");}
                return;
            }

            mempool.put(getSHAString(transaction.getUID()), transaction);
            gossipTransaction(transaction);

            if(DEBUG_LEVEL == 1){System.out.println("Node " + myAddress.getPort() + ": Added transaction. MP:" + mempool.values());}
        }         
    }

    //Reconcile blocks
    public void sendQuorumReady(){
        //state = 1;
        stateChangeRequest(1);
        quorumSigs = new ArrayList<>();
        Block currentBlock = blockchain.getLast();
        ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);

        if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + " sent quorum is ready for q: " + quorum);

        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                try {
                    Thread.sleep(2000);
                    MessagerPack mp = Messager.sendInterestingMessage(quorumAddress, new Message(Message.Request.QUORUM_READY), myAddress);
                    Message messageReceived = mp.getMessage();
                    Message reply = new Message(Message.Request.PING);

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
                            //blockCatchUp();

                        }
                        mp.getOout().writeObject(reply);
                        mp.getOout().flush();
                    }

                    mp.getSocket().close();
                } catch (IOException e) {
                    System.out.println("Node " + myAddress.getPort() + ": sendQuorumReady Received IO Exception from node " + quorumAddress.getPort());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //Reconcile blocks
    public void receiveQuorumReady(ObjectOutputStream oout, ObjectInputStream oin){
        synchronized (quorumReadyVotesLock){
            while(state != 1){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Block currentBlock = blockchain.getLast();
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);

            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": receiveQuorumReady invoked for " + quorum );

            try {

                if(!inQuorum()){
                    if(DEBUG_LEVEL == 1) {
                        System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress);
                    }
                    oout.writeObject(new Message(Message.Request.RECONCILE_BLOCK, new Object[]{currentBlock.getBlockId(), getBlockHash(currentBlock, 0)}));
                    oout.flush();
                    Message reply = (Message) oin.readObject();

                    if(reply.getRequest().name().equals("RECONCILE_BLOCK")){
                        //blockCatchUp();
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
        synchronized (memPoolLock){
            stateChangeRequest(2);

            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes invoked");
            
            HashSet<String> keys = new HashSet<String>(mempool.keySet());
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);
            
            for (Address quorumAddress : quorum) {
                if (!myAddress.equals(quorumAddress)) {
                    try {
                        MessagerPack mp = Messager.sendInterestingMessage(quorumAddress, new Message(Message.Request.RECEIVE_MEMPOOL, keys), myAddress);                        ;
                        Message messageReceived = mp.getMessage();
                        if(messageReceived.getRequest().name().equals("REQUEST_TRANSACTION")){
                            ArrayList<String> hashesRequested = (ArrayList<String>) messageReceived.getMetadata();
                            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes: requested trans: " + hashesRequested);
                            ArrayList<Transaction> transactionsToSend = new ArrayList<>();
                            for(String hash : keys){
                                if(mempool.containsKey(hash)){
                                    transactionsToSend.add(mempool.get(hash));
                                }else{
                                    if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes: requested trans not in mempool. MP: " + mempool);
                                }
                            }
                            mp.getOout().writeObject(new Message(Message.Request.RECEIVE_MEMPOOL, transactionsToSend));
                        }
                        mp.getSocket().close();
                    } catch (IOException e) {
                        System.out.println(e);
                    } catch (Exception e){
                        System.out.println(e);
                    }
                }
            }
        }
    }

    public void receiveMempool(Set<String> keys, ObjectOutputStream oout, ObjectInputStream oin) {
        while(state != 2){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        resolveMempool(keys, oout, oin);
    }


    public void resolveMempool(Set<String> keys, ObjectOutputStream oout, ObjectInputStream oin) {
        synchronized(memPoolRoundsLock){
            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked"); 
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);
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
                    if(DEBUG_LEVEL == 1) {System.out.println("Node " + myAddress.getPort() + ": receiveMempool requesting transactions for: " + keysAbsent); }
                    oout.writeObject(new Message(Message.Request.REQUEST_TRANSACTION, keysAbsent));
                    oout.flush();
                    Message message = (Message) oin.readObject();
                    ArrayList<Transaction> transactionsReturned = (ArrayList<Transaction>) message.getMetadata();
                    
                    for(Transaction transaction : transactionsReturned){
                        mempool.put(getSHAString(transaction.getUID()), transaction);
                        if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": recieved transactions: " + keysAbsent);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

            memPoolRounds++;
            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked: mempoolRounds: " + memPoolRounds); 
            if(memPoolRounds == quorum.size() - 1){
                memPoolRounds = 0;
                constructBlock();
            }
        }
    }

    public void constructBlock(){
        synchronized(memPoolLock){
            if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": constructBlock invoked");
            stateChangeRequest(3);
            
            /* Make sure compiled transactions don't conflict */
            HashMap<String, Transaction> blockTransactions = new HashMap<>();

            TransactionValidator tv;
            if(USE.equals("Defi")){
                tv = new DefiTransactionValidator();
            }else if(USE.equals("Prescription")){
                // Room to enable another use case 
                tv = new ptTransactionValidator();
            }

            
            for(String key : mempool.keySet()){
                Transaction transaction = mempool.get(key);
                Object[] validatorObjects = new Object[3];
                if(USE.equals("Defi")){
                    validatorObjects[0] = transaction;
                    validatorObjects[1] = accounts;
                    validatorObjects[2] = blockTransactions;
                }else if(USE.equals("Prescription")){
                    // Validator objects will change according to another use case
                }
                tv.validate(validatorObjects);
                blockTransactions.put(key, transaction);
            }

            try {
                if(USE.equals("Defi")){
                    quorumBlock = new DefiBlock(blockTransactions,
                        getBlockHash(blockchain.getLast(), 0),
                                blockchain.size());
                }else if{

                    // Room to enable another use case 
                    quorumBlock = new ptBlock(blockTransactions,
                        getBlockHash(blockchain.getLast(), 0),
                                blockchain.size());
                }

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            sendSigOfBlockHash();
        }
    }

    public void sendSigOfBlockHash(){
        String blockHash;
        byte[] sig;

        try {blockHash = getBlockHash(quorumBlock, 0);
            sig = signHash(blockHash, privateKey);
        } catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}

        BlockSignature blockSignature = new BlockSignature(sig, blockHash, myAddress);
        sendOneWayMessageQuorum(new Message(Message.Request.RECEIVE_SIGNATURE, blockSignature));

        if(DEBUG_LEVEL == 1) {System.out.println("Node " + myAddress.getPort() + ": sendSigOfBlockHash invoked for hash: " + blockHash.substring(0, 4));}
    }

    public void receiveQuorumSignature(BlockSignature signature){
        synchronized (sigRoundsLock){
            if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": 1st part receiveQuorumSignature invoked. state: " + state);}

            while(state != 3){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);

            if(!containsAddress(quorum, signature.getAddress())){
                if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": false sig from " + signature.getAddress());
                return;
            }

            if(!inQuorum()){
                if(DEBUG_LEVEL == 1) System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress); 
                return;
            } 

            quorumSigs.add(signature);
            int blockId = blockchain.size() - 1;

            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveQuorumSignature invoked from " + 
                signature.getAddress().toString() + " qSigs: " + quorumSigs + " quorum: " + quorum + " block " + quorumBlock.getBlockId());
            }

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
            resetMempool();

            if (DEBUG_LEVEL == 1) {System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs invoked");}

            //state = 4;
            stateChangeRequest(4);
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);

            if(!inQuorum()){
                System.out.println("Node " + myAddress.getPort() + ": tQs: not in quorum? q: " + quorum + " my addr: " + myAddress);
                return;
            }

            HashMap<String, Integer> hashVotes = new HashMap<>();
            String quorumBlockHash;
            int block = blockchain.size() - 1;
            try {                
                if(quorumBlock == null){
                    System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs quorum null");
                }

                quorumBlockHash = getBlockHash(quorumBlock, 0);
                hashVotes.put(quorumBlockHash, 1);;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            for (BlockSignature sig : quorumSigs) {
                if (verifySignatureFromRegistry(sig.getHash(), sig.getSignature(), sig.getAddress())) {
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
                    sendSkeleton();
                    addBlock(quorumBlock);
                    if(quorumBlock == null){
                        System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs quorum null");

                    }                    
                } else {
                    System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs: quorumBlockHash does not equals(winningHash)");
                }
            } else {
                System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs: failed vote. votes: " + hashVotes + " my block " + quorumBlock.getBlockId() + " " + quorumBlockHash.substring(0, 4) +
                " quorumSigs: " + quorumSigs);
            } 
            hashVotes.clear();
            quorumSigs.clear();
        }
    }

    private void resetMempool(){
        synchronized(memPoolLock){
            mempool = new HashMap<>();
        }
    }

    public void sendSkeleton(){
        synchronized (lock){
            //state = 0;

            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": sendSkeleton invoked. qSigs " + quorumSigs);
            }
            BlockSkeleton skeleton = null;
            try {
                if(quorumBlock == null){
                    System.out.println("Node " + myAddress.getPort() + ": sendSkeleton quorum null");

                }
                skeleton = new BlockSkeleton(quorumBlock.getBlockId(),
                        new ArrayList<String>(quorumBlock.getTxList().keySet()), quorumSigs, getBlockHash(quorumBlock, 0));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            for(Address address : localPeers){
                Messager.sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton), myAddress);
            }

        }
    }

    public void sendSkeleton(BlockSkeleton skeleton){
        synchronized (lock){
            if(DEBUG_LEVEL == 1) {
                System.out.println("Node " + myAddress.getPort() + ": sendSkeleton(local) invoked: BlockID " + skeleton.getBlockId());
            }
            for(Address address : localPeers){
                if(!address.equals(myAddress)){
                    Messager.sendOneWayMessage(address, new Message(Message.Request.RECEIVE_SKELETON, skeleton), myAddress);
                }
            }
        }
    }

    public void receiveSkeleton(BlockSkeleton blockSkeleton){
        while(state != 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        validateSkeleton(blockSkeleton);
    }

    public void validateSkeleton(BlockSkeleton blockSkeleton){
        synchronized (blockLock){
            Block currentBlock = blockchain.getLast();

            if(currentBlock.getBlockId() + 1 != blockSkeleton.getBlockId()){
                //if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) currentblock not synced with skeleton. current id: " + currentBlock.getBlockId() + " new: " + blockSkeleton.getBlockId()); }
                return;
            }else{
                if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) invoked. Hash: " + blockSkeleton.getHash());}
            }

            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0);
            int verifiedSignatures = 0;
            String hash = blockSkeleton.getHash();

            if(blockSkeleton.getSignatures().size() < 1){
                if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": No signatures. blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId() 
                + " quorum: " + quorum ); }
            }

            for(BlockSignature blockSignature : blockSkeleton.getSignatures()){
                Address address = blockSignature.getAddress();
                if(containsAddress(quorum, address)){
                    if(verifySignatureFromRegistry(hash, blockSignature.getSignature(), address)){
                        verifiedSignatures++;
                    }else{
                        if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": Failed to validate signature. blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId()); };
                    }
                }else{
                    if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId()
                    + " quorum: " + quorum + ". Address: " + address);}
                }
            }

            if(verifiedSignatures != quorum.size() - 1){
                if(DEBUG_LEVEL == 1) { System.out.println("Node " + myAddress.getPort() + ": sigs not verified for block " + blockSkeleton.getBlockId() + 
                ". Verified sigs: " + verifiedSignatures + ". Needed: " + quorum.size() + " - 1."); }
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

            if(USE.equals("Defi")){
                try {
                    newBlock = new DefiBlock(blockTransactions,
                            getBlockHash(blockchain.getLast(), 0),
                            blockchain.size());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }else if(USE.equals("Prescription")){
                try {
                    newBlock = new ptBlock(blockTransactions,
                            getBlockHash(blockchain.getLast(), 0),
                            blockchain.size());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            

            return newBlock;
        }
    }

    private Object stateLock = new Object();
    private void stateChangeRequest(int statetoChange){
        synchronized(stateLock){
            state = statetoChange;
        }
    }

    /**
     * Adds a block
     * @param block Block to add
     */
    public void addBlock(Block block){
        stateChangeRequest(0);
        // state = 0;
        
        HashMap<String, Transaction> txMap = block.getTxList();
        HashSet<String> keys = new HashSet<>(txMap.keySet());
        ArrayList<Transaction> txList = new ArrayList<>();
        for(String hash : txMap.keySet()){
            txList.add(txMap.get(hash));
        }

        MerkleTree mt = new MerkleTree(txList);
        if(mt.getRootNode() != null) block.setMerkleRootHash(mt.getRootNode().getHash());

        blockchain.add(block);
        System.out.println("Node " + myAddress.getPort() + ": " + chainString(blockchain) + " MP: " + mempool.values());

        if(USE.equals("Defi")){
            HashMap<String, DefiTransaction> defiTxMap = new HashMap<>();

            for(String key : keys){
                DefiTransaction transactionInList = (DefiTransaction) txMap.get(key);
                defiTxMap.put(key, transactionInList);
            }

            DefiTransactionValidator.updateAccounts(defiTxMap, accounts);

            synchronized(accountsLock){
                for(String account : accountsToAlert.keySet()){
                    // System.out.println(account);
                    for(String transHash : txMap.keySet()){
                        DefiTransaction dtx = (DefiTransaction) txMap.get(transHash);
                        // System.out.println(dtx.getFrom() + "---" + dtx.getTo());
                        if(dtx.getFrom().equals(account) ||
                        dtx.getTo().equals(account)){
                            Messager.sendOneWayMessage(accountsToAlert.get(account), 
                            new Message(Message.Request.ALERT_WALLET, mt.getProof(txMap.get(transHash))), myAddress);
                            //System.out.println("sent update");
                        }
                    }
                }
            }
        }
        /* need to add elif for use prescription. */

        ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);

        if(DEBUG_LEVEL == 1) {
            System.out.println("Node " + myAddress.getPort() + ": Added block " + block.getBlockId() + ". Next quorum: " + quorum);
        }

        if(inQuorum()){
            while(mempool.size() < MINIMUM_TRANSACTIONS){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendQuorumReady();
        }
    }

    public void sendOneWayMessageQuorum(Message message){
        ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);
        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                Messager.sendOneWayMessage(quorumAddress, message, myAddress);
            }
        }
    }

    public boolean inQuorum(){
        synchronized (quorumLock){
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);
            Boolean quorumMember = false;
            for(Address quorumAddress : quorum){
                if(myAddress.equals(quorumAddress)) {
                    quorumMember = true;
                }
            }
            return quorumMember;
        }
    }

    public boolean inQuorum(Block block){
        synchronized (quorumLock){
            if(block.getBlockId() - 1 != blockchain.getLast().getBlockId()){ // 
                return false;
            }
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0);
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
                ArrayList<Address> quorum = new ArrayList<>(); // New list for returning a quorum, list of addr
                blockHash = Hashing.getBlockHash(block, nonce); // gets the hash of the block
                BigInteger bigInt = new BigInteger(blockHash, 16); // Converts the hex hash in to a big Int
                bigInt = bigInt.mod(BigInteger.valueOf(NUM_NODES)); // we mod the big int I guess
                int seed = bigInt.intValue(); // This makes our seed
                Random random = new Random(seed); // Makes our random in theory the same across all healthy nodes
                int quorumNodeIndex; // The index from our global peers from which we select nodes to participate in next quorum
                Address quorumNode; // The address of thenode from the quorumNode Index to go in to the quorum
                //System.out.println("Node " + myAddress.getPort() + ": blockhash" + chainString(blockchain));
                while(quorum.size() < QUORUM_SIZE){
                    quorumNodeIndex = random.nextInt(NUM_NODES); // may be wrong but should still work
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

    private HashMap<String, Address> accountsToAlert;

    public void alertWallet(String accountPubKey, Address address){
        synchronized(accountsLock){
            accountsToAlert.put(accountPubKey, address);
        }
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
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                for(Address address : localPeers){
                    try {                 
                        Thread.sleep(10000);
                        Message messageReceived = Messager.sendTwoWayMessage(address, new Message(Message.Request.PING), myAddress);

                        /* Use heartbeat to also output the block chain of the node */

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

    private final int MAX_PEERS, NUM_NODES, QUORUM_SIZE, MIN_CONNECTIONS, DEBUG_LEVEL, MINIMUM_TRANSACTIONS;
    private final Object lock, quorumLock, memPoolLock, quorumReadyVotesLock, memPoolRoundsLock, sigRoundsLock, blockLock, accountsLock;
    private int quorumReadyVotes, memPoolRounds;
    private ArrayList<Address> globalPeers;
    private ArrayList<Address> localPeers;
    private HashMap<String, Transaction> mempool;
    HashMap<String, Integer> accounts;
    private ArrayList<BlockSignature> quorumSigs;
    private LinkedList<Block> blockchain;
    private final Address myAddress;
    private ServerSocket ss;
    private Block quorumBlock;
    private PrivateKey privateKey;
    private int state;
    private final String USE;

}

