package blockchain.usecases.defi;

import java.util.HashMap;
import java.util.HashSet;

import blockchain.Transaction;
import blockchain.TransactionValidator;
import communication.messaging.Message;
import communication.messaging.Messager;
import utils.Address;
import utils.DSA;
import utils.LockManager;
import utils.merkletree.MerkleTree;

/**
 * An implementation of a TransactionValidator for the Defi use case
 */
public class DefiTransactionValidator extends TransactionValidator{

    private HashMap<String, Integer> accounts;
    private HashMap<String, Address> accountsToAlert;
    private LockManager lockManager;
    

    public DefiTransactionValidator(){
        accounts = new HashMap<String, Integer>();
        accountsToAlert = new HashMap<>();
        lockManager = new LockManager();
        lockManager.addLock("accountsLock");
    }
    
    /**
     * Validates a transaction throughout blockchain and mempool
     * @param transaction The transaction we are deciding the validity of
     * @param accounts Our current accounts which are represented by their public key hash string and their balance in a Map
     * @param assumedT The transactions we assume to valid so far
     * @return
     */
    public static boolean isValid(Transaction t, HashMap<String, Integer> accounts, HashMap<String, Transaction> assumedT){
        DefiTransaction transaction = (DefiTransaction) t; // Convert the generic transaction to be a DefiTransaction
        
        HashMap<String, DefiTransaction> assumedTransactions = new HashMap<>();
        HashSet<String> keys = new HashSet<>(assumedT.keySet());

        /* Converting each Transaction to DefiTransaction */
        for(String key : keys){
            DefiTransaction transactionInList = (DefiTransaction) assumedT.get(key);
            assumedTransactions.put(key, transactionInList);
        }
        
        /* Two contexts to validate. Non quorum node need to validate 
        against their mempool. Quorum node needs to validate against 
        compiled mempool */

        /* We want to assume transactions added to our mempool are valid 
        and take priority. If there is a conflict from a new transaction 
        and existing ones we choose existing ones */

        HashMap<String, Integer> tempAccounts = new HashMap<>(accounts); // We make a temp set of accounts which contain all previous accounts
        updateAccounts(assumedTransactions, tempAccounts); // We updated the temp accounts with the mempool, creating a "what-if" scenario where each transaction in mempool is valid

        /* Check mempool also for double dipping */

        /* Validate Transaction */
        String fromAccount = transaction.getFrom();
        int amount = transaction.getAmount();

        if(amount < 0) return false; // No negatives

        if(!tempAccounts.containsKey(fromAccount)) {
            if(amount != 10){ // This is our cheat for now
                return false;
            }
        }else{
            int balance = tempAccounts.get(fromAccount);
            if(amount > balance) return false; // Too much money trying to be spent
        }
        
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
     * @param blockTxList DefiTransaction list from the block and their associated hash strings
     * @param accounts
     */
    public static void updateAccounts(HashMap<String, DefiTransaction> blockTxList, HashMap<String, Integer> accounts){
        HashSet<String> keys = new HashSet<>(blockTxList.keySet());

        // For each hash of a transaction
        for(String key : keys){
            DefiTransaction transaction = blockTxList.get(key); // Grabbing the first transaction from our list of tx using hash
            
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

    @Override
    public boolean validate(Object[] objects) {
        Transaction transaction = (Transaction) objects[0];
        HashMap<String, Transaction> assumedTransactions = (HashMap<String, Transaction>) objects[1];
        return isValid(transaction, accounts, assumedTransactions);
    }

    public void alertWallet(HashMap<String, Transaction> txMap, MerkleTree mt, Address myAddress){
        HashMap<String, DefiTransaction> defiTxMap = new HashMap<>();
        HashSet<String> keys = new HashSet<>(txMap.keySet());

        for(String key : keys){
            DefiTransaction transactionInList = (DefiTransaction) txMap.get(key);
            defiTxMap.put(key, transactionInList);
        }

        DefiTransactionValidator.updateAccounts(defiTxMap, accounts);

        synchronized(lockManager.getLock("accountsLock")){
            for(String account : accountsToAlert.keySet()){
                // System.out.println(account);
                for(String transHash : txMap.keySet()){
                    DefiTransaction dtx = (DefiTransaction) txMap.get(transHash);
                    // System.out.println(dtx.getFrom() + "---" + dtx.getTo());
                    if(dtx.getFrom().equals(account) ||
                    dtx.getTo().equals(account)){
                        Messager.sendOneWayMessage(accountsToAlert.get(account), 
                        new Message(Message.Request.ALERT_WALLET, mt.getProof(txMap.get(transHash))), myAddress);
                        //System.out.println("sent update");
                    }
                }
            }
        }
    }

    public void addAccountsToAlert(String accountPubKey, Address address){
        synchronized(lockManager.getLock("accountsLock")){
            accountsToAlert.put(accountPubKey, address);
        }
    }
}