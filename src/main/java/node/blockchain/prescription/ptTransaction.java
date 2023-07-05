package node.blockchain.prescription;

import node.blockchain.Transaction;

public class ptTransaction extends Transaction {

    private Event event;

    public ptTransaction(Event event){
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