package node.blockchain;

import java.io.Serializable;

public class Transaction implements Serializable {
    private final String data;

    public Transaction(String data){
        this.data = data;
    }

    public String getData(){
        return data;
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
