package utils;

import static utils.Hashing.getBlockHash;

import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import blockchain.Block;
import blockchain.Transaction;
import node.Node;

public class Utils {

    public static HashMap<String, Transaction> deepCloneHashmap(HashMap<String, Transaction> givenHashMap){
        HashMap<String, Transaction> newHashMap = new HashMap<>();
        for(Map.Entry<String, Transaction> entry : givenHashMap.entrySet()){
            newHashMap.put(entry.getKey(), entry.getValue());
        }
        return newHashMap;
    }


    public static ArrayList<Block> deepCloneBlockChain(ArrayList<Block> blockchain, Object blockLock){
        synchronized(blockLock){
            ArrayList<Block> newBlockchain = new ArrayList<>();
            for(Block block : blockchain){
                newBlockchain.add(block);
            }
            return newBlockchain;
        }
    }

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
     * Returns true if the provided address is in the list, otherwise false
     * @param list
     * @param address
     * @return
     */
    public static boolean containsAddress(ArrayList<Address> list, Address address){
        for (Address existingAddress : list) {
            if (existingAddress.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsTransactionInMap(Transaction transaction, HashMap<String, Transaction> mempool){
        for(Map.Entry<String, Transaction> entry : mempool.entrySet()){
            if (entry.getValue().equals(transaction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a connection is eligible
     * @param address Address to verify
     * @param connectIfEligible Connect to address if it is eligible
     * @return True if eligible, otherwise false
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
     * Add a connection to our dynamic list of peers to speak with
     * @param address
     */
    public static void establishConnection(Node node, Address address){
        synchronized (node.getLockManager().getLock("lock")){
            node.getLocalPeers().add(address);
        }
    }

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
