package node.blockchain.prescription;

import node.blockchain.TransactionValidator;
import node.blockchain.prescription.Events.FillScript;

public class ptTransactionValidator extends TransactionValidator {


    //simple fix atm is to just make it valid if it is one of the cases.
    @Override
    public boolean validate(Object[] objects) {
        ptTransaction transaction = (ptTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Prescription")){
            

        }else if (transaction.getEvent().getAction().name().equals("FillScript")){

        }

        //HCTransaction transaction = new HCTransaction(event);        
        return true;
    }
    
}
