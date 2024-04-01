package communication;

import node.Node;
import utils.Address;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import blockchain.Block;
import blockchain.BlockSignature;
import blockchain.BlockSkeleton;
import blockchain.Transaction;
import blockchain.TransactionValidator;
import blockchain.usecases.defi.DefiTransaction;
import blockchain.usecases.defi.DefiTransactionValidator;
import blockchain.usecases.healthcare.HCTransactionValidator;
import communication.*;
import communication.messaging.Message;

/**
 * Deterministic thread which implements the node's protocol.
 * Handles communication with a connected client, processing incoming messages
 * and responding accordingly based on the node's protocol.
 */
public class ServerConnection extends Thread {
    private final Socket client;
    private final Node node;
    private TransactionValidator tv;

    /**
     * Constructs a new ServerConnection instance.
     *
     * @param client The Socket representing the connected client.
     * @param node The Node instance associated with this connection.
     * @param tv The TransactionValidator instance for handling transactions.
     * @throws SocketException If an error occurs while creating the thread.
     */
    public ServerConnection(Socket client, Node node, TransactionValidator tv) throws SocketException {
        this.client = client;
        this.node = node;
        this.tv = tv;
        setPriority(NORM_PRIORITY - 1);
    }

    /**
     * Runs the ServerConnection thread. Reads incoming messages, processes them,
     * and handles the communication protocol with the connected client.
     */
    public void run() {
        try {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            ObjectInputStream oin = new ObjectInputStream(in);
            Message incomingMessage = (Message) oin.readObject();
            handleRequest(incomingMessage, oout, oin);
            // client.close();
        } catch (IOException e) {
            System.out.println(node.getAddress().getPort() + ": IO Error. Port exhausted likely. " + e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the incoming message based on the node's protocol.
     *
     * @param incomingMessage The incoming message from the connected client.
     * @param oout The ObjectOutputStream for sending responses to the client.
     * @param oin The ObjectInputStream for reading additional data from the client.
     * @throws IOException If an I/O error occurs during message handling.
     */
    public void handleRequest(Message incomingMessage, ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
        Message outgoingMessage;
        switch(incomingMessage.getRequest()){
            case REQUEST_CONNECTION:
                Address address = (Address) incomingMessage.getMetadata();
                if (Utils.eligibleConnection(node, address, true)) {
                    outgoingMessage = new Message(Message.Request.ACCEPT_CONNECTION, node.getAddress());
                    oout.writeObject(outgoingMessage);
                    oout.flush();
                    return;
                }
                outgoingMessage = new Message(Message.Request.REJECT_CONNECTION, node.getAddress());
                oout.writeObject(outgoingMessage);
                oout.flush();
                break;
            case QUERY_PEERS:
                outgoingMessage = new Message(node.getLocalPeers());
                oout.writeObject(outgoingMessage);
                oout.flush();
                break;
            case ADD_BLOCK:
                Block proposedBlock = (Block) incomingMessage.getMetadata();
                node.addBlock(proposedBlock);
            case PING:
                outgoingMessage = new Message(Message.Request.PING);
                oout.writeObject(outgoingMessage);
                oout.flush();
                break;
            case ADD_TRANSACTION:
                Transaction transaction = (Transaction) incomingMessage.getMetadata();
                node.addTransaction(transaction);
                break;
            case RECEIVE_MEMPOOL:
                Set<String> memPoolHashes = (HashSet<String>) incomingMessage.getMetadata();
                node.receiveMempoolHashes(memPoolHashes, oout, oin);
                break;
            case QUORUM_READY:
                node.receiveQuorumReady(oout, oin);
                break;
            case RECEIVE_SIGNATURE:
                BlockSignature blockSignature = (BlockSignature) incomingMessage.getMetadata();
                node.receiveQuorumSignature(blockSignature);
                break;
            case RECEIVE_SKELETON:
                BlockSkeleton blockSkeleton = (BlockSkeleton) incomingMessage.getMetadata();
                node.receiveSkeleton(blockSkeleton);
                break;
            case ALERT_WALLET:
                Object[] data = (Object[]) incomingMessage.getMetadata();
                DefiTransactionValidator dtv = (DefiTransactionValidator) tv;
                dtv.addAccountsToAlert((String) data[0], (Address) data[1]);
                break;
            case ALERT_HC_CLIENTS:
                System.out.println("Message recieved");
                Object mData = (Object) incomingMessage.getMetadata();
                HCTransactionValidator hctv = (HCTransactionValidator) tv;
                hctv.addClientsToAlert((Address) mData);
                break;
            case REQUEST_LEDGER:
                Object mData2 = (Object) incomingMessage.getMetadata();
                node.getAllTransactions((Address) mData2);
                break;
        }
    }
}