package node.blockchain.PRISM;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import node.blockchain.Block;
import node.blockchain.Transaction;
import node.blockchain.PRISM.RecordTypes.ProvenanceRecord;
import node.blockchain.defi.DefiTransaction;

public class WorkflowTaskBlock extends Block {
    

    List<MinerData> minerData;

    //List of correct and incorrec miners and their signatures and their time to mine
     public WorkflowTaskBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId,  List<MinerData> minerData ) {
        super(setBlockType(BlockType.wTask));
        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
        this.minerData = minerData; //This will work if we only have one tx per block. Otherwise, this may need to go into PRISMTransaction class.

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            PRISMTransaction transactionInList = (PRISMTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }


   
}
 