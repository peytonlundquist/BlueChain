package node.defi;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import node.communication.utils.*;

public class Transaction implements Serializable {
    private String timestamp;
    private String to; //  Public key of reciever
    private String from; // Public key of sender
    private int amount;
    private String UID;
    private byte[] sigUID;

    public Transaction(String to, String from, int amount, String timestamp){
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.timestamp = timestamp;

        UID = Hashing.getSHAString(to + from + amount + timestamp);
    }

    public void setSigUID(byte[] sig){
        sigUID = sig;
    }

    public byte[] getSigUID(){
        return sigUID;
    }

    public String getUID(){
        return timestamp + toString();
    }

    public String getTimestamp(){
        return timestamp;
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

    public boolean equals(Transaction transaction){
        if(transaction.getUID().equals(this.getUID())){
            return true;
        }
        return false;
    }

    public String toString(){
        if(to.length() > 5){
            return to.substring(0, 4) + from.substring(0, 4) + amount;
        }
        return to.substring(0, 1) + from.substring(0, 1) + amount;
    }
}
