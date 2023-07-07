package node.blockchain.healthcare;

import node.blockchain.Transaction;

public class HCTransaction extends Transaction {

    private Event event;

    public HCTransaction(Event event){
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