package node.blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TransactionValidator{
    ArrayList<Block> blockchain;

    public TransactionValidator(ArrayList<Block> blockchain){
        this.blockchain = blockchain;
    }

    public boolean validate(Transaction transaction){
        System.out.println("Validator: starting");

        if(blockchain == null) return false;

        /* First, we need to find the state of the ledger */
        HashMap<String, Integer> accounts = new HashMap<String, Integer>();

        /* Crawling the chain from beginning */
        for(Block block : blockchain){
            HashSet<String> keys = new HashSet<>(block.getTxList().keySet());

            /* Looking at each transaction of the block */
            for(String key : keys){
                String fromAccount = block.getTxList().get(key).getFrom();
                String toAccount = block.getTxList().get(key).getTo();
                int amount = block.getTxList().get(key).getAmount();

                System.out.println("Validator: from: " + fromAccount + " to: " + toAccount + " amt: " + amount);

                /* Update our accounts based on this transaction */
                if(accounts.containsKey(toAccount)){
                    int toBalance = accounts.get(toAccount);
                    toBalance = toBalance + amount;
                    accounts.put(toAccount, toBalance);
                }else {
                    accounts.put(toAccount, amount);
                }
                
                if(accounts.containsKey(fromAccount)){
                    int fromBalance = accounts.get(fromAccount);
                    fromBalance = fromBalance - amount;
                    accounts.put(fromAccount, fromBalance);
                }else{
                    accounts.put(fromAccount, 0); // Not sure yet if we have spent from an account that doesnt exist ie genesis
                }
            }
        }

        System.out.println("Accounts: " + accounts);

        /* Validate Transaction */
        String fromAccount = transaction.getFrom();
        
        int amount = transaction.getAmount();
        if(!accounts.containsKey(fromAccount)) return false; // We don't have the account youre spending from
        
        int balance = accounts.get(fromAccount);
        if(amount > balance) return false; // Too much moeny trying to be spent

        System.out.println("Validator: valid");
        return true;
    }
}