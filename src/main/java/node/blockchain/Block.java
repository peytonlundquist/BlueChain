package node.blockchain;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Block implements Serializable {
    protected int blockId;
    protected HashMap<String, Transaction> txList;
    protected String prevBlockHash;

    // public Block(HashMap<String, Transaction> txList, String prevBlockHash, int blockId){
    //     this.txList = txList;
    //     this.prevBlockHash = prevBlockHash;
    //     this.blockId = blockId;
    // }

    public int getBlockId() {
        return blockId;
    }

    public HashMap<String, Transaction> getTxList() {
        return txList;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }
}
