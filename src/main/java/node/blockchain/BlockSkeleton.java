package node.blockchain;

import node.communication.BlockSignature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class BlockSkeleton implements Serializable{
    private final int blockId;
    private String hash;
    private final Set<String> keys;
    private ArrayList<BlockSignature> signatures;

    public BlockSkeleton (int blockId, Set<String> keys, ArrayList<BlockSignature> signatures, String hash){
        this.keys = keys;
        this.blockId = blockId;
        this.signatures = signatures;
        this.hash = hash;
    }

    public ArrayList<BlockSignature> getSignatures() {return signatures;}
    public int getBlockId(){return blockId;}
    public Set<String> getKeys() {return keys;}
    public String getHash(){return hash;}
}

