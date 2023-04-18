package node.communication.utils;

import node.blockchain.Block;
import node.communication.Address;
import node.defi.Transaction;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static node.communication.utils.Hashing.getBlockHash;

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
}
