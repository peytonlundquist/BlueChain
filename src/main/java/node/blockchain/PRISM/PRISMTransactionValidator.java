package node.blockchain.PRISM;

import java.util.HashMap;
import java.util.Map;

import node.blockchain.TransactionValidator;
import node.blockchain.PRISM.RecordTypes.Project;
import node.blockchain.PRISM.RecordTypes.Record.RecordType;
import node.communication.Address;

public class PRISMTransactionValidator extends TransactionValidator {

    public Map<Address, Float> calculateReputations(float alpha, float beta, float gamma) {

        Map<Address, Float> reputations = new HashMap<Address, Float>();
        /* Pseudocode:
         * For each node in the nodeRegistry:
         *  For each block in the ledger:
         *    If the node signature is in the PrismTransaction:
         *       blocksParticipated ++;
         *       accuracy += MinerData.correctness
         *       if(accuracy == 1)
         *           accuracyCount ++
         *       time += MinerData.time - minimumCorrectTime
         *   Rep = (alpha * accuracy + beta * time + gamma * (accuracyCount / blocksParticipated)) / blocksParticipated
         * reputations.put(address, Rep)
         */

         return null;

    }

    public Float calculateReputation(Address address, float alpha, float beta, float gamma) {
         return null;
    }

    @Override
    public boolean validate(Object[] objects) {
        // TODO Auto-generated method stub
       //Here we can check what the RecordType is and validate it this way. 
        PRISMTransaction transaction = (PRISMTransaction) objects[0];
        if(transaction.getRecord().getRecordType().equals(RecordType.ProvenanceRecord)) { 
            return true;//Eventually, we want to check if a node has enough of a reputation to propose a transaction.
        }else if(transaction.getRecord().getRecordType().equals(RecordType.Project)) {
            Project project = (Project) transaction.getRecord();

            if(calculateReputation(project.getAuthors()[0], 0, 0, 0) > 0.75){
                return true; //Same is true here
            }

            return false;

        }
        
        return false;

    }
    
}
