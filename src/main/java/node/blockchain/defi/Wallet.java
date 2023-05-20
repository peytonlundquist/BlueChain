package node.blockchain.defi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;

import node.blockchain.MerkleTree;
import node.blockchain.MerkleTreeProof;
import node.blockchain.Transaction;
import node.communication.Address;
import node.communication.Message;
import node.communication.Messager;
import node.communication.utils.DSA;
import node.communication.utils.Hashing;

public class Wallet {

    BufferedReader reader;
    ArrayList<Account> accounts;
    ServerSocket ss;
    Address myAddress;
    ArrayList<Address> fullNodes;
    Address fullNodeAddress;
    HashSet<DefiTransaction> seenTransactions;
    Object updateLock;
    boolean test;

    public Wallet(int port){
        fullNodes = new ArrayList<>();

        Address fullNodeAddress1 = new Address(8001, "localhost"); fullNodes.add(fullNodeAddress1);
        Address fullNodeAddress2 = new Address(8002, "localhost"); fullNodes.add(fullNodeAddress2);
        Address fullNodeAddress3 = new Address(8003, "localhost"); fullNodes.add(fullNodeAddress3);

        reader = new BufferedReader(new InputStreamReader(System.in));
        accounts = new ArrayList<>();
        seenTransactions = new HashSet<>();
        updateLock = new Object();

        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Specify a new port in args[0]");
            System.exit(1);
        }
        myAddress = new Address(port, "localhost");

        System.out.println("Wallet bound to " + myAddress);

        if(!this.test) System.out.println("Full Nodes to connect to by default: \n" + fullNodes + 
        "\nTo update Full Nodes address use 'u' command.");

        Acceptor acceptor = new Acceptor(this);
        acceptor.start();
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
            System.out.print("]");
            System.out.println("Sleeping wallet for last minute updates...");
            Thread.sleep(50000);
            if(accounts.get(0).getBalance() == expectedBalance){
                System.out.println("*********************Test passed.*********************");
            }else{
                System.out.println("*********************Test Failed*********************");
            }

            System.out.println("Satoshi expected balance: " + expectedBalance + ". Actual: " + accounts.get(0).getBalance());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                return;
            }
        }

        Wallet wallet = new Wallet(port);
        wallet.test = false;

        while(!input.equals("exit") | !input.equals("e")){
            System.out.print(">");
            input = mainReader.readLine();
            wallet.interpretInput(input);
        }
    }


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
            Messager.sendOneWayMessage(new Address(8001, "localhost"), 
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
        System.out.println(signedUID.toString() + " sig not null from wallet");
        newTransaction.setSigUID(signedUID);
        System.out.println(newTransaction.getSigUID().toString() + " sig not null from wallet");

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
            if (!this.test) System.out.println("\nFull node has update. Updating accounts..." );

            DefiTransaction transaction = (DefiTransaction) mtp.getTransaction();

            for(DefiTransaction existingTransaction : seenTransactions){
                if(existingTransaction.equals(transaction)){
                    return;
                }
            }

            boolean interested = false;
            /* Make sure transaction is about accounts we have */
            for(Account account : accounts){
                if(!DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getFrom())
                && !DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getTo())){
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
    
            //System.out.println("\nFull node has update. Updating accounts..." );
            for(Account account : accounts){
                if(DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getFrom())){
                    account.updateBalance(-(transaction.getAmount()));
                }
                if (DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getTo())){
                    account.updateBalance(transaction.getAmount());
                }
            }
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
            Messager.sendOneWayMessage(new Address(8001, "localhost"), 
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