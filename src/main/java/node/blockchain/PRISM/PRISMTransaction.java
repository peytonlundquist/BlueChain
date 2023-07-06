package node.blockchain.PRISM;

import node.blockchain.Transaction;
import node.blockchain.PRISM.RecordTypes.Record;

public class PRISMTransaction extends Transaction {

    private Record record;

    public PRISMTransaction(Record record) {
        this.record = record;
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
