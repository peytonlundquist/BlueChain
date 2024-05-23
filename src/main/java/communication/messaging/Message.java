package communication.messaging;

import java.io.Serializable;

/**
 * Represents a message exchanged between nodes in a network.
 */
public class Message implements Serializable {
    private Request request;
    private Object metadata;

    /**
     * Constructs a Message object with a specified request type and metadata.
     *
     * @param request The type of request associated with the message.
     * @param metadata The metadata associated with the message.
     */
    public Message(Request request, Object metadata){
        this.request = request;
        this.metadata = metadata;
    }

    /**
     * Constructs a Message object with a specified request type.
     *
     * @param request The type of request associated with the message.
     */
    public Message(Request request){
        this.request = request;
    }

    /**
     * Constructs a Message object with specified metadata.
     *
     * @param metadata The metadata associated with the message.
     */
    public Message(Object metadata){
        this.metadata = metadata;
    }

    /**
     * Enumeration representing different types of requests that a message can convey.
     */
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
        REQUEST_TX,
        SEND_TX, 
        INCREMENT_NONCE,
        RESET_VOTE
    }

    /**
     * Gets the type of request associated with the message.
     *
     * @return The request type.
     */
    public Request getRequest(){
        return this.request;
    }

    /**
     * Gets the metadata associated with the message.
     *
     * @return The metadata.
     */
    public Object getMetadata(){
        return this.metadata;
    }
}
