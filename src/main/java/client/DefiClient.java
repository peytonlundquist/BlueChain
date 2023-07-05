package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;

import node.blockchain.defi.Account;
import node.blockchain.defi.DefiTransaction;
import node.blockchain.merkletree.MerkleTreeProof;
import node.communication.Address;
import node.communication.messaging.Message;
import node.communication.messaging.Messager;
import node.communication.utils.DSA;

public class DefiClient {

    Object updateLock;
    BufferedReader reader;
    ArrayList<Account> accounts; // Our defi account list
    HashSet<DefiTransaction> seenTransactions; // Transactions we've seen from full nodes
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    boolean test; // Boolean for test vs normal output

    public DefiClient(Object updateLock, BufferedReader reader, Address myAddress, ArrayList<Address> fullNodes){
        this.reader = reader;
        this.updateLock = updateLock;
        this.myAddress = myAddress;
        this.fullNodes = fullNodes;

        seenTransactions = new HashSet<>();
        accounts = new ArrayList<>();

    }

    protected void addAccount() throws IOException{
        synchronized(updateLock){
            System.out.println("Adding account. Account NickName?: ");
            String input = "unnamed";
            input = reader.readLine();
            KeyPair newKeyPair = DSA.generateDSAKeyPair();
            Account newAccount = new Account(input, newKeyPair);
            
            for(Account account : accounts){
                if(account.getNickname().equals(input)){
                    System.out.println("An account with this nickname already exists. Try a new one.");
                    return;
                }
            }

            accounts.add(newAccount);

            String pubKeyString = DSA.bytesToString(newAccount.getKeyPair().getPublic().getEncoded());

            Object[] data = new Object[2];
            data[0] = pubKeyString;
            data[1] = myAddress;
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost(), null), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);

            System.out.println("===============================");
            System.out.println("Account: " + newAccount.getNickname() + "\n\n Pubkey: " + pubKeyString);
            System.out.println("===============================");
        }
    }

    protected void submitTransaction() throws IOException{
        System.out.println("Generating Transaction");
        System.out.println("Deposit address?");
        String to = reader.readLine();
        System.out.println("Withdraw account nickname?");
        String nickname = reader.readLine();
        System.out.println("Amount to send?");
        int amount = Integer.valueOf(reader.readLine());

        Account chosenAccount = null;
        for(Account account : accounts){
            if(account.getNickname().equals(nickname)) chosenAccount = account;
        }

        if(chosenAccount == null){
            System.out.println("Account with the nickname " + nickname + " is not found.");
            return;
        } 

        PrivateKey pk = chosenAccount.getKeyPair().getPrivate();
        String myPublicKeyString = DSA.bytesToString(chosenAccount.getKeyPair().getPublic().getEncoded());

        if(myPublicKeyString.equals(to)){
            System.out.println("Cannot send to self.");
            return;
        }

        DefiTransaction newTransaction = new DefiTransaction(to, myPublicKeyString, amount, String.valueOf(System.currentTimeMillis()));
        String UID = newTransaction.getUID();
        byte[] signedUID = DSA.signHash(UID, pk);
        newTransaction.setSigUID(signedUID);

        System.out.println("Submitting transaction to nodes: ");
        for(Address address : fullNodes){
            submitTransaction(newTransaction, address);
        }
    }

    protected void submitTransaction(DefiTransaction transaction, Address address){
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, transaction);
            oout.writeObject(message);
            oout.flush();
            Thread.sleep(1000);
            s.close();
            if(!this.test) System.out.println("Full node: " + address);
        } catch (IOException e) {
            System.out.println("Full node at " + address + " appears down.");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void printAccounts(){
        System.out.println("=============== Accounts ================");
        for(Account account :  accounts){
            System.out.println(account.getNickname() + " balance: " + account.getBalance());
            System.out.println("Pubkey: " + DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()) + "\n");
        }
        System.out.print(">");
    }

    protected void updateAccounts(MerkleTreeProof mtp){
        synchronized(updateLock){

            DefiTransaction transaction = (DefiTransaction) mtp.getTransaction();

            for(DefiTransaction existingTransaction : seenTransactions){
                if(existingTransaction.equals(transaction)){
                    return;
                }
            }

            boolean interested = false;

            /* Make sure transaction is about accounts we have */
            for(Account account : accounts){
                if(DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getFrom())
                || DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getTo())){
                    interested = true;
                }
            }

            if(interested == false){ 
                System.out.println("\nOur accounts isn't in this transaction I guess..." );
                return;
            }
            
            seenTransactions.add(transaction);

            if(!mtp.confirmMembership()){
                System.out.println("Could not validate tx in MerkleTreeProof" );
                return;
            }
    
            for(Account account : accounts){
                if(DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getFrom())){
                    account.updateBalance(-(transaction.getAmount()));
                }
                if (DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getTo())){
                    account.updateBalance(transaction.getAmount());
                }
            }

            if(!this.test) System.out.println("\nFull node has update. Updating accounts..." );
            if(!this.test) printAccounts();
        }
    }


    protected void testAddAccount(String nickname) throws IOException{
        synchronized(updateLock){

            KeyPair newKeyPair = DSA.generateDSAKeyPair();
            Account newAccount = new Account(nickname, newKeyPair);
            
            for(Account account : accounts){
                if(account.getNickname().equals(nickname)){
                    System.out.println("An account with this nickname already exists. Try a new one.");
                    return;
                }
            }

            accounts.add(newAccount);

            String pubKeyString = DSA.bytesToString(newAccount.getKeyPair().getPublic().getEncoded());

            Object[] data = new Object[2];
            data[0] = pubKeyString;
            data[1] = myAddress;
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost(), null), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);
        }
    }

    protected void testSubmitTransaction(String nickname, String to, int amount) throws IOException{
        Account chosenAccount = null;
        for(Account account : accounts){
            if(account.getNickname().equals(nickname)) chosenAccount = account;
        }

        if(chosenAccount == null){
            System.out.println("Account with the nickname " + nickname + " is not found.");
            return;
        } 

        PrivateKey pk = chosenAccount.getKeyPair().getPrivate();
        String myPublicKeyString = DSA.bytesToString(chosenAccount.getKeyPair().getPublic().getEncoded());

        if(myPublicKeyString.equals(to)){
            System.out.println("Cannot send to self.");
            return;
        }

        DefiTransaction newTransaction = new DefiTransaction(to, myPublicKeyString, amount, String.valueOf(System.currentTimeMillis()));
        String UID = newTransaction.getUID();
        byte[] signedUID = DSA.signHash(UID, pk);
        newTransaction.setSigUID(signedUID);

        for(Address address : fullNodes){
            submitTransaction(newTransaction, address);
        }
    }

    void testNetwork(int j){

        System.out.println("Beginning Test");

        try {
            testAddAccount("Satoshi");
            int expectedBalance = j * 10;

            System.out.print("[");
            for(int i = 0; i < j; i++){
                    testAddAccount(String.valueOf(i));
                    Thread.sleep(2000);
                    testSubmitTransaction(String.valueOf(i), DSA.bytesToString(accounts.get(0).getKeyPair().getPublic().getEncoded()), 10);
                    System.out.print("#");
            }
            System.out.println("]");
            System.out.println("Sleeping wallet for last minute updates...");
            Thread.sleep(50000);
            if(accounts.get(0).getBalance() == expectedBalance){
                System.out.println("\n*********************Test passed.*********************");
            }else{
                System.out.println("\n*********************Test Failed*********************");
            }

            System.out.println("Satoshi expected balance: " + expectedBalance + ". Actual: " + accounts.get(0).getBalance());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void printUsage(){
        System.out.println("BlueChain Wallet Usage:");
        System.out.println("a: Add a new account");
        System.out.println("t: Create a transaction");
        System.out.println("p: Print acccounts and balances");
        System.out.println("u: Update full nodes");
    }
}