package node.blockchain;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Block implements Serializable {

    public enum BlockType {
        wTask,
        wProject
    }
    protected int blockId;
    protected HashMap<String, Transaction> txList;
    protected String prevBlockHash;
    protected String merkleRootHash;
    protected BlockType blockType; 

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public int getBlockId() {
        return blockId;
    }

    public HashMap<String, Transaction> getTxList() {
        return txList;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public void setMerkleRootHash(String rootHash){
        this.merkleRootHash = rootHash;
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }
}
