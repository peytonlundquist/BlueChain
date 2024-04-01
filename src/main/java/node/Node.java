package node;

import static utils.DSA.*;
import static utils.Hashing.*;
import static utils.Utils.*;

import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;

import blockchain.*;
import blockchain.usecases.defi.*;
import blockchain.usecases.healthcare.*;
import communication.*;
import communication.messaging.*;
import utils.*;
import utils.merkletree.MerkleTree;


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
     * Constructs a Node and initiates a server socket to accept connections.
     *
     * @param configValues       Config values for the node.
     * @param port               Port number on which the node will listen for connections.
     */
    public Node(Config configValues, int port) {

        /* Configurations */
        this.configValues = configValues;

        /* Locks for Multithreading */
        Set<String> locks = new HashSet<>(Arrays.asList("lock", "quorumLock", "memPoolLock", "quorumReadyVotesLock", 
                                            "memPoolRoundsLock", "sigRoundsLock","blockLock", "accountsLock", "stateLock"));
        lockManager = new LockManager(locks);
        stateManager = new StateManager();


        /* Multithreaded Counters for Stateful Servant */
        memPoolRounds = 0;
        quorumReadyVotes = 0;
        stateManager.stateChangeRequest(0);

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

        /* Make tx validator */
        if(configValues.getUse().equals("Defi")){
            tv = new DefiTransactionValidator();
        }else if(configValues.getUse().equals("HC")){
            // Room to enable another configValues.getUse() case 
            tv = new HCTransactionValidator();
        }else{
            tv = new DefiTransactionValidator();
        }

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
    public int getMaxPeers(){return configValues.getMaxConnections();}
    public int getMinConnections(){return configValues.getMinConnections();}
    public Address getAddress(){return this.myAddress;}
    public ArrayList<Address> getLocalPeers(){return this.localPeers;}
    public HashMap<String, Transaction> getMempool(){return this.mempool;}
    public LinkedList<Block> getBlockchain(){return blockchain;}
    public LockManager getLockManager(){return lockManager;}


    /**
     * Initializes the blockchain based on the configured use case.
     */
    public void initializeBlockchain(){
        blockchain = new LinkedList<Block>();

        if(configValues.getUse().equals("Defi")){
            addBlock(new DefiBlock(new HashMap<String, Transaction>(), "000000", 0));
        }else if(configValues.getUse().equals("HC")){
            addBlock(new HCBlock(new HashMap<String, Transaction>(), "000000", 0));
        }
    }

    /**
     * Attempts to establish connections with a specified number of nodes from a list of global peers.
     *
     * @param globalPeers List of global peers to connect to.
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
     * Gossips a transaction to the network.
     *
     * @param transaction The transaction to gossip.
     */
    public void gossipTransaction(Transaction transaction){
        synchronized (lockManager.getLock("lock")){
            Messager.sendOneWayMessageToGroup(new Message(Message.Request.ADD_TRANSACTION, transaction), localPeers, myAddress);
        }
    }

    /**
     * Adds a transaction to the mempool after validation and gossiping.
     *
     * @param transaction The transaction to add.
     */
    public void addTransaction(Transaction transaction){
        stateManager.waitForState(0);
        synchronized(lockManager.getLock("memPoolLock")){
            if(Utils.containsTransactionInMap(transaction, mempool)) return;

            if(configValues.getDebugLevel() == 1){System.out.println("Node " + myAddress.getPort() + ": verifyTransaction: " + 

            transaction.getUID() + ", blockchain size: " + blockchain.size());}
            LinkedList<Block> clonedBlockchain = new LinkedList<>();


            clonedBlockchain.addAll(blockchain);
            for(Block block : clonedBlockchain){
                if(block.getTxList().containsKey(getSHAString(transaction.getUID()))){
                    // We have this transaction in a block
                    if(configValues.getDebugLevel() == 1){System.out.println("Node " + myAddress.getPort() + ": trans :" + transaction.getUID() + " found in prev block " + block.getBlockId());}
                    return;
                }
            }

            Object[] validatorObjects = new Object[3];

            if(configValues.getUse().equals("Defi")){            
                validatorObjects[0] = transaction;
                validatorObjects[1] = mempool;

            }else{
                //tv = new HCTransactionValidator(); // To be changed to another configValues.getUse() case in the future
                validatorObjects[0] = transaction;
            }

            if(!tv.validate(validatorObjects)){
                if(configValues.getDebugLevel() == 1){System.out.println("Node " + myAddress.getPort() + "Transaction not valid");}
                return;
            }

            mempool.put(getSHAString(transaction.getUID()), transaction);
            gossipTransaction(transaction);

            if(configValues.getDebugLevel() == 1){System.out.println("Node " + myAddress.getPort() + ": Added transaction. MP:" + mempool.values());}
        }         
    }

    /**
     * Sends a quorum ready signal to a list of quorum peers.
     */
    public void sendQuorumReady(){
        stateManager.stateChangeRequest(1);
        quorumSigs = new ArrayList<>();
        Block currentBlock = blockchain.getLast();
        ArrayList<Address> quorum = deriveQuorum(currentBlock, 0, configValues, globalPeers);

        if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + " sent quorum is ready for q: " + quorum);

        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                try {
                    Thread.sleep(2000);
                    MessagerPack mp = Messager.sendComplexMessage(quorumAddress, new Message(Message.Request.QUORUM_READY), myAddress);
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
                            if(configValues.getDebugLevel() == 1) {
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
                    System.out.println("Node " + myAddress.getPort() + ": sendQuorumReady Received IO Exception from node " + quorumAddress.getPort() + "Exception " 
                    + e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Receives and processes a quorum ready signal from a peer.
     *
     * @param oout Output stream to send a response.
     * @param oin  Input stream to receive the signal.
     */
    public void receiveQuorumReady(ObjectOutputStream oout, ObjectInputStream oin){
        stateManager.waitForState(1);
        synchronized (lockManager.getLock("quorumReadyVotesLock")){

            Block currentBlock = blockchain.getLast();
            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0, configValues, globalPeers);

            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": receiveQuorumReady invoked for " + quorum );

            try {

                if(!inQuorum()){
                    if(configValues.getDebugLevel() == 1) {
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

    /**
     * Sends the hash values of the transactions in the mempool to a quorum of peers,
     * allowing them to request missing transactions.
     */
    public void sendMempoolHashes() {
        synchronized (lockManager.getLock("memPoolLock")){
            stateManager.stateChangeRequest(2);

            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes invoked");
            
            HashSet<String> keys = new HashSet<String>(mempool.keySet());
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);
            
            for (Address quorumAddress : quorum) {
                if (!myAddress.equals(quorumAddress)) {
                    try {
                        MessagerPack mp = Messager.sendComplexMessage(quorumAddress, new Message(Message.Request.RECEIVE_MEMPOOL, keys), myAddress);                        ;
                        Message messageReceived = mp.getMessage();
                        if(messageReceived.getRequest().name().equals("REQUEST_TRANSACTION")){
                            ArrayList<String> hashesRequested = (ArrayList<String>) messageReceived.getMetadata();
                            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes: requested trans: " + hashesRequested);
                            ArrayList<Transaction> transactionsToSend = new ArrayList<>();
                            for(String hash : keys){
                                if(mempool.containsKey(hash)){
                                    transactionsToSend.add(mempool.get(hash));
                                }else{
                                    if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": sendMempoolHashes: requested trans not in mempool. MP: " + mempool);
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

    /**
     * Receives the hash values of transactions from a peer, identifies missing transactions
     * in the local mempool, and requests the missing transactions.
     *
     * @param keys  Set of hash values representing transactions in the mempool.
     * @param oout  Output stream to send a response.
     * @param oin   Input stream to receive the signal.
     */
    public void receiveMempoolHashes(Set<String> keys, ObjectOutputStream oout, ObjectInputStream oin) {
        stateManager.waitForState(2);
        synchronized(lockManager.getLock("memPoolRoundsLock")){
            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked"); 
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);
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
                    if(configValues.getDebugLevel() == 1) {System.out.println("Node " + myAddress.getPort() + ": receiveMempool requesting transactions for: " + keysAbsent); }
                    oout.writeObject(new Message(Message.Request.REQUEST_TRANSACTION, keysAbsent));
                    oout.flush();
                    Message message = (Message) oin.readObject();
                    ArrayList<Transaction> transactionsReturned = (ArrayList<Transaction>) message.getMetadata();
                    
                    for(Transaction transaction : transactionsReturned){
                        mempool.put(getSHAString(transaction.getUID()), transaction);
                        if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": recieved transactions: " + keysAbsent);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

            memPoolRounds++;
            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": receiveMempool invoked: mempoolRounds: " + memPoolRounds); 
            if(memPoolRounds == quorum.size() - 1){
                memPoolRounds = 0;
                constructBlock();
            }
        }
    }

    /**
     * Constructs a block from the transactions in the mempool, validates the transactions,
     * and initiates the process of obtaining signatures from the quorum.
     */
    public void constructBlock(){
        synchronized(lockManager.getLock("memPoolLock")){
            if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": constructBlock invoked");
            stateManager.stateChangeRequest(3);
            
            /* Make sure compiled transactions don't conflict */
            HashMap<String, Transaction> blockTransactions = new HashMap<>();

            
            
            for(String key : mempool.keySet()){
                Transaction transaction = mempool.get(key);
                Object[] validatorObjects = new Object[3];
                if(configValues.getUse().equals("Defi")){
                    validatorObjects[0] = transaction;
                    validatorObjects[1] = blockTransactions;
                }else if(configValues.getUse().equals("HC")){
                    // Validator objects will change according to another configValues.getUse() case
                    validatorObjects[0] = transaction;
                }
                tv.validate(validatorObjects);
                blockTransactions.put(key, transaction);
            }

            try {
                if(configValues.getUse().equals("Defi")){
                    quorumBlock = new DefiBlock(blockTransactions,
                        getBlockHash(blockchain.getLast(), 0),
                                blockchain.size());
                }else{

                    // Room to enable another configValues.getUse() case 
                    quorumBlock = new HCBlock(blockTransactions,
                        getBlockHash(blockchain.getLast(), 0),
                                blockchain.size());
                }

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            sendQuorumSignature();
        }
    }

    /**
     * Signs the hash of the constructed block and sends the signature to the quorum.
     * Invoked after constructing a block.
     */
    public void sendQuorumSignature(){
        String blockHash;
        byte[] sig;

        try {blockHash = getBlockHash(quorumBlock, 0);
            sig = signHash(blockHash, privateKey);
        } catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}

        BlockSignature blockSignature = new BlockSignature(sig, blockHash, myAddress);
        sendOneWayMessageQuorum(new Message(Message.Request.RECEIVE_SIGNATURE, blockSignature));

        if(configValues.getDebugLevel() == 1) {System.out.println("Node " + myAddress.getPort() + ": sendSigOfBlockHash invoked for hash: " + blockHash.substring(0, 4));}
    }

    /**
     * Receives quorum signatures, verifies them, and processes them to reach consensus on the block.
     * Invoked after receiving signatures from a quorum of peers.
     *
     * @param signature The signature of the block hash.
     */
    public void receiveQuorumSignature(BlockSignature signature){
        stateManager.waitForState(3);
        synchronized (lockManager.getLock("sigRoundsLock")){
            if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": 1st part receiveQuorumSignature invoked. state: " + state);}

            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);

            if(!containsAddress(quorum, signature.getAddress())){
                if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": false sig from " + signature.getAddress());
                return;
            }

            if(!inQuorum()){
                if(configValues.getDebugLevel() == 1) System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress); 
                return;
            } 

            quorumSigs.add(signature);
            int blockId = blockchain.size() - 1;

            if(configValues.getDebugLevel() == 1) {
                System.out.println("Node " + myAddress.getPort() + ": receiveQuorumSignature invoked from " + 
                signature.getAddress().toString() + " qSigs: " + quorumSigs + " quorum: " + quorum + " block " + quorumBlock.getBlockId());
            }

            if(quorumSigs.size() == quorum.size() - 1){
                if(!inQuorum()){
                    if(configValues.getDebugLevel() == 1) {
                        System.out.println("Node " + myAddress.getPort() + ": not in quorum? q: " + quorum + " my addr: " + myAddress);
                    }
                    System.out.println("Node " + myAddress.getPort() + ": rQs: not in quorum? q: " + quorum + " my addr: " + myAddress + " block: " + blockId);
                    return;
                }
                tallyQuorumSigs();
            }
        }
    }

    /**
     * Tallies the received quorum signatures, determines the winning hash, and adds the block
     * to the blockchain if consensus is reached.
     * Invoked after receiving all signatures from the quorum.
     */
    public void tallyQuorumSigs(){
        synchronized (lockManager.getLock("blockLock")) {
            resetMempool();

            if (configValues.getDebugLevel() == 1) {System.out.println("Node " + myAddress.getPort() + ": tallyQuorumSigs invoked");}

            stateManager.stateChangeRequest(4);
            ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);

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
            if (configValues.getDebugLevel() == 1) {
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

    /**
     * Clears the transaction mempool after constructing a block and achieving consensus.
     * Invoked to prepare the mempool for new transactions.
     */
    private void resetMempool(){
        synchronized(lockManager.getLock("memPoolLock")){
            mempool = new HashMap<>();
        }
    }

    /**
     * Constructs a BlockSkeleton from the current quorum block and signatures,
     * then sends it to the local group of peers.
     * Invoked after constructing a block and obtaining quorum signatures.
     */
    public void sendSkeleton(){
        synchronized (lockManager.getLock("lock")){

            if(configValues.getDebugLevel() == 1) {
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

            Messager.sendOneWayMessageToGroup(new Message(Message.Request.RECEIVE_SKELETON, skeleton), localPeers, myAddress);
        }
    }

    /**
     * Sends a pre-constructed BlockSkeleton to the local group of peers.
     * Invoked when receiving a BlockSkeleton from another node.
     *
     * @param skeleton The pre-constructed BlockSkeleton to be sent.
     */
    public void sendSkeleton(BlockSkeleton skeleton){
        synchronized (lockManager.getLock("lock")){
            if(configValues.getDebugLevel() == 1) {
                System.out.println("Node " + myAddress.getPort() + ": sendSkeleton(local) invoked: BlockID " + skeleton.getBlockId());
            }

            Messager.sendOneWayMessageToGroup(new Message(Message.Request.RECEIVE_SKELETON, skeleton), localPeers, myAddress);
        }
    }

    /**
     * Receives a BlockSkeleton from another node, validates the signatures, and
     * adds the corresponding block to the blockchain if validations succeed.
     *
     * @param blockSkeleton The received BlockSkeleton.
     */
    public void receiveSkeleton(BlockSkeleton blockSkeleton){
        stateManager.waitForState(0);
        synchronized (lockManager.getLock("blockLock")){
            Block currentBlock = blockchain.getLast();

            if(currentBlock.getBlockId() + 1 != blockSkeleton.getBlockId()){
                //if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) currentblock not synced with skeleton. current id: " + currentBlock.getBlockId() + " new: " + blockSkeleton.getBlockId()); }
                return;
            }else{
                if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": receiveSkeleton(local) invoked. Hash: " + blockSkeleton.getHash());}
            }

            ArrayList<Address> quorum = deriveQuorum(currentBlock, 0, configValues, globalPeers);
            int verifiedSignatures = 0;
            String hash = blockSkeleton.getHash();

            if(blockSkeleton.getSignatures().size() < 1){
                if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": No signatures. blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId() 
                + " quorum: " + quorum ); }
            }

            for(BlockSignature blockSignature : blockSkeleton.getSignatures()){
                Address address = blockSignature.getAddress();
                if(containsAddress(quorum, address)){
                    if(verifySignatureFromRegistry(hash, blockSignature.getSignature(), address)){
                        verifiedSignatures++;
                    }else{
                        if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": Failed to validate signature. blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId()); };
                    }
                }else{
                    if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": blockskeletonID: " + blockSkeleton.getBlockId() + ". CurrentBlockID: " + currentBlock.getBlockId()
                    + " quorum: " + quorum + ". Address: " + address);}
                }
            }

            if(verifiedSignatures != quorum.size() - 1){
                if(configValues.getDebugLevel() == 1) { System.out.println("Node " + myAddress.getPort() + ": sigs not verified for block " + blockSkeleton.getBlockId() + 
                ". Verified sigs: " + verifiedSignatures + ". Needed: " + quorum.size() + " - 1."); }
                return;
            }

            Block newBlock = constructBlockWithSkeleton(blockSkeleton);
            addBlock(newBlock);
            sendSkeleton(blockSkeleton);

        }
    }

    /**
     * Constructs a new block using the transactions specified in the given BlockSkeleton.
     * Invoked after receiving a valid BlockSkeleton from another node.
     *
     * @param skeleton The BlockSkeleton containing transaction details.
     * @return The constructed Block.
     */
    public Block constructBlockWithSkeleton(BlockSkeleton skeleton){
        synchronized (lockManager.getLock("memPoolLock")){
            if(configValues.getDebugLevel() == 1) {
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

            if(configValues.getUse().equals("Defi")){
                try {
                    newBlock = new DefiBlock(blockTransactions,
                            getBlockHash(blockchain.getLast(), 0),
                            blockchain.size());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }else{
                try {
                    newBlock = new HCBlock(blockTransactions,
                            getBlockHash(blockchain.getLast(), 0),
                            blockchain.size());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            

            return newBlock;
        }
    }

    /**
     * Adds a block to the blockchain, updates the merkle root hash, and alerts
     * the wallet if applicable. Also triggers the next quorum readiness check.
     *
     * @param block The block to be added.
     */
    public void addBlock(Block block){
        stateManager.stateChangeRequest(0);
        
        HashMap<String, Transaction> txMap = block.getTxList();
        ArrayList<Transaction> txList = new ArrayList<>();
        for(String hash : txMap.keySet()){
            txList.add(txMap.get(hash));
        }

        MerkleTree mt = new MerkleTree(txList);
        if(mt.getRootNode() != null) block.setMerkleRootHash(mt.getRootNode().getHash());

        blockchain.add(block);
        System.out.println("Node " + myAddress.getPort() + ": " + chainString(blockchain) + " MP: " + mempool.values());

        if(configValues.getUse().equals("Defi")){
            DefiTransactionValidator dtv = (DefiTransactionValidator) tv;
            dtv.alertWallet(txMap, mt, myAddress);
        } else {
            HCTransactionValidator hctv = (HCTransactionValidator) tv;
            hctv.alertWallet(txMap, mt, myAddress);
        }


        ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);

        if(configValues.getDebugLevel() == 1) {
            System.out.println("Node " + myAddress.getPort() + ": Added block " + block.getBlockId() + ". Next quorum: " + quorum);
        }

        if(inQuorum()){
            while(mempool.size() < configValues.getMinimumTransactions()){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendQuorumReady();
        }
    }

    /**
     * Sends a one-way message to all nodes in the current quorum, excluding itself.
     *
     * @param message The message to be sent.
     */
    public void sendOneWayMessageQuorum(Message message){
        ArrayList<Address> quorum = deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);
        for(Address quorumAddress : quorum){
            if(!myAddress.equals(quorumAddress)) {
                Messager.sendOneWayMessage(quorumAddress, message, myAddress);
            }
        }
    }

    /**
     * Checks whether the current node is a member of the quorum.
     *
     * @return True if the node is in the quorum, otherwise false.
     */
    public boolean inQuorum(){
        synchronized (lockManager.getLock("quorumLock")){
            ArrayList<Address> quorum = Utils.deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);
            Boolean quorumMember = false;
            for(Address quorumAddress : quorum){
                if(myAddress.equals(quorumAddress)) {
                    quorumMember = true;
                }
            }
            return quorumMember;
        }
    }

    /**
     * Checks whether the current node is a member of the quorum for a specific block.
     *
     * @param block The block for which to check the quorum.
     * @return True if the node is in the quorum, otherwise false.
     */
    public boolean inQuorum(Block block){
        synchronized (lockManager.getLock("quorumLock")){
            if(block.getBlockId() - 1 != blockchain.getLast().getBlockId()){ // 
                return false;
            }
            ArrayList<Address> quorum = Utils.deriveQuorum(blockchain.getLast(), 0, configValues, globalPeers);
            Boolean quorumMember = false;
            for(Address quorumAddress : quorum){
                if(myAddress.equals(quorumAddress)) {
                    quorumMember = true;
                }
            }
            return quorumMember;
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
                    new ServerConnection(client, node, tv).start();
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

                        /* configValues.getUse() heartbeat to also output the block chain of the node */

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

    private LockManager lockManager;
    private int quorumReadyVotes, memPoolRounds, state;
    private ArrayList<Address> globalPeers, localPeers;
    private HashMap<String, Transaction> mempool;
    private ArrayList<BlockSignature> quorumSigs;
    private LinkedList<Block> blockchain;
    private final Address myAddress;
    private ServerSocket ss;
    private Block quorumBlock;
    private PrivateKey privateKey;
    private Config configValues;
    private StateManager stateManager;
    private TransactionValidator tv;

}