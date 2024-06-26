package blockchain.usecases.defi;

import blockchain.Transaction;
import utils.*;

/**
 * A specific transaction made for the Defi use case
 */
public class DefiTransaction extends Transaction {

    protected String to; //  Public key string of reciever
    protected String from; // Public key string of sender
    protected int amount; // Amount to being transferred

    protected String note; // Note attached to the transaction
    
    public DefiTransaction(String to, String from, int amount, String timestamp, String note){
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = note;
        UID = Hashing.getSHAString(to + from + amount + timestamp); // Hashing above fields to generate a unique timestamp
    }

    public DefiTransaction(String to, String from, int amount, String timestamp){
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = "";
        UID = Hashing.getSHAString(to + from + amount + timestamp); // Hashing above fields to generate a unique timestamp
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

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        if(to.length() > 5){
            return to.substring(to.length() - 2, to.length() - 1) + from.substring(from.length() - 2, from.length() - 1) + amount + note;
        }
        return to.substring(0, 1) + from.substring(0, 1) + amount + note;
    }

}
