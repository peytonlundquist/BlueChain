package node.blockchain.defi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import node.blockchain.merkletree.MerkleTreeProof;
import node.communication.Address;
import node.communication.messaging.Message;
import node.communication.messaging.Messager;
import node.communication.utils.DSA;

public class Wallet {

    BufferedReader reader; // To read user input
    ArrayList<Account> accounts; // Our defi account list
    ServerSocket ss;
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    HashSet<DefiTransaction> seenTransactions; // Transactions we've seen from full nodes
    Object updateLock; // Lock for multithreading
    boolean test; // Boolean for test vs normal output

    public Wallet(int port){

        /* Initializations */
        fullNodes = new ArrayList<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        accounts = new ArrayList<>();
        seenTransactions = new HashSet<>();
        updateLock = new Object();


        boolean boundToPort = false;
        int portBindingAttempts = 10; // Amount of attempts to bind to a port
        int fullNodeDefaultAmount = 3; // Full nodes we will try to connect to by default

        String path = "./src/main/java/node/nodeRegistry/"; 
        File folder = new File(path);        
        File[] listOfFiles = folder.listFiles();

        /* Iterate through each file in the nodeRegistry dir in order to derive our full nodes dynamically */
        for (int i = 0; i < listOfFiles.length; i++) {

            /* Make sure each item is in fact a file, isn't the special '.keep' file */
            if (listOfFiles[i].isFile() && !listOfFiles[i].getName().contains("keep") && fullNodes.size() < fullNodeDefaultAmount) {

                /* Extracting address from file name */
                String[] addressStrings = listOfFiles[i].getName().split("_");
                String hostname = addressStrings[0];
                String portString[] = addressStrings[1].split((Pattern.quote(".")));
                int fullNodePort = Integer.valueOf(portString[0]);
                fullNodes.add(new Address(fullNodePort, hostname));
            }
        }

        /* Binding to our Server Socket so full nodes can hit us up */
        try {
            ss = new ServerSocket(port);
            boundToPort = true;
        } catch (IOException e) {
            for(int i = 1; i < portBindingAttempts; i++){ // We will try several attempts to find a port we can bind too
                try {
                    ss = new ServerSocket(port - i);
                    boundToPort = true;
                    port = port - i;
                } catch (IOException E) {}
            }
        }

        if(boundToPort == false){
            System.out.println("Specify a new port in args[0]");
            System.exit(1);
        }

        InetAddress ip;

        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String host = ip.getHostAddress();
        myAddress = new Address(port, host);

        System.out.println("Wallet bound to " + myAddress);

        if(!this.test) System.out.println("Full Nodes to connect to by default: \n" + fullNodes + 
        "\nTo update Full Nodes address use 'u' command. \nUse 'h' command for full list of options");

        Acceptor acceptor = new Acceptor(this);
        acceptor.start();
    }

    public static void main(String[] args) throws IOException{

        System.out.println("============ BlueChain Wallet =============");

        BufferedReader mainReader = new BufferedReader(new InputStreamReader(System.in));
 
        // Reading data using readLine
        String input = "";
        int port = 7999;
        if(args.length > 0){
            if(args[0].equals("-port")){
                port = Integer.valueOf(args[0]);
            }else if(args[0].equals("-test")){
                Wallet wallet = new Wallet(port);
                wallet.test = true;
                wallet.testNetwork(Integer.valueOf(args[1]));
                System.exit(0); // We just test then exit
            }
        }

        Wallet wallet = new Wallet(port);
        wallet.test = false; // This is not a test

        while(!input.equals("exit") | !input.equals("e")){
            System.out.print(">");
            input = mainReader.readLine();
            wallet.interpretInput(input);
        }
    }

    /**
     * Interpret the string input
     * 
     * @param input the string to interpret
     */
    public void interpretInput(String input){
        try {
            switch(input){
                case("a"):
                    addAccount();
                    break;
                case("t"):
                    submitTransaction();
                    break;
                case("p"):
                    printAccounts();
                    break;
                case("u"):
                    updateFullNode();
                    break;
                case("d"): // implements functionality to deposit into account instead of arbitrary 10 coin cheat transaction 
                    depositAccount(); 
                    break; 
                case("h"):
                    printUsage();
                    break;
            }
        } catch (IOException e) {
            System.out.println("Input malformed. Try again.");
        } 
    }

    public void updateFullNode() throws IOException{
        System.out.println("Updating Full Nodes. \nAdd or remove? ('a' or 'r'): ");
        String response = reader.readLine();
        if(response.equals("a")){
            System.out.println("Full Node host?: ");
            String hostname = reader.readLine();
            System.out.println("Full Node port?: ");
            String port = reader.readLine();
            fullNodes.add(new Address(Integer.valueOf(port), hostname));
        }else if(response.equals("r")){
            System.out.println("Full Node index to remove?: \n" + fullNodes);
            int index = Integer.parseInt(reader.readLine());
            if(index > fullNodes.size()){
                System.out.println("Index not in range.");
                return;
            } 

            Address removedAddress = fullNodes.remove(index);
            System.out.println("Removed full node: " + removedAddress);
        }else{
            System.out.println("Invalid option");
        }
    }

    public void addAccount() throws IOException{
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
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);

            System.out.println("===============================");
            System.out.println("Account: " + newAccount.getNickname() + "\n\n Pubkey: " + pubKeyString);
            System.out.println("===============================");
        }
    }

    public void submitTransaction() throws IOException{
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

    private void submitTransaction(DefiTransaction transaction, Address address){
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

    private void depositAccount() { // need to implement functionality for depositing tokens into account, works around 10 coin cheat 
        ; 
    }

    private void printAccounts(){
        System.out.println("=============== Accounts ================");
        for(Account account :  accounts){
            System.out.println(account.getNickname() + " balance: " + account.getBalance());
            System.out.println("Pubkey: " + DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()) + "\n");
        }
        System.out.print(">");
    }

    private void updateAccounts(MerkleTreeProof mtp){
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


    public void testAddAccount(String nickname) throws IOException{
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
            Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()), 
            new Message(Message.Request.ALERT_WALLET, data), myAddress);
        }
    }

    public void testSubmitTransaction(String nickname, String to, int amount) throws IOException{
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

    private void testNetwork(int j){

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

    private void printUsage(){
        System.out.println("BlueChain Wallet Usage:");
        System.out.println("a: Add a new account");
        System.out.println("t: Create a transaction");
        System.out.println("p: Print acccounts and balances");
        System.out.println("u: Update full nodes");
        System.out.println("d: Deposit into account"); 
    }

    class Acceptor extends Thread {
        Wallet wallet;

        Acceptor(Wallet wallet){
            this.wallet = wallet;
        }

        public void run() {
            Socket client;
            while (true) {
                try {
                    client = ss.accept();
                    OutputStream out = client.getOutputStream();
                    InputStream in = client.getInputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    ObjectInputStream oin = new ObjectInputStream(in);
                    Message incomingMessage = (Message) oin.readObject();
                    
                    if(incomingMessage.getRequest().name().equals("ALERT_WALLET")){
                        MerkleTreeProof mtp = (MerkleTreeProof) incomingMessage.getMetadata();
                        updateAccounts(mtp);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}