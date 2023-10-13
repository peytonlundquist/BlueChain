package communication.messaging;

import java.io.*;
import java.net.Socket;

import utils.Address;

public class Messager{
    
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
            System.out.println("Node " + myAddress.getPort() + ": sendOneWayMessage: Received IO Exception from node " + address.getPort());
        }
    }

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
     * 
     * @param address
     * @param message
     * @param myAddress
     * @return
     */
    public static MessagerPack sendInterestingMessage(Address address, Message message, Address myAddress) {
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
}