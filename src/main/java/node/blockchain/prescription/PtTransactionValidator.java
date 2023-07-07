package node.blockchain.prescription;

import node.blockchain.TransactionValidator;

public class PtTransactionValidator extends TransactionValidator {


    @Override
    public boolean validate(Object[] objects) {
        PtTransaction transaction = (PtTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Prescription") 
        || transaction.getEvent().getAction().name().equals("FillScript") 
        || (transaction.getEvent().getAction().name().equals("Algorithm"))){
            return true;

        } else {
            return false;
        }
      
    }
    
}
