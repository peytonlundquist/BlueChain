package node.blockchain;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String timestamp;
    private String to;
    private String from;
    private int amount;

    public Transaction(String to, String from, int amount, String timestamp){
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
        this.amount = amount;
    }

    public String getData(){
        return timestamp + to + from + amount;
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
        if(transaction.getData().equals(this.getData())){
            return true;
        }
        return false;
    }

    public String toString(){
        return getData();
    }
}
