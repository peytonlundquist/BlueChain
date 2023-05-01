package node.blockchain;

import java.io.Serializable;
import java.util.ArrayList;

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
}