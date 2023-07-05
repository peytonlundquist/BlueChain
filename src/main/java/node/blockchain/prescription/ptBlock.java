package node.blockchain.prescription;

import java.util.HashMap;
import java.util.HashSet;
import node.blockchain.Block;
import node.blockchain.Transaction;

/**
 * A block implemented for the Defi use case
 */
public class ptBlock extends Block{

    public ptBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            ptTransaction transactionInList = (ptTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
