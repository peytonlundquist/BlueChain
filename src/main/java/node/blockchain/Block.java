package node.blockchain;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Block implements Serializable {
    private final HashMap<String, Transaction> txList;

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    private final String prevBlockHash;

    public int getBlockId() {
        return blockId;
    }

    public HashMap<String, Transaction> getTxList() {
        return txList;
    }


    private final int blockId;

    public Block(HashMap<String, Transaction> txList, String prevBlockHash, int blockId){
        this.txList = txList;
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
    }
}
