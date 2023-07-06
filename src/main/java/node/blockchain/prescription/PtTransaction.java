package node.blockchain.prescription;

import java.util.ArrayList;

import node.blockchain.Transaction;
import node.communication.ValidationResultSignature;

public class PtTransaction extends Transaction {

    private Event event;
    
    private ArrayList<ValidationResultSignature> validationResultSignatures;

    public ArrayList<ValidationResultSignature> getValidationResultSignatures() {
        return validationResultSignatures;
    }

    public void setValidationResultSignatures(ArrayList<ValidationResultSignature> validationResultSignatures) {
        this.validationResultSignatures = validationResultSignatures;
    }

    public PtTransaction(Event event){
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return null;
    }    
}