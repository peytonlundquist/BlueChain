package node.communication.utils;

import node.blockchain.Transaction;
import node.communication.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Utils {

    public static boolean containsAddress(ArrayList<Address> list, Address address){
        for(Address existingAddress : list){
            if(existingAddress.equals(address)){
                return true;
            }
        }
        return false;
    }

    public static HashMap<String, Transaction> deepCloneHashmap(HashMap<String, Transaction> givenHashMap){
        HashMap newHashMap = new HashMap<>();
        for(Map.Entry<String, Transaction> entry : givenHashMap.entrySet()){
            newHashMap.put(entry.getKey(), entry.getValue());
        }
        return newHashMap;
    }
}
