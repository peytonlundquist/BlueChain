package node.communication.utils;

import node.blockchain.Block;
import node.blockchain.Transaction;
import node.communication.Address;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static String chainString(ArrayList<Block> blockChain){
        String hash = null;
        String chainString = "Chain: [";
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
        return chainString.concat("]");
    }
}
