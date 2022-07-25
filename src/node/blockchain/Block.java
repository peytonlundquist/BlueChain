package node.blockchain;

import java.util.ArrayList;

public class Block {
    private ArrayList<Transaction> txList;
    private String prevBlockHash;
    private String txListHash;

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
