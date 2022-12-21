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
        HashMap newHashMap = new HashMap<>();
        for(Map.Entry<String, Transaction> entry : givenHashMap.entrySet()){
            newHashMap.put(entry.getKey(), entry.getValue());
        }
        return newHashMap;
    }

    public static String chainString(ArrayList<Block> blockChain){
        String hash;
        String chainString = "Chain: [";
        for(Block block : blockChain){
            hash = block.getPrevBlockHash().substring(0, 4);
            if(block.getTxList().size() > 0){
                hash = hash.concat(" tx{" + block.getTxList().keySet().toString().substring(0, 4) + "}");
            }
            chainString = chainString.concat(block.getBlockId() + " " + hash + ", ");
        }
        return chainString.concat("]");
    }
}
