import java.util.ArrayList;

public class Block {
    private ArrayList<Transaction> txList;
    private String prevBlockHash;
    private String txListHash;

    public Block(ArrayList<Transaction> txList, String prevBlockHash){
        this.txList = txList;
        this.prevBlockHash = prevBlockHash;
        txListHash = calcHash(txList);
    }

    private String calcHash(ArrayList<Transaction> txList){
        return "";
    }

}
