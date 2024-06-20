package communication.messaging;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import utils.Address;

/**
 * Utility class for handling messaging functionalities in a network.
 */
public class Messager{

    /**
     * Sends a one-way message to the specified address.
     *
     * @param address The target address to send the message to.
     * @param message The message to be sent.
     * @param myAddress The sender's address.
     */
    @SuppressWarnings("unused")
    public static void sendOneWayMessage(Address address, Message message, Address myAddress) {
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(message);
            oout.flush();
            s.close();
        } catch (IOException e) {
            System.out.println("Node " + myAddress.getPort() + ": sendOneWayMessage: Received IO Exception from node " + address.getPort() + " Exception " + e);
        }
    }

    /**
     * Sends a two-way message to the specified address and receives a response.
     *
     * @param address The target address to send the message to.
     * @param message The message to be sent.
     * @param myAddress The sender's address.
     * @return The response message received from the target address.
     */
    public static Message sendTwoWayMessage(Address address, Message message, Address myAddress) {
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            s.close();
            return messageReceived;
        } catch (IOException e) {
            System.out.println("Node " + myAddress.getPort() + ": sendTwoWayMessage: Received IO Exception from node " + address.getPort());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Sends a complex message to the specified address and receives a response
     * along with associated streams and socket.
     *
     * @param address The target address to send the message to.
     * @param message The message to be sent.
     * @param myAddress The sender's address.
     * @return A MessagerPack containing the output/input streams, the response message, and the socket.
     */
    public static MessagerPack sendComplexMessage(Address address, Message message, Address myAddress) {
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            return new MessagerPack(oout, oin, s, messageReceived);
        } catch (IOException e) {
            System.out.println("Node " + myAddress.getPort() + ": sendTwoWayMessage: Received IO Exception from node " + address.getPort());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Sends a one-way message to a group of addresses, excluding the sender's address.
     *
     * @param message The message to be sent.
     * @param addresses The list of target addresses.
     * @param myAddress The sender's address.
     */
    public static void sendOneWayMessageToGroup(Message message, ArrayList<Address> addresses, Address myAddress){
        for(Address address : addresses){
            if(!address.equals(myAddress)){
                Messager.sendOneWayMessage(address, message, myAddress);
            }
        }
    }
}