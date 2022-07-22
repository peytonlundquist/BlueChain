package node;


import node.communication.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static node.utils.utils.containsAddress;

/**
 * Handles one connection in a separate thread.
 */
public class ServerConnection extends Thread {
    private Socket client;
    private Node node;

    ServerConnection(Socket client, Node node) throws SocketException {
        this.client = client;
        this.node = node;
        setPriority(NORM_PRIORITY - 1);
    }

    public void run() {
        try {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            ObjectInputStream oin = new ObjectInputStream(in);
            Message incomingMessage = (Message) oin.readObject();
            interpretMessage(incomingMessage, oout);
            client.close();
        } catch (IOException e) {
            System.out.println("I/O error " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void interpretMessage(Message incomingMessage, ObjectOutputStream oout) throws IOException {
        switch(incomingMessage.getRequest()){
            case REQUEST_CONNECTION:
                System.out.println("Received: Connection request.");
                Address address = (Address) incomingMessage.getMetadata();

                if(node.getLocalPeers().size() < node.getMaxPeers()){
                    if (!address.equals(node.getAddress()) && !containsAddress(node.getLocalPeers(), address)) {
                        Message outgoingMessage = new Message(Message.Request.ACCEPT_CONNECTION, node.getAddress());
                        oout.writeObject(outgoingMessage);
                        oout.flush();
                        node.establishConnection(address);
                        return;
                    }
                }
                Message outgoingMessage = new Message(Message.Request.REJECT_CONNECTION, node.getAddress());
                oout.writeObject(outgoingMessage);
                oout.flush();

            case REQUEST_BLOCK:
            case ADD_BLOCK:
        }
    }
}