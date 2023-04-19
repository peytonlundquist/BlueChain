package node.defi;
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

import node.communication.Address;
import node.communication.Message;
import node.communication.Messager;
import node.communication.utils.DSA;

public class Wallet {

    BufferedReader reader;
    ArrayList<Account> accounts;
    ServerSocket ss;
    Address myAddress;
    Address fullNodeAddress;

    public Wallet(int port){
        reader = new BufferedReader(new InputStreamReader(System.in));
        accounts = new ArrayList<>();
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Specify a new port in args[0]");
            System.exit(1);
        }
        myAddress = new Address(port, "localhost");

        System.out.println("Wallet bound to " + myAddress);

        fullNodeAddress = new Address(8001, "localhost");
        System.out.println("Full Node to connect to by default: " + fullNodeAddress + 
        "\nTo update Full Node address use 'u' command.");

        Acceptor acceptor = new Acceptor(this);
        acceptor.start();
    }

    public static void main(String[] args) throws IOException{
        System.out.println("============ BlueChain Wallet =============");

        BufferedReader mainReader = new BufferedReader(
            new InputStreamReader(System.in));
 
        // Reading data using readLine
        String input = "";
        int port;
        if(args.length > 0){
            port = Integer.valueOf(args[0]);
        }else{
            port = 7999;
        }

        Wallet wallet = new Wallet(port);

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
            e.printStackTrace();
        }
    }

    public void updateFullNode() throws IOException{
        System.out.println("Updating Full Node. \nFull Node host?: ");
        String hostname = reader.readLine();

        System.out.println("Full Node port?: ");
        String port = reader.readLine();

        fullNodeAddress = new Address(Integer.valueOf(port), hostname);
    }

    public void addAccount() throws IOException{
        System.out.println("Adding account. Account NickName?: ");
        String input = "unnamed";
        input = reader.readLine();
        KeyPair newKeyPair = DSA.generateDSAKeyPair();
        Account newAccount = new Account(input, newKeyPair);
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

        PrivateKey pk = chosenAccount.getKeyPair().getPrivate();
        String myPublicKeyString = DSA.bytesToString(chosenAccount.getKeyPair().getPublic().getEncoded());

        Transaction newTransaction = new Transaction(to, myPublicKeyString, amount, String.valueOf(System.currentTimeMillis()));
        String UID = newTransaction.getUID();
        byte[] signedUID = DSA.signHash(UID, pk);
        newTransaction.setSigUID(signedUID);
        submitTransaction(newTransaction, fullNodeAddress);
    }

    private void submitTransaction(Transaction transaction, Address address){
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, transaction);
            oout.writeObject(message);
            oout.flush();
            Thread.sleep(3000);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    }

    private void updateAccounts(Transaction transaction){
        for(Account account : accounts){
            if(DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getFrom())){
                account.updateBalance(-(transaction.getAmount()));
            }else if (DSA.bytesToString(account.getKeyPair().getPublic().getEncoded()).equals(transaction.getTo())){
                account.updateBalance(transaction.getAmount());
            }
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
                        Transaction transaction = (Transaction) incomingMessage.getMetadata();
                        updateAccounts(transaction);
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
