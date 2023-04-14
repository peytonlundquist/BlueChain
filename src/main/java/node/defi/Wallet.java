package node.defi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;

import node.communication.Message;
import node.communication.utils.DSA;

public class Wallet {

    BufferedReader reader;
    ArrayList<Account> accounts;

    public Wallet(){
        reader = new BufferedReader(new InputStreamReader(System.in));
        accounts = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException{
        
        BufferedReader mainReader = new BufferedReader(
            new InputStreamReader(System.in));
 
        // Reading data using readLine
        String input = "";
        Wallet wallet = new Wallet();

        while(!input.equals("exit") | !input.equals("e")){
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
                    addAccount();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAccount() throws IOException{
        System.out.println("Adding account. Account NickName?: ");
        String input = "unnamed";
        input = reader.readLine();
        KeyPair newKeyPair = DSA.generateDSAKeyPair();
        Account newAccount = new Account(input, newKeyPair);
        accounts.add(newAccount);
        System.out.println("===============================");
        System.out.println("Account: " + newAccount.getNickname() + "\n pubkey: " + newAccount.getKeyPair().getPublic().toString());
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
        submitTransaction(newTransaction, 8001);
    }

    private static void submitTransaction(Transaction transaction, int port){
        try {
            Socket s = new Socket("localhost", port);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, transaction);
            oout.writeObject(message);
            oout.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
