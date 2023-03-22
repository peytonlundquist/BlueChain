package node.blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TransactionValidator{
    ArrayList<Block> blockchain;
    HashMap<String, Transaction> mempool;
    HashMap<String, Integer> accounts;

    public TransactionValidator(ArrayList<Block> blockchain, HashMap<String, Transaction> mempool){
        this.blockchain = blockchain;
        this.mempool = mempool;
        accounts = new HashMap<String, Integer>();
    }

    private void tallyBalance(HashMap<String, Transaction> hashMap){

        HashSet<String> keys = new HashSet<>(hashMap.keySet());

        /* Looking at each transaction of the block */
        for(String key : keys){

            String fromAccount = hashMap.get(key).getFrom();
            String toAccount = hashMap.get(key).getTo();
            int amount = hashMap.get(key).getAmount();

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

    /**
     * Validates a transaction throughout blockchain and mempool
     * @param transaction
     * @return
     */
    public boolean validate(Transaction transaction){
        // System.out.println("Validator: starting");

        if(blockchain == null) return false;

        /* First, we need to find the state of the ledger */
        
        /* Crawling the chain from beginning to get history */
        for(Block block : blockchain){
            HashMap<String, Transaction> hashMap = block.getTxList();
            tallyBalance(hashMap);
        }

        /* Check mempool also for double dipping */
        tallyBalance(mempool);
        System.out.println("Accounts: " + accounts);

        /* Validate Transaction */
        String fromAccount = transaction.getFrom();
        int amount = transaction.getAmount();

        if(!accounts.containsKey(fromAccount)) return false; // We don't have the account youre spending from
        
        int balance = accounts.get(fromAccount);
        if(amount > balance) return false; // Too much money trying to be spent
        
        return true;
    }
}