package node;

import node.communication.Address;
import node.communication.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Node represents a peer, a cooperating member within the network
 */
public class Node  {
    private final int MAX_PEERS;
    private final int INITIAL_CONNECTIONS;
    private final Object lock;
    private final Address myAddress;
    private ServerSocket ss;
    private ArrayList<Address> localPeers;

    /* A collection of getters */
    public int getMaxPeers(){return this.MAX_PEERS;}
    public int getInitialConnections(){return this.INITIAL_CONNECTIONS;}
    public Address getAddress(){return this.myAddress;}
    public ArrayList<Address> getLocalPeers(){synchronized (lock){return this.localPeers;}}

    /**
     * Node constructor creates node and begins server socket to accept connections
     *
     * @param port Port
     * @param maxPeers Maximum amount of peer connections to maintain
     * @param initialConnections How many nodes we want to attempt to connect to on start
     */
    public Node(int port, int maxPeers, int initialConnections) {
        lock =  new Object();
        myAddress = new Address(port, "localhost");
        this.localPeers = new ArrayList<>();
        this.INITIAL_CONNECTIONS = initialConnections;
        this.MAX_PEERS = maxPeers;
        try {
            ss = new ServerSocket(port);
            Acceptor acceptor = new Acceptor(this);
            acceptor.start();
            System.out.println("node.Node up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * If eligible, add a connection to our dynamic list of peers to speak with
     * @param address
     */
    public void establishConnection(Address address){
        synchronized(lock) {
            if (localPeers.size() < MAX_PEERS && !containsAddress(localPeers, address)) {
                localPeers.add(address);
                System.out.println("Node " + this.getAddress().getPort() + ": Added peer: " + address.getPort());

            }
        }
    }

    public Message sendMessage(Message message, Address address, boolean expectReply){
        try{
            Thread.sleep(10000);
            if(localPeers.size() > 0){
                Socket s = new Socket("localhost", localPeers.get(0).getPort());
                InputStream in = s.getInputStream();
                ObjectInputStream oin = new ObjectInputStream(in);
                OutputStream out = s.getOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                Message outgoingMessage = message;
                oout.writeObject(outgoingMessage);
                oout.flush();
                Message messageReceived = (Message) oin.readObject();
                s.close();
                return messageReceived;
            }

        }catch(IOException e){
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

//    public String stringLocalPeers(ArrayList<Address> peerList){
//        String peers = "";
//        for(Address address : peerList){
//            peers = peers.concat()
//        }
//        return peers;
//    }

    public void queryPeers(){
        try{
            Thread.sleep(10000);
            if(localPeers.size() > 0){
                Socket s = new Socket("localhost", localPeers.get(0).getPort());
                InputStream in = s.getInputStream();
                ObjectInputStream oin = new ObjectInputStream(in);
                OutputStream out = s.getOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                Message message = new Message(Message.Request.QUERY_PEERS);
                oout.writeObject(message);
                oout.flush();
                Message messageReceived = (Message) oin.readObject();
                ArrayList<Address> localPeers = (ArrayList<Address>) messageReceived.getMetadata();
                System.out.println("Node " + this.getAddress().getPort() + ": Node " + localPeers.get(0).getPort() + " has " + localPeers.size() + " local peer connections.");
                s.close();
            }

        }catch(IOException e){
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterate through a list of peers and attempt to establish a mutual connection
     * with a specified amount of nodes
     * @param globalPeers
     */
    public void requestConnections(ArrayList<Address> globalPeers){
        try {
            if(globalPeers.size() > 0){
                ClientConnection connect = new ClientConnection(this, globalPeers);
                connect.start();
                queryPeers();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the provided address is in the list, otherwise false
     * @param list
     * @param address
     * @return
     */
    public boolean containsAddress(ArrayList<Address> list, Address address){
        synchronized(lock) {
            for (Address existingAddress : list) {
                if (existingAddress.equals(address)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Acceptor is a thread responsible for maintaining the server socket by
     * accepting incoming connection requests, and starting a new ServerConnection
     * thread for each request. Requests terminate in a finite amount of steps, so
     * threads return upon completion.
     */
    class Acceptor extends Thread {
        Node node;

        Acceptor(Node node){
            this.node = node;
        }

        public void run() {
            Socket client;
            while (true) {
                try {
                    client = ss.accept();
                    new ServerConnection(client, node).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

