package blockchain.usecases.healthcare;

import blockchain.TransactionValidator;
import blockchain.usecases.healthcare.Events.Appointment;

public class HCTransactionValidator extends TransactionValidator {

    @Override
    public boolean validate(Object[] objects) {
        HCTransaction transaction = (HCTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Appointment")){
            // Appointment appointment = (Appointment) transaction.getEvent();
            // if(appointment.getTime())

        }else if (transaction.getEvent().getAction().name().equals("Prescription")){

        }

        //HCTransaction transaction = new HCTransaction(event);        
        return true;
    }
    
}
