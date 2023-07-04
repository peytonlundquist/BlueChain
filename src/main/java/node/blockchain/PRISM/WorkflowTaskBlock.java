package node.blockchain.PRISM;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import node.blockchain.Block;
import node.blockchain.Transaction;
import node.blockchain.PRISM.TransactionTypes.ProvenanceRecord;
import node.blockchain.defi.DefiTransaction;

public class WorkflowTaskBlock extends Block {
    
    public String outputData;
    public ArrayList<MinerData> minerData; 

    //List of correct and incorrec miners and their signatures and their time to mine
     public WorkflowTaskBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId, ArrayList<MinerData> minerData) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
        this.minerData = minerData;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            ProvenanceRecord transactionInList = (ProvenanceRecord) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }


   
}
 