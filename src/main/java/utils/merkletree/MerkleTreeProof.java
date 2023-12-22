package utils.merkletree;

import java.io.Serializable;
import java.util.ArrayList;

import blockchain.Transaction;
import utils.Hashing;

/**
 * Represents a Merkle Tree proof for a specific transaction.
 */
public class MerkleTreeProof implements Serializable{
    private ArrayList<String> hashes;
    private String rootHash;
    private Transaction transaction;

    /**
     * Constructs a MerkleTreeProof object with the specified parameters.
     *
     * @param hashes      The list of hashes in the proof.
     * @param transaction The transaction for which the proof is generated.
     * @param rootHash    The root hash of the Merkle Tree.
     */
    public MerkleTreeProof(ArrayList<String> hashes, Transaction transaction, String rootHash) {
        this.hashes = hashes;
        this.rootHash = rootHash;
        this.transaction = transaction;
    }

    /**
     * Gets the list of hashes in the Merkle Tree proof.
     *
     * @return The list of hashes.
     */
    public ArrayList<String> getHashes() {
        return hashes;
    }

    /**
     * Gets the root hash of the Merkle Tree.
     *
     * @return The root hash.
     */
    public String getRootHash() {
        return rootHash;
    }

    /**
     * Gets the transaction for which the proof is generated.
     *
     * @return The transaction.
     */
    public Transaction getTransaction(){
        return transaction;
    }

    /**
     * Confirms the membership of the transaction in the Merkle Tree.
     *
     * @return True if the transaction is a member, false otherwise.
     */
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