import graphing.Graph;
import graphing.GraphNode;
import node.blockchain.Transaction;
import node.communication.Address;
import node.communication.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

//import javax.json.*;

/**
 * One shot client that communicates with the network's nodes
 * Usage: <[graph] [query <portNum>] [trans <portNum> <Transaction String Id>]>
 */
public class Client {

    public static void main(String[] args) throws FileNotFoundException {
        int port;
        int numNodes = 0;
        int startingPort = 0;

        try {
            String configFilePath = "src/main/java/config.properties";
            FileInputStream fileInputStream = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(fileInputStream);

            numNodes = Integer.parseInt(prop.getProperty("NUM_NODES"));
            startingPort = Integer.parseInt(prop.getProperty("STARTING_PORT"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(args.length > 0) {
            if (args[0].equals("graph")) {
                LinkedList<GraphNode> graphNodes = new LinkedList<>();
                for (int i = 0; i < numNodes; i++) {
                    port = startingPort + i;
                    ArrayList<Address> localPeers = queryPeer(port);
                    if (localPeers != null) {
                        System.out.println("Node " + port + " has " + localPeers.size() + " local peer connections.");
                        graphNodes.add(new GraphNode(port, localPeers));
                    }
                }
                new Graph(graphNodes);
            } else if (args[0].equals("query")) {
                try {
                    port = Integer.parseInt(args[1]);
                    ArrayList<Address> localPeers = queryPeer(port);
                    if (localPeers != null) {
                        System.out.print("Node " + port + " has " + localPeers.size() + " local peer connections. Peers: ");
                        for (Address address : localPeers) {
                            System.out.print(address.getPort() + " ");
                        }
                        System.out.print("\n");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Expected integer or no arguments");
                    System.out.println("Usage: [graph] [query <portNum>]");
                } catch (Exception e) {
                    System.out.println(e);
                }

            }else if(args[0].equals("json")){

//                JsonArrayBuilder jsonNodes = Json.createArrayBuilder();
//                JsonArrayBuilder jsonLinks = Json.createArrayBuilder();
//
//                for (int i = 0; i < numNodes; i++) {
//                    port = startingPort + i;
//                    ArrayList<Address> localPeers = queryPeer(port);
//                    if (localPeers != null) {
//                        jsonNodes.add(Json.createObjectBuilder().add("id", String.valueOf(port)).add("group", 1));
//
//                        for(Address address : localPeers){
//                            jsonLinks.add(Json.createObjectBuilder().add("source", String.valueOf(port)).add("target", String.valueOf(address.getPort())).add("value", 2));
//                        }
//                    }
//                }
//
//                JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
//                jsonObjectBuilder.add("nodes", jsonNodes).add("links", jsonLinks);
//                JsonObject empJsonObject = jsonObjectBuilder.build();
//
//                OutputStream os = new FileOutputStream("graph.json");
//                JsonWriter jsonWriter = Json.createWriter(os);
//                jsonWriter.writeObject(empJsonObject);
//                jsonWriter.close();
            }else if(args[0].equals("trans")){
                port = Integer.parseInt(args[1]);
                submitTransaction(port, args[2]);
                System.out.println("Submitted transaction");
            }else if(args[0].equals("transEx")){
                port = 8000;
                for(int i = 0; i < 10; i++){
                    port = port + i;

                    submitTransaction(port, String.valueOf(i));
                    System.out.println("Submitted transaction");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else{
                System.out.println("Usage: <[graph] [query <portNum>] [trans <portNum> <Transaction String Id>]>");
            }
        }else{
            System.out.println("Usage: <[graph] [query <portNum>] [trans <portNum> <Transaction String Id>]>");
        }
    }

    /**
     * Queries a specified node given its port, assuming localhost
     * @param port
     * @return ArrayList<Address> node's connection list
     */
    private static ArrayList<Address> queryPeer(int port){
        try {
            Socket s = new Socket("localhost", port);
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.QUERY_PEERS);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            ArrayList<?> localPeers = (ArrayList<?>) messageReceived.getMetadata();
            s.close();
            return (ArrayList<Address>) localPeers;
        } catch (IOException | ClassNotFoundException e) {
            //System.out.println("Error occurred");
        }
        return null;
    }

    private static void submitTransaction(int port, String transaction){
        try {
            Socket s = new Socket("localhost", port);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, new Transaction(transaction));
            oout.writeObject(message);
            oout.flush();
            Thread.sleep(2000);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
