package node.communication;

import node.Node;
import node.blockchain.Block;
import node.blockchain.BlockSkeleton;
import node.blockchain.Transaction;
import node.blockchain.defi.DefiTransaction;
import node.communication.*;
import node.communication.messaging.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Deterministic thread which implements the node's protocol
 */
public class ServerConnection extends Thread {
    private final Socket client;
    private final Node node;

    public ServerConnection(Socket client, Node node) throws SocketException {
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
            handleRequest(incomingMessage, oout, oin);
            // client.close();
        } catch (IOException e) {
            System.out.println(node.getAddress().getPort() + ": IO Error. Port exhausted likely. " + e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleRequest(Message incomingMessage, ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
        Message outgoingMessage;
        switch(incomingMessage.getRequest()){
            case REQUEST_CONNECTION:
                Address address = (Address) incomingMessage.getMetadata();
                if (node.eligibleConnection(address, true)) {
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
                node.receiveMempool(memPoolHashes, oout, oin);
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
                node.alertWallet((String) data[0], (Address) data[1]);
                break;
            case REQUEST_CALCULATION:
                String hash = (String) incomingMessage.getMetadata();
                node.calculateEligibity(hash, oout, oin);
                break;
        }
    }
}