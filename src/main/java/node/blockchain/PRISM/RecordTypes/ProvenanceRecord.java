package node.blockchain.PRISM.RecordTypes;

import java.util.List;

import node.blockchain.Transaction;
import node.blockchain.PRISM.MinerData;
import node.blockchain.PRISM.PRISMTransaction;
import node.blockchain.PRISM.RecordTypes.Record.RecordType;
import node.communication.utils.Hashing;

public class ProvenanceRecord extends Record {

    /*
     * A provenance record will be contained in a workflowTaskBlock
     */
    protected String inputData; //  Public key string of reciever
    protected String task; // Public key string of sender
    protected List<MinerData> minerData; 
    public ProvenanceRecord(String inputData, String task, String workflowID, List<MinerData> minerData) {
        super(RecordType.ProvenanceRecord, workflowID);
        this.inputData = inputData;
        this.task = task;
        this.minerData = minerData; 
    }

    
}
