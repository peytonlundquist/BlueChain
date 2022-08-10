package node.blockchain;

import java.util.ArrayList;

public class Block {
    private ArrayList<Transaction> txList;

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    private String prevBlockHash;
    private String txListHash;

    public int getBlockId() {
        return blockId;
    }

    private int blockId;

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
