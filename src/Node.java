
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.ArrayList;

public class Node implements NodeInterface {
    private ServerSocket ss;
    private ArrayList<Block> blockchain;
    private ArrayList<Address> globalPeers;
    private ArrayList<Address> localPeers;
    private Address myAddress;
    private Object lock1 = new Object();
    private Object lock2 = new Object();
    private final int MAX_PEERS;

    @Override
    public void addBlock() {

    }

    @Override
    public boolean validateBlock() {
        return false;
    }

    public void searchForPeers(){

    }

    public Node(int port, int MAX_PEERS) {
        blockchain = new ArrayList<Block>();
        myAddress = new Address(port, "localhost");
        this.localPeers = new ArrayList<Address>();
        this.MAX_PEERS = MAX_PEERS;
        try {
            ss = new ServerSocket(port);
            System.out.println("Node up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void runNode(ArrayList<Address> globalPeers) {
        Socket client;
        this.globalPeers = globalPeers;
        System.out.println("ClientConnection Started");
        this.requestConnections();
        System.out.println("ServerConnection Started");
        try {
            while (true) {
                client = ss.accept();
                System.out.println("Received connect from " + client.getInetAddress().getHostName() + " [ "
                        + client.getInetAddress().getHostAddress() + " ] " + client.getPort());
                new ServerConnection(client, this).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public int getMaxPeers(){
        return this.MAX_PEERS;
    }

    public Address getAddress(){
        return this.myAddress;
    }

    public ArrayList<Address> getLocalPeers(){
        synchronized (lock2){
            return this.localPeers;
        }
    }

    public void establishConnection(Address address){
        synchronized(lock1) {
            if (localPeers.size() < MAX_PEERS && !localPeers.contains(address)) {
                localPeers.add(address);
                System.out.println("Added peer: " + address.getPort());

            }
        }
    }

    @Override
    public void requestConnections(){
        try {
            if(globalPeers.size() > 0){
                ClientConnection connect = new ClientConnection(this, globalPeers);
                connect.start();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}

