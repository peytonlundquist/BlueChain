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

//    @Override
//    public int compareTo(Object o) {
//        Transaction transaction = (Transaction) o;
//        try {
//            String hashedTransaction = getSHAString(transaction.getData());
//            String thisHashedTransaction = getSHAString(this.getData());
//
//
//        } catch (NoSuchAlgorithmException e) {
//            return 0;
//        }
//        return 0;
//    }
}
