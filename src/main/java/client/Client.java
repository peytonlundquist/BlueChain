package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import blockchain.Transaction;
import communication.messaging.Message;
import utils.Address;
import utils.merkletree.MerkleTreeProof;

public abstract class Client {
    protected Object updateLock;
    protected BufferedReader reader;
    protected Address myAddress;
    protected ArrayList<Address> fullNodes;
    protected boolean test;

    private ServerSocket ss;
    private String use;

    public Client(int port) {
        /* Initializations */
        this.fullNodes = new ArrayList<>();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.updateLock = new Object();

        boolean boundToPort = false;
        int portBindingAttempts = 10; // Amount of attempts to bind to a port
        int fullNodeDefaultAmount = 3; // Full nodes we will try to connect to by default

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

        String path = "./src/main/java/node/nodeRegistry/"; 
        File folder = new File(path);        
        File[] listOfFiles = folder.listFiles();

        /* Iterate through each file in the nodeRegistry dir in order to derive our full nodes dynamically */
        for (int i = 0; i < listOfFiles.length; i++) {

            /* Make sure each item is in fact a file, isn't the special '.keep' file */
            if (listOfFiles[i].isFile() && !listOfFiles[i].getName().contains("keep") && this.fullNodes.size() < fullNodeDefaultAmount) {

                /* Extracting address from file name */
                String[] addressStrings = listOfFiles[i].getName().split("_");
                String hostname = addressStrings[0];
                String portString[] = addressStrings[1].split((Pattern.quote(".")));
                int fullNodePort = Integer.valueOf(portString[0]);
                this.fullNodes.add(new Address(fullNodePort, hostname));
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
        this.myAddress = new Address(port, host);

        Acceptor acceptor = new Acceptor(this);
        acceptor.start();
    }

    public ServerSocket getSS() {
        return ss;
    }

    public ArrayList<Address> getFullNodes() {
        return fullNodes;
    }

    public Address getMyAddress() {
        return myAddress;
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

    public abstract void testNetwork(int numOfTests);
    public abstract void printUsage();
    public abstract void interpretInput(String input) throws IOException, ParseException;
    public abstract void updateAccounts(MerkleTreeProof mtp) throws IOException;
    public void initializeClient(ArrayList<Transaction> transactions) {}

    /**
     *  A thread for accepting incoming connections.
     */
    class Acceptor extends Thread {
        Client wallet;

        Acceptor(Client client){
            this.wallet = client;
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
                        updateAccounts(mtp);
                    } else if (incomingMessage.getRequest().name().equals("SEND_TX") && use.equals("HC")) {
                        initializeClient((ArrayList<Transaction>) incomingMessage.getMetadata());
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
