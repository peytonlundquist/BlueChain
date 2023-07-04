package node.blockchain.PRISM;
import java.util.HashMap;
import java.util.HashSet;
import node.blockchain.Block;
import node.blockchain.Transaction;
import node.blockchain.PRISM.TransactionTypes.Project;
import node.blockchain.defi.DefiTransaction;

public class WorkflowInceptionBlock extends Block {

    
     public WorkflowInceptionBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            Project transactionInList = (Project) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
    
   
}
