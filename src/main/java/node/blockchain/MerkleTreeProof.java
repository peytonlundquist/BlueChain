package node.blockchain;

import java.io.Serializable;
import java.util.ArrayList;

import node.communication.utils.Hashing;
import node.defi.Transaction;

public class MerkleTreeProof implements Serializable{
    private ArrayList<String> hashes;
    private String rootHash;

    public MerkleTreeProof(ArrayList<String> hashes, String rootHash) {
        this.hashes = hashes;
        this.rootHash = rootHash;
    }

    public ArrayList<String> getHashes() {
        return hashes;
    }

    public String getRootHash() {
        return rootHash;
    }

    public static boolean confirmMembership(ArrayList<String> hashList, Transaction myTransaction, String rootHash){
        String myHash = Hashing.getSHAString(myTransaction.getUID());
        String hash1 = hashList.get(0).substring(1, hashList.get(0).length());  // Remove padding
        String hash2 = hashList.get(1).substring(1, hashList.get(1).length());  // Remove padding

        if(!hash1.equals(myHash) && !hash2.equals(myHash)){
            System.out.println("My TX hash not given in proof");
        }

        String growingHash = Hashing.getSHAString(hash2 + hash1);

        for(int i = 2; i < hashList.size(); i++){

            String rightLeftPadding = hashList.get(i).substring(0, 1); // get padding to determine left or right hash
            String hash = hashList.get(i).substring(1, hashList.get(i).length()); // Remove padding

            if(rightLeftPadding.equals("0")){
                // This is left hash
                growingHash = Hashing.getSHAString(growingHash + hash);

            }else{
                // This is a right hash
                growingHash = Hashing.getSHAString(hash + growingHash);

            }
        }
        
        if(growingHash.equals(rootHash)){
            return true;
        }else{
            return false;
        }
    }
}