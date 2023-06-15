package node;

import java.io.IOException;
import java.net.*;

import node.blockchain.Block;
import node.blockchain.Transaction;

public class Logger {
    
    Node node; 
    InetAddress ip; 
    int debug; 

    public Logger(Node node) {
        this.node = node; 
    }


    public void printPort(int port) {
        try {
            System.out.println("Node up and running on port " +  port + " " +  InetAddress.getLocalHost());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println(e); 
        } 
    }

    public void printTransactionVerify(Transaction transaction) {
        System.out.println("Node " + node.getAddress().getPort() + ": verifyTransaction: " + 
            transaction.getUID() + ", blockchain size: " + node.getBlockchain().size());
    }

    public void printDupTransaction(Transaction transaction, Block block) {
        System.out.println("Node " + node.getAddress().getPort() + ": trans :" + transaction.getUID() + " found in prev block " + block.getBlockId());
    }

    public void printDefiTxInvalid() {
        System.out.println("Node " + node.getAddress().getPort() + "Transaction not valid");
    }



























































}