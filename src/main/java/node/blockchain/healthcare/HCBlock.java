package node.blockchain.healthcare;

import java.util.HashMap;
import java.util.HashSet;
import node.blockchain.Block;
import node.blockchain.Transaction;

/**
 * A block implemented for the Defi use case
 */
public class HCBlock extends Block{

    public HCBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            HCTransaction transactionInList = (HCTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
