package node.blockchain.prescription;

import node.blockchain.TransactionValidator;
import node.blockchain.prescription.Events.FillScript;
import node.blockchain.prescription.Events.Algorithm;
import node.blockchain.prescription.Events.Prescription;
import node.blockchain.prescription.ValidationResult;

public class ptTransactionValidator extends TransactionValidator {



   

    @Override
    public boolean validate(Object[] objects) {
        ptTransaction transaction = (ptTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Prescription") 
        || transaction.getEvent().getAction().name().equals("FillScript") 
        || (transaction.getEvent().getAction().name().equals("Algorithm"))){
            return true;

        } else {
            return false;
        }
      
    }
    
}
