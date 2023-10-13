package blockchain.usecases.defi;

import java.util.HashMap;
import java.util.HashSet;

import blockchain.Block;
import blockchain.Transaction;

/**
 * A block implemented for the Defi use case
 */
public class DefiBlock extends Block{

    public DefiBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            DefiTransaction transactionInList = (DefiTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
