package node.blockchain.defi;

import java.util.HashMap;
import java.util.HashSet;

import node.blockchain.Block;
import node.blockchain.Transaction;

public class DefiBlock extends Block{

    public DefiBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId) {
        HashSet<String> keys = new HashSet<>(txList.keySet());
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
        // For each hash of a transaction
        for(String key : keys){
            DefiTransaction transactionInList = (DefiTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
