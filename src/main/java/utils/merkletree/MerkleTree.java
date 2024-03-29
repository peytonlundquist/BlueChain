package utils.merkletree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import blockchain.Transaction;
import utils.Hashing;

/**
 * Represents a Merkle Tree data structure for a list of transactions.
 */
public class MerkleTree {
    protected Node rootNode;

    /**
     * Constructs a MerkleTree object based on a list of transactions.
     *
     * @param txList The list of transactions to build the Merkle Tree.
     */
    public MerkleTree(ArrayList<Transaction> txList){
        txList.sort(new TransactionComparator());
        LinkedList<Node> nodeQueue = new LinkedList<>();

        /* Initializing Queue */
        for(int i = 0; i < txList.size(); i++){
            String hash = Hashing.getSHAString(txList.get(i).getUID());
            nodeQueue.addLast(new Node(hash, txList.get(i))); // Leaf node uses overloaded constuctor to ref tx
        }

        /* Odd node */
        if(txList.size() % 2 != 0){
            String hash = Hashing.getSHAString(txList.get(txList.size() - 1).getUID());
            nodeQueue.addLast(new Node(hash, txList.get(txList.size() - 1)));
        }

        /* Algorithm to build merkle tree */
        while(!nodeQueue.isEmpty()){
            Node leftNode = nodeQueue.removeFirst();

            if(nodeQueue.isEmpty()){
                // root
                rootNode = leftNode;
                break;
            }

            Node rightNode = nodeQueue.removeFirst();
            Node parentNode = new Node(Hashing.getSHAString(rightNode.getHash() + 
                                                            leftNode.getHash()));

            parentNode.setLeftChild(leftNode);
            parentNode.setRightChild(rightNode);
            leftNode.setParent(parentNode);
            rightNode.setParent(parentNode);
            
            nodeQueue.addLast(parentNode);
        }
    }

    /**
     * Gets the root node of the Merkle Tree.
     *
     * @return The root node.
     */
    public Node getRootNode(){
        return rootNode;
    }
    
    /**
     * Generates a Merkle Tree proof for a specific transaction.
     *
     * @param transaction The transaction for which to generate the proof.
     * @return The Merkle Tree proof.
     */
    public MerkleTreeProof getProof(Transaction transaction){
        LinkedList<Node> nodeQueue = new LinkedList<>();
        nodeQueue.addLast(rootNode);
        boolean foundTransaction = false;

        Node searchNode = null;
        while(!nodeQueue.isEmpty()){
            searchNode = nodeQueue.removeFirst();

            /* If leaf node and has the specefied tx */
            if(searchNode.getTransaction() != null && searchNode.getTransaction().equals(transaction)){
                foundTransaction = true;
                break;
            }

            /* If there are children add them to Q to process */
            if(searchNode.getLeftChild() != null) nodeQueue.addLast(searchNode.getLeftChild());
            if(searchNode.getRightChild() != null) nodeQueue.addLast(searchNode.getRightChild());
        }

        if(foundTransaction == false) return null;

        Node leftNode = searchNode.getParent().getLeftChild();
        Node rightNode = searchNode.getParent().getRightChild();
        ArrayList<String> retHashes = new ArrayList<>();
        retHashes.add("1" + leftNode.getHash()); // Pad 1 lead char. "1" for left
        retHashes.add("0" + rightNode.getHash()); // Pad 1 lead char. "0" for right

        nodeQueue.clear();
        nodeQueue.addLast(searchNode.getParent());
        
        while(!nodeQueue.isEmpty()){
            Node node = nodeQueue.removeFirst();
            Node parentNode = node.getParent();

            if(parentNode == null){
                // This is the root
                break;
            }

            Node complimentNode;
            String rightLeftPad;

            if(parentNode.getLeftChild().equals(node)){
                complimentNode = parentNode.getRightChild(); // I am left need right
                rightLeftPad = "1";
            }else{
                complimentNode = parentNode.getLeftChild();
                rightLeftPad = "0";
            }

            retHashes.add(rightLeftPad + complimentNode.getHash());
            nodeQueue.addLast(parentNode);
        }

        return new MerkleTreeProof(retHashes, transaction, rootNode.getHash());
    } 

    /**
     * Prints the structure of the Merkle Tree.
     */
    public void printTree(){
        LinkedList<Node> nodeQueue = new LinkedList<>();
        nodeQueue.addLast(rootNode);
        
        while(!nodeQueue.isEmpty()){
            Node node = nodeQueue.removeFirst();

            if(node.getParent() == null){
                // this is the root node
                System.out.println(node.getHash().substring(0, 4) + " Root.");
            }else if(node.getTransaction() == null){
                System.out.println(node.getHash().substring(0, 4) + " P:" + node.getParent().getHash().substring(0, 4));
            }else if(node.getLeftChild() == null && node.getRightChild() == null){
                System.out.println(node.getHash().substring(0, 4) + " P:" + node.getParent().getHash().substring(0, 4) 
                + " T:" + node.getTransaction().getUID());
            }

            if(node.getLeftChild() != null) nodeQueue.addLast(node.getLeftChild());
            if(node.getLeftChild() != null) nodeQueue.addLast(node.getRightChild());
        }
    }
    
    /**
     * Comparator for sorting transactions based on their UID hash.
     */
    class TransactionComparator implements Comparator<Transaction> {

        @Override
        public int compare(Transaction arg0, Transaction arg1) {
            return Hashing.getSHAString(arg0.getUID()).compareTo(Hashing.getSHAString(arg1.getUID()));
        }
    }

    /**
     * Represents a node in the Merkle Tree.
     */
    public class Node {
        private String hash;
        private Node parent;
        private Node leftChild;
        private Node rightChild;
        private Transaction transaction;

        public Node(String hash){
            this.hash = hash;
        }

        public Node(String hash, Transaction transaction){
            this.hash = hash;
            this.transaction = transaction;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public String getHash() {
            return hash;
        }

        public Node getParent() {
            return parent;
        }

        public Node getLeftChild() {
            return leftChild;
        }

        public Node getRightChild() {
            return rightChild;
        }

        public void setParent(Node parent){
            this.parent = parent;
        }

        public void setLeftChild(Node leftChild){
            this.leftChild = leftChild;
        }
        
        public void setRightChild(Node rightChild){
            this.rightChild = rightChild;
        }
    }
}
