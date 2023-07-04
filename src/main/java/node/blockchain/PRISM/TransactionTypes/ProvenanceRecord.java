package node.blockchain.PRISM.TransactionTypes;

import node.blockchain.Transaction;
import node.communication.utils.Hashing;

public class ProvenanceRecord extends Transaction {

    /*
     * A provenance record will be contained in a workflowTaskBlock
     * 
     * 
     */
    protected String inputData; //  Public key string of reciever
    protected String task; // Public key string of sender
    
    public ProvenanceRecord(String inputData, String task, String timestamp){
        this.inputData = inputData;
        this.task = task;
        this.timestamp = timestamp;
        UID = Hashing.getSHAString(inputData + task + timestamp); // Hashing above fields to generate a unique timestamp
    }

    



    @Override

    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }
    
}
