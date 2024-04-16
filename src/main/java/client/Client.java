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
import java.util.HashMap;

/**
 * Represents a client application for interacting with the BlueChain network.
 */
public class Client {

    BufferedReader reader; // To read user input
    ServerSocket ss;
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    Object updateLock; // Lock for multithreading
    boolean test; // Boolean for test vs normal output
    static String use;
    DefiClient defiClient;

    HCClient hcClient;
    static boolean isPatient;

    /**
     * Constructs a Client instance.
     * @param port The port to bind the client's server socket.
     */
    public Client(int port){

        /* Initializations */
        fullNodes = new ArrayList<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        updateLock = new Object();


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

        defiClient = new DefiClient(updateLock, reader, myAddress, fullNodes);
        hcClient = new HCClient(updateLock, reader, myAddress, fullNodes);

        if (isPatient) {
            hcClient.setPatientClient(true);
        }
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
                Client testClient = new Client(port);
                testClient.test = true;
                testClient.testNetwork( Integer.valueOf(args[1]));
                System.exit(0);
            } else if (args[0].equals("-patient")) {
                isPatient = true;
            }
        }

        Client client = new Client(port);

        while(!input.equals("exit") | !input.equals("e")){
            System.out.print(">");
            input = mainReader.readLine();
            client.interpretInput(input);
        }
    }

    /**
     * Interpret the string input
     * 
     * @param input the string to interpret
     * @throws ParseException 
     */
    public void interpretInput(String input) throws ParseException{
        try {
            switch(input){

                /* Add account (or something similar depends on use) */
                case("a"):
                    if(use.equals("Defi")) defiClient.addAccount();
                    if(use.equals("HC") && !isPatient) hcClient.createAppointment();
                    break;

                /* Submit Transaction */
                case("t"):
                    if(use.equals("Defi")) defiClient.submitTransaction();
                    break;

                /* Print accounts (or something similar depends on use) */
                case("p"):
                    if(use.equals("Defi")) defiClient.printAccounts();
                    if(use.equals("HC") && !isPatient) hcClient.createPerscription();
                    break;

                /* Print the specific usage / commmands */
                case("h"):
                    if(use.equals("Defi")) defiClient.printUsage();
                    if(use.equals("HC") && !isPatient) hcClient.printUsage();
                    if(use.equals("HC") && isPatient) hcClient.printPatientUsage();
                    break;

                case("n"):
                    if(use.equals("HC") && !isPatient) hcClient.createNewPatient();
                    break;

                case("r"):
                    if(use.equals("HC") && !isPatient) hcClient.updateRecord();
                    break;

                case("s"):
                    if(use.equals("HC")) hcClient.showPatientDetails();
                    break;

                case("c"):
                    if(use.equals("HC")) hcClient.createNewPatient();
                    break;

                case ("d"):
                    if(use.equals("HC") && !isPatient) hcClient.showAllPatients();
                    break;

                /* Update full nodes */
                case("u"):
                    updateFullNode();
                    break;
    
            }
        } catch (IOException e) {
            System.out.println("Input malformed. Try again.");
        } 
    }

    /**
     * Update the list of full nodes we are communicating with in the network
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * Test the network by simulating transactions.
     * @param iterations The number of test iterations.
     */
    public void testNetwork(int iterations){
        if(use.equals("Defi")){
            defiClient.test = true;
            defiClient.testNetwork(iterations);
        } else {
            hcClient.test = true;
            hcClient.testNetwork(iterations);
        }
    }

    /**
     *  A thread for accepting incoming connections.
     */
    class Acceptor extends Thread {
        Client wallet;

        Acceptor(Client wallet){
            this.wallet = wallet;
        }

        @SuppressWarnings("unchecked")
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
                    
                    if(incomingMessage.getRequest().name().equals("ALERT_WALLET")) {
                        MerkleTreeProof mtp = (MerkleTreeProof) incomingMessage.getMetadata();
                        if (use.equals("Defi")) {
                            defiClient.updateAccounts(mtp);
                        } else if (use.equals("HC")) {
                            hcClient.updatePatientDetails(mtp);
                        }
                    } else if (incomingMessage.getRequest().name().equals("SEND_TX")) {
                        hcClient.initializeClient((HashMap<String, Transaction>) incomingMessage.getMetadata());
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