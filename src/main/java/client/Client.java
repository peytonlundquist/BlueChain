package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import utils.Address;

public abstract class Client {
    protected Object updateLock;
    protected BufferedReader reader;
    protected Address myAddress;
    protected ArrayList<Address> fullNodes;
    protected boolean test;

    private ServerSocket ss;

    public Client(int port) {
        /* Initializations */
        this.fullNodes = new ArrayList<>();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.updateLock = new Object();

        boolean boundToPort = false;
        int portBindingAttempts = 10; // Amount of attempts to bind to a port
        int fullNodeDefaultAmount = 3; // Full nodes we will try to connect to by default

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
}
