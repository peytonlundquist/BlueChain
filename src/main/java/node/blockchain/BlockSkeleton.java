package node.blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class BlockSkeleton implements Serializable{
    private final Set<String> keys;
    private final int blockId;

    private ArrayList<String> signatures;

    public BlockSkeleton (int blockId, Set<String> keys , ArrayList<String> signatures){
        this.keys = keys;
        this.blockId = blockId;
        this.signatures = signatures;
    }

    public ArrayList<String> getSignatures() {
        return signatures;
    }

    public int getBlockId(){return blockId;}
}
