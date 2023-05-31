package node.communication.messaging;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessagerPack {
    private ObjectOutputStream oout;
    private ObjectInputStream oin;
    private Message message;
    private Socket socket;
    
    public MessagerPack(ObjectOutputStream oout, ObjectInputStream oin, Socket socket, Message message){
        this.oout = oout;
        this.oin = oin;
        this.message = message;
        this.socket = socket;
    }

    public ObjectOutputStream getOout(){
        return oout;
    }

    public ObjectInputStream getOin(){
        return oin;
    }

    public Message getMessage(){
        return message;
    }

    public Socket getSocket(){
        return socket;
    }
}
