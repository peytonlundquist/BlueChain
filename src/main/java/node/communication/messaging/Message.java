package node.communication.messaging;

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
        ADD_BLOCK,
        REQUEST_BLOCK,
        REQUEST_CONNECTION,
        ACCEPT_CONNECTION,
        REJECT_CONNECTION,
        QUERY_PEERS,
        PING,
        REQUEST_QUORUM_CONNECTION,
        ADD_TRANSACTION,
        RECEIVE_MEMPOOL,
        QUORUM_READY,
        CONSTRUCT_BLOCK,
        VOTE_BLOCK,
        QUORUM_COMPLETE,
        REQUEST_TRANSACTION,
        RECEIVE_SKELETON,
        RECEIVE_SIGNATURE,
        RECONCILE_BLOCK,
        ALERT_WALLET,
        CALCULATION_COMPLETE,
        REQUEST_CALCULATION
    }

    public Request getRequest(){
        return this.request;
    }

    public Object getMetadata(){
        return this.metadata;
    }
}
