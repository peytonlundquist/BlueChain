package node.communication;

import java.io.Serializable;

public class Message implements Serializable {
    private Request request;
    private Object metadata;

    public Message(Request request, Object metadata){
        this.request = request;
        this.metadata = metadata;
    }

    public Message(Request request){
        this.request = request;
    }

    public Message(Object metadata){
        this.metadata = metadata;
    }

    public enum Request{
        ADD_BLOCK, REQUEST_BLOCK, REQUEST_CONNECTION, ACCEPT_CONNECTION, REJECT_CONNECTION, QUERY_PEERS, PING
    }

    public Request getRequest(){
        return this.request;
    }

    public Object getMetadata(){
        return this.metadata;
    }
}
