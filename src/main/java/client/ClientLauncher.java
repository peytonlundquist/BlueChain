package client;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties; 
import java.util.regex.Pattern;

import com.github.lalyos.jfiglet.FigletFont;

import communication.messaging.Message;
import utils.Address;
import utils.merkletree.MerkleTreeProof;
import blockchain.Transaction;

/**
 * Represents a client application for interacting with the BlueChain network.
 */
public class ClientLauncher {

    BufferedReader reader; // To read user input
    ServerSocket ss;
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    Object updateLock; // Lock for multithreading
    boolean test; // Boolean for test vs normal output
    static String use;
    //DefiClient defiClient;

    HCClient hcClient;
    static boolean isPatient;
    public Client client;

    /**
     * Constructs a Client instance.
     * @param port The port to bind the client's server socket.
     */
    public ClientLauncher(int port){
        reader = new BufferedReader(new InputStreamReader(System.in));

        /* Grab values from config file */
        String configFilePath = "src/main/java/config.properties";
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(configFilePath);    
            Properties prop = new Properties();
            prop.load(fileInputStream);
            use = prop.getProperty("USE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (use.equals("Defi")) {
            client = new DefiClient(port);
        } else if (use.equals("HC")) {
            client = new HCClient(port);

            if (isPatient) {
                ((HCClient) client).setPatientClient(true);
            }
        }

        this.ss = client.getSS();
        this.fullNodes = client.getFullNodes();
        this.myAddress = client.getMyAddress();

        Acceptor acceptor = new Acceptor(this);
        acceptor.start();

        System.out.println("Wallet bound to " + myAddress);

        if(!this.test) System.out.println("Full Nodes to connect to by default: \n" + fullNodes + 
        "\nTo update Full Nodes address use 'u' command. \nUse 'h' command for full list of options");
    }

    public static void main(String[] args) throws IOException, ParseException{

        String asciiArt1 = FigletFont.convertOneLine("BlueChain Client");
        System.out.println(asciiArt1);

        BufferedReader mainReader = new BufferedReader(new InputStreamReader(System.in));
 
        // Reading data using readLine
        String input = "";
        int port = 7999;
        isPatient = false;

        if(args.length > 0){
            if(args[0].equals("-port")){
                port = Integer.valueOf(args[0]);
            }else if(args[0].equals("-test")){
                ClientLauncher testClient = new ClientLauncher(port);
                testClient.test = true;
                testClient.testNetwork( Integer.valueOf(args[1]));
                System.exit(0);
            } else if (args[0].equals("-patient")) {
                isPatient = true;
            }
        }

        ClientLauncher clientLauncher = new ClientLauncher(port);

        while(!input.equals("exit") | !input.equals("e")){
            System.out.print(">");
            input = mainReader.readLine();
            clientLauncher.client.interpretInput(input);
        }
    }

    /**
     * Test the network by simulating transactions.
     * @param iterations The number of test iterations.
     */
    public void testNetwork(int iterations){
        client.test = true;
        client.testNetwork(iterations);
    }

    /**
     *  A thread for accepting incoming connections.
     */
    class Acceptor extends Thread {
        ClientLauncher wallet;

        Acceptor(ClientLauncher wallet){
            this.wallet = wallet;
        }

        @SuppressWarnings({ "unchecked", "unused" })
        public void run() {
            Socket ssClient;
            while (true) {
                try {
                    ssClient = ss.accept();
                    OutputStream out = ssClient.getOutputStream();
                    InputStream in = ssClient.getInputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    ObjectInputStream oin = new ObjectInputStream(in);
                    Message incomingMessage = (Message) oin.readObject();
                    
                    if(incomingMessage.getRequest().name().equals("ALERT_WALLET")) {
                        MerkleTreeProof mtp = (MerkleTreeProof) incomingMessage.getMetadata();
                        if (use.equals("Defi")) {
                            ((DefiClient) client).updateAccounts(mtp);
                        } else if (use.equals("HC")) {
                            ((HCClient) client).updatePatientDetails(mtp);
                        }
                    } else if (incomingMessage.getRequest().name().equals("SEND_TX")) {
                        ((HCClient) client).initializeClient((ArrayList<Transaction>) incomingMessage.getMetadata());
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