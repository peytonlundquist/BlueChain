package node.blockchain;

import java.util.ArrayList;

public class Block {
    private final ArrayList<Transaction> txList;

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    private final String prevBlockHash;
    private final String txListHash;

    public int getBlockId() {
        return blockId;
    }

    private final int blockId;

    public Block(ArrayList<Transaction> txList, String prevBlockHash, int blockId){
        this.txList = txList;
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
        txListHash = calcHash(txList);
    }

    private String calcHash(ArrayList<Transaction> txList){
        return "";
    }

}
