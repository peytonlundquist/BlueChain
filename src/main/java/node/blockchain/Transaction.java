package node.blockchain;

import java.io.Serializable;

public abstract class Transaction implements Serializable{
    protected String timestamp;
    protected String UID;
    protected byte[] sigUID;

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

    public boolean equals(Transaction transaction){
        if(transaction.getUID().equals(this.getUID())){
            return true;
        }
        return false;
    }

    abstract public String toString();
}
