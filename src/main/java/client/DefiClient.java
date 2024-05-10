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

import blockchain.usecases.defi.Account;
import blockchain.usecases.defi.DefiTransaction;
import communication.messaging.Message;
import communication.messaging.Messager;
import me.tongfei.progressbar.ProgressBar;
import utils.Address;
import utils.DSA;
import utils.merkletree.MerkleTreeProof;

public class DefiClient {

    Object updateLock;
    BufferedReader reader;
    ArrayList<Account> accounts; // Our defi account list
    HashSet<DefiTransaction> seenTransactions; // Transactions we've seen from full nodes
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    boolean test; // Boolean for test vs normal output


    /**
     * Constructs a DefiClient instance.
     * @param updateLock The lock for multithreading.
     * @param reader The BufferedReader for reading user input.
     * @param myAddress The address of the client.
     * @param fullNodes The list of full nodes to interact with.
     */
    public DefiClient(Object updateLock, BufferedReader reader, Address myAddress, ArrayList<Address> fullNodes){
        this.reader = reader;
        this.updateLock = updateLock;
        this.myAddress = myAddress;
        this.fullNodes = fullNodes;

        seenTransactions = new HashSet<>();
        accounts = new ArrayList<>();

    }

    /**
     * Adds a new account to the client.
     * @throws IOException If an I/O error occurs.
     */
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

            Object[] data = {pubKeyString, myAddress};
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);

            System.out.println("===============================");
            System.out.println("Account: " + newAccount.getNickname() + "\n\n Pubkey: " + pubKeyString);
            System.out.println("===============================");
        }
    }

    /**
     * Submits a new transaction to the Defi network.
     * @throws IOException If an I/O error occurs.
     */
    protected void submitTransaction() throws IOException{
        System.out.println("Generating Transaction");
        System.out.println("Deposit address?");
        String to = reader.readLine();
        System.out.println("Withdraw account nickname?");
        String nickname = reader.readLine();
        System.out.println("Amount to send?");
        int amount = Integer.valueOf(reader.readLine());
        System.out.println("Note?");
        String note = reader.readLine();

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

        DefiTransaction newTransaction = new DefiTransaction(to, myPublicKeyString, amount, String.valueOf(System.currentTimeMillis()), note);
        String UID = newTransaction.getUID();
        byte[] signedUID = DSA.signHash(UID, pk);
        newTransaction.setSigUID(signedUID);

        System.out.println("Submitting transaction to nodes: ");
        for(Address address : fullNodes){
            submitTransaction(newTransaction, address);
        }
    }

    /**
     * Submits a transaction to a specific full node.
     * @param transaction The DefiTransaction to submit.
     * @param address The address of the full node.
     */
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

    /**
     * Prints information about the client's accounts and their balances.
     */
    protected void printAccounts(){
        System.out.println("=============== Accounts ================");
        for(Account account :  accounts){
            System.out.println(account.getNickname() + " balance: " + account.getBalance());
            System.out.println("Pubkey: " + DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()) + "\n");
        }
        System.out.print(">");
    }

    /**
     * Updates the client's accounts based on a MerkleTreeProof.
     * @param mtp The MerkleTreeProof containing the transaction information.
     */
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

    /**
     * Tests the addition of a new account to the client for simulation purposes.
     * Generates a new key pair, creates a new account, and adds it to the account list.
     * Sends an alert to the network about the new account.
     *
     * @param nickname The nickname for the new account.
     * @throws IOException If an I/O error occurs.
     */
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

            Object[] data = {pubKeyString, myAddress};
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);
        }
    }

    /**
     * Tests the submission of a transaction to the Defi network for simulation purposes.
     * Retrieves the chosen account, generates a new DefiTransaction, signs it,
     * and submits the transaction to each full node in the network.
     *
     * @param nickname The nickname of the sending account.
     * @param to The deposit address for the transaction.
     * @param amount The amount to be sent in the transaction.
     * @throws IOException If an I/O error occurs.
     */
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

        DefiTransaction newTransaction = new DefiTransaction(to, myPublicKeyString, amount, String.valueOf(System.currentTimeMillis()), "Admin");
        String UID = newTransaction.getUID();
        byte[] signedUID = DSA.signHash(UID, pk);
        newTransaction.setSigUID(signedUID);

        for(Address address : fullNodes){
            submitTransaction(newTransaction, address);
        }
    }

    /**
     * Performs a network test with the specified number of iterations.
     * @param iterations The number of test iterations.
     */
    void testNetwork(int j){
        System.out.println("Beginning Test");
        try {            
            testAddAccount("Satoshi");
            int expectedBalance = j * 10;

            ProgressBar pb = new ProgressBar("Test", j);
            pb.start(); // the progress bar starts timing
            pb.setExtraMessage("Testing..."); // Set extra message to display at the end of the bar
            

            for(int i = 0; i < j; i++){
                testAddAccount(String.valueOf(i));
                Thread.sleep(500);
                testSubmitTransaction(String.valueOf(i), DSA.bytesToString(accounts.get(0).getKeyPair().getPublic().getEncoded()), 10);
                pb.step(); 
            }
            pb.stop(); // stops the progress bar
            System.out.println("Sleeping wallet for last minute updates...");
            Thread.sleep(100000);
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

    /**
     * Prints the usage information for the BlueChain Wallet.
     */
    protected void printUsage(){
        System.out.println("BlueChain Wallet Usage:");
        System.out.println("a: Add a new account");
        System.out.println("t: Create a transaction");
        System.out.println("p: Print acccounts and balances");
        System.out.println("u: Update full nodes");
    }
}