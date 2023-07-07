package node.blockchain.prescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import node.blockchain.Block;
import node.blockchain.Transaction;
import node.communication.ValidationResultSignature;

/**
 * A block implemented for the Defi use case
 */
public class PtBlock extends Block{

    HashMap<String, ArrayList<ValidationResultSignature>> answerSigs;


    public HashMap<String, ArrayList<ValidationResultSignature>> getAnswerSigs() {
        return answerSigs;
    }


    public PtBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId, HashMap<String, ArrayList<ValidationResultSignature>> answerSigs) {

        /* Setting variables inherited from Block class */
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
        this.answerSigs = answerSigs;

        /* Converting the transaction from Block to DefiTransactions*/
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            PtTransaction transactionInList = (PtTransaction) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
