package communication.messaging;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Container class that holds components related to messaging, facilitating communication
 * between entities in a network
 */
public class MessagerPack {
    private ObjectOutputStream oout;
    private ObjectInputStream oin;
    private Message message;
    private Socket socket;
    
    /**
     * Constructs a new MessagerPack with the specified components.
     *
     * @param oout The ObjectOutputStream associated with the communication.
     * @param oin The ObjectInputStream associated with the communication.
     * @param socket The Socket associated with the communication.
     * @param message The Message object representing the content of the communication.
     */
    public MessagerPack(ObjectOutputStream oout, ObjectInputStream oin, Socket socket, Message message){
        this.oout = oout;
        this.oin = oin;
        this.message = message;
        this.socket = socket;
    }

    /**
     * Gets the ObjectOutputStream associated with this MessagerPack.
     *
     * @return The ObjectOutputStream.
     */
    public ObjectOutputStream getOout(){
        return oout;
    }

    /**
     * Gets the ObjectInputStream associated with this MessagerPack.
     *
     * @return The ObjectInputStream.
     */
    public ObjectInputStream getOin(){
        return oin;
    }

    /**
     * Gets the Message associated with this MessagerPack.
     *
     * @return The Message object representing the content of the communication.
     */
    public Message getMessage(){
        return message;
    }

    /**
     * Gets the Socket associated with this MessagerPack.
     *
     * @return The Socket used for communication.
     */
    public Socket getSocket(){
        return socket;
    }
}
