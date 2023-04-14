package node.defi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import node.communication.utils.DSA;

public class TransactionValidator{

    /**
     * Validates a transaction throughout blockchain and mempool
     * @param transaction
     * @return
     */
    public static boolean validate(Transaction transaction, HashMap<String, Integer> accounts, HashMap<String, Transaction> assumedTransactions){
        // System.out.println("Validator: starting");

        /* Two contexts to validate. Non quorum node need to validate against their mempool. Quorum node needs to validate against compiled mempool */

        /* We want to assume transactions added to our mempool are valid and take priority. 
        If there is a conflict from a new transaction and existing ones we choose existing ones */

        HashMap<String, Integer> tempAccounts = new HashMap<>(accounts); // We make a temp set of accounts which contain all previous accounts
        updateAccounts(assumedTransactions, tempAccounts); // We updated the temp accounts with the mempool, creating a "what-if" scenario where each transaction in mempool is valid

        /* Check mempool also for double dipping */
        System.out.println("Accounts: " + accounts);

        /* Validate Transaction */
        String fromAccount = transaction.getFrom();
        int amount = transaction.getAmount();

        if(amount < 0) return false; // No negatives

        if(!tempAccounts.containsKey(fromAccount)) return false; // We don't have the account youre spending from
        
        int balance = tempAccounts.get(fromAccount);
        if(amount > balance) return false; // Too much money trying to be spent

        /* Let's validate the signature */
        String publicKeyString = transaction.getFrom(); // We get the public key in string format
        byte[] publicKeyBytes = DSA.stringToBytes(publicKeyString); // Convert back to bytes for DSA
        byte[] sigOfUID = transaction.getSigUID(); // Get signature of UID
        String UID = transaction.getUID();    // Get UID

        if(!DSA.verifySignature(UID, sigOfUID, publicKeyBytes)) return false; // Validate that the sender signed the transaction

        return true;
    }

    /**
     * Update the provided accounts hashmap with already validated transactions
     * @param blockTxList
     * @param accounts
     */
    public static void updateAccounts(HashMap<String, Transaction> blockTxList, HashMap<String, Integer> accounts){
        HashSet<String> keys = new HashSet<>(blockTxList.keySet());

        // For each hash of a transaction
        for(String key : keys){
            Transaction transaction = blockTxList.get(key); // Grabbing the first transaction from our list of tx using hash
            
            String fromAccount = transaction.getFrom();
            String toAccount = transaction.getTo();
            int amount = transaction.getAmount();

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
}