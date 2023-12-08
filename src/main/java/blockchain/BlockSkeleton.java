package blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class BlockSkeleton implements Serializable{
    private final int blockId;
    private String hash;
    private final ArrayList<String> keys;
    private ArrayList<BlockSignature> signatures;

    public BlockSkeleton (int blockId, ArrayList<String> keys, ArrayList<BlockSignature> signatures, String hash){
        this.keys = keys;
        this.blockId = blockId;
        this.signatures = signatures;
        this.hash = hash;
    }

    public ArrayList<BlockSignature> getSignatures() {return signatures;}
    public int getBlockId(){return blockId;}
    public ArrayList<String> getKeys() {return keys;}
    public String getHash(){return hash;}
}

