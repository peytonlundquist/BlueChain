package node.blockchain.PRISM;

import node.blockchain.TransactionValidator;
import node.blockchain.PRISM.RecordTypes.Record.RecordType;

public class PRISMTransactionValidator extends TransactionValidator {

    @Override
    public boolean validate(Object[] objects) {
        // TODO Auto-generated method stub
       //Here we can check what the RecordType is and validate it this way. 
        PRISMTransaction transaction = (PRISMTransaction) objects[0];
        if(transaction.getRecord().getRecordType().equals(RecordType.ProvenanceRecord)) { 
            return true;//Eventually, we want to check if a node has enough of a reputation to propose a transaction.
        }else if(transaction.getRecord().getRecordType().equals(RecordType.Project)) {
            return true; //Same is true here
        }
        
        return false;

    }
    
}
