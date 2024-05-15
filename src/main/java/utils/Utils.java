package utils;

import static utils.Hashing.getBlockHash;

import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import blockchain.Block;
import blockchain.Transaction;
import node.Node;

/**
 * The Utils class provides utility methods for various common operations in the blockchain application.
 */
public class Utils {

    /**
     * Deep clones a HashMap containing Transaction objects.
     *
     * @param givenHashMap The HashMap to be cloned.
     * @return A new HashMap with the same entries as the givenHashMap.
     */
    public static HashMap<String, Transaction> deepCloneHashmap(HashMap<String, Transaction> givenHashMap){
        HashMap<String, Transaction> newHashMap = new HashMap<>();
        for(Map.Entry<String, Transaction> entry : givenHashMap.entrySet()){
            newHashMap.put(entry.getKey(), entry.getValue());
        }
        return newHashMap;
    }

    /**
     * Deep clones an ArrayList of Block objects.
     *
     * @param blockchain The ArrayList to be cloned.
     * @param blockLock  The lock object for synchronizing access to the blockchain.
     * @return A new ArrayList with the same entries as the blockchain.
     */
    public static ArrayList<Block> deepCloneBlockChain(ArrayList<Block> blockchain, Object blockLock){
        synchronized(blockLock){
            ArrayList<Block> newBlockchain = new ArrayList<>();
            for(Block block : blockchain){
                newBlockchain.add(block);
            }
            return newBlockchain;
        }
    }

    /**
     * Generates a string representation of the last six blocks in the blockchain.
     *
     * @param blockChain The linked list of blocks representing the blockchain.
     * @return A string representation of the last six blocks in the blockchain.
     */
    public static String chainString(LinkedList<Block> blockChain){
        String hash = null;
        String chainString = "Chain: [";


        if(blockChain.size() > 6){
            for(int i = blockChain.size() - 6; i < blockChain.size(); i++){
                try {
                    hash = getBlockHash(blockChain.get(i), 0).substring(0, 4);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if(blockChain.get(i).getTxList().size() > 0){
                    hash = hash.concat(" tx{" + blockChain.get(i).getTxList().values().toString() + "}");
                }
                chainString = chainString.concat(blockChain.get(i).getBlockId() + " " + hash + ", ");
            }
        }else{
            for(Block block : blockChain){
                try {
                    hash = getBlockHash(block, 0).substring(0, 4);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if(block.getTxList().size() > 0){
                    hash = hash.concat(" tx{" + block.getTxList().values().toString() + "}");
                }
                chainString = chainString.concat(block.getBlockId() + " " + hash + ", ");
            }
        }    
        return chainString.concat("]");
    }

    /**
     * Checks if the provided address is present in the list.
     *
     * @param list    The list of addresses to be checked.
     * @param address The address to be checked for presence.
     * @return True if the address is present, otherwise false.
     */
    public static boolean containsAddress(ArrayList<Address> list, Address address){
        for (Address existingAddress : list) {
            if (existingAddress.equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the provided transaction is present in the given mempool.
     *
     * @param transaction The transaction to be checked for presence.
     * @param mempool     The mempool to search for the transaction.
     * @return True if the transaction is present, otherwise false.
     */
    public static boolean containsTransactionInMap(Transaction transaction, HashMap<String, Transaction> mempool){
        for(Map.Entry<String, Transaction> entry : mempool.entrySet()){
            if (entry.getValue().equals(transaction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a connection is eligible and, if specified, establishes the connection.
     *
     * @param node                The node initiating the connection.
     * @param address             The address to be checked for eligibility.
     * @param connectIfEligible  If true, establishes the connection if eligible.
     * @return True if the connection is eligible, otherwise false.
     */
    public static boolean eligibleConnection(Node node ,Address address, boolean connectIfEligible){
        synchronized(node.getLockManager().getLock("lock")) {
            if (node.getLocalPeers().size() < node.getMaxPeers() - 1 && (!address.equals(node.getAddress()) && !Utils.containsAddress(node.getLocalPeers(), address))) {
                if(connectIfEligible){
                    establishConnection(node ,address);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Adds a connection to the dynamic list of peers.
     *
     * @param node    The node initiating the connection.
     * @param address The address to be added to the list of peers.
     */
    public static void establishConnection(Node node, Address address){
        synchronized (node.getLockManager().getLock("lock")){
            node.getLocalPeers().add(address);
        }
    }

    /**
     * Removes an address from the dynamic list of peers.
     *
     * @param node    The node initiating the removal.
     * @param address The address to be removed.
     * @return The removed address, or null if not found.
     */
    public Address removeAddress(Node node, Address address){
        synchronized (node.getLockManager().getLock("lock")){
            for (Address existingAddress : node.getLocalPeers()) {
                if (existingAddress.equals(address)) {
                    node.getLocalPeers().remove(address);
                    return address;
                }
            }
            return null;
        }
    }

    /**
     * Derives a quorum of addresses from the global peers based on a block's hash and nonce.
     *
     * @param block        The block for which to derive the quorum.
     * @param nonce        The nonce value used in the mining process.
     * @param configValues The configuration values for the blockchain.
     * @param globalPeers  The list of global peers in the network.
     * @return The derived quorum of addresses.
     */
    public static ArrayList<Address> deriveQuorum(Block block, int nonce, Config configValues, ArrayList<Address> globalPeers){
        String blockHash;
        if(block != null && block.getPrevBlockHash() != null){
            try {
                ArrayList<Address> quorum = new ArrayList<>(); // New list for returning a quorum, list of addr
                blockHash = Hashing.getBlockHash(block, nonce); // gets the hash of the block
                BigInteger bigInt = new BigInteger(blockHash, 16); // Converts the hex hash in to a big Int
                bigInt = bigInt.mod(BigInteger.valueOf(configValues.getNumNodes())); // we mod the big int I guess
                int seed = bigInt.intValue(); // This makes our seed
                Random random = new Random(seed); // Makes our random in theory the same across all healthy nodes
                int quorumNodeIndex; // The index from our global peers from which we select nodes to participate in next quorum
                Address quorumNode; // The address of thenode from the quorumNode Index to go in to the quorum

                // Loops through the list of global peers and adds them to the quorum until the quorum size is reached.
                while(quorum.size() < configValues.getQuorumSize()){
                    quorumNodeIndex = random.nextInt(configValues.getNumNodes()); // may be wrong but should still work
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
}
