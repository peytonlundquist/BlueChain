package node.blockchain;

import java.io.Serializable;
import java.util.ArrayList;

import node.communication.utils.Hashing;
import node.defi.Transaction;

public class MerkleTreeProof implements Serializable{
    private ArrayList<String> hashes;
    private String rootHash;
    private Transaction transaction;

    public MerkleTreeProof(ArrayList<String> hashes, Transaction transaction, String rootHash) {
        this.hashes = hashes;
        this.rootHash = rootHash;
        this.transaction = transaction;
    }

    public ArrayList<String> getHashes() {
        return hashes;
    }

    public String getRootHash() {
        return rootHash;
    }

    public Transaction getTransaction(){
        return transaction;
    }

    public boolean confirmMembership(){
        String myHash = Hashing.getSHAString(transaction.getUID());
        String hash1 = hashes.get(0).substring(1, hashes.get(0).length());  // Remove padding
        String hash2 = hashes.get(1).substring(1, hashes.get(1).length());  // Remove padding

        if(!hash1.equals(myHash) && !hash2.equals(myHash)){
            System.out.println("My TX hash not given in proof");
        }

        String growingHash = Hashing.getSHAString(hash2 + hash1);

        for(int i = 2; i < hashes.size(); i++){

            String rightLeftPadding = hashes.get(i).substring(0, 1); // get padding to determine left or right hash
            String hash = hashes.get(i).substring(1, hashes.get(i).length()); // Remove padding

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