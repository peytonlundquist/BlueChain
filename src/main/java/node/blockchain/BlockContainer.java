package node.blockchain;

import java.io.Serializable;
import java.util.ArrayList;

public class BlockContainer implements Serializable {
    private final Block block;

    private ArrayList<String> signatures = new ArrayList<>();

    public BlockContainer (Block block){
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public ArrayList<String> getSignatures() {
        return signatures;
    }

    public void addSignature(String signature) {
        signatures.add(signature);
    }
}
