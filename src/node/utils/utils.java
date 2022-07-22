package node.utils;

import node.communication.Address;

import java.util.ArrayList;

public class utils {

    public static boolean containsAddress(ArrayList<Address> list, Address address){
        for(Address existingAddress : list){
            if(existingAddress.equals(address)){
                return true;
            }
        }
        return false;
    }
}
