package node.blockchain.PRISM.RecordTypes;

public class Record {
    public enum RecordType {
        Project,
        ProvenanceRecord,
        InvalidTask,
        InvalidData
    }

    private RecordType recordType;  
    private String workflowID;

    public RecordType getRecordType() {
        return recordType;
    }
    Record(RecordType type, String workflowID) {
        this.recordType = type;
        this.workflowID = workflowID;
    }
}
