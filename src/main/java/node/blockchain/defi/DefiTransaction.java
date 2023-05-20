package node.blockchain.defi;

import java.io.Serializable;

import node.blockchain.Transaction;
import node.communication.utils.*;

public class DefiTransaction extends Transaction {

    protected String to; //  Public key of reciever
    protected String from; // Public key of sender
    protected int amount;
    
    public DefiTransaction(String to, String from, int amount, String timestamp){
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.timestamp = timestamp;
        UID = Hashing.getSHAString(to + from + amount + timestamp);
    }

    public String getTo(){
        return to;
    }

    public String getFrom(){
        return from;
    }

    public int getAmount(){
        return amount;
    }

    @Override
    public String toString() {
        if(to.length() > 5){
            return to.substring(0, 4) + from.substring(0, 4) + amount;
        }
        return to.substring(0, 1) + from.substring(0, 1) + amount;
    }

}
