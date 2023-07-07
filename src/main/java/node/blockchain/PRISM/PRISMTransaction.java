package node.blockchain.PRISM;

import node.blockchain.Transaction;
import node.blockchain.PRISM.RecordTypes.Record;

public class PRISMTransaction extends Transaction {

    private Record record;
    private String timestamp;

    public PRISMTransaction(Record record, String timestamp) {
        this.record = record;
        this.timestamp = timestamp;
    }

    public Record getRecord() {
        return record;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

}
