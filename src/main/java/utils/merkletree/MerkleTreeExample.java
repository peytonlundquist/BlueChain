package utils.merkletree;

import java.util.ArrayList;

import blockchain.Transaction;
import blockchain.usecases.defi.DefiTransaction;
import utils.Hashing;

public class MerkleTreeExample {
    public static void main(String[] args) {
        ArrayList<Transaction> txList = new ArrayList<>();
        Transaction myTransaction = new DefiTransaction("me", "you", 5, null);

        txList.add(new DefiTransaction("me", "you", 1, null));
        txList.add(new DefiTransaction("me", "you", 2, null));
        txList.add(new DefiTransaction("me", "you", 3, null));
        txList.add(new DefiTransaction("me", "you", 4, null));

        txList.add(myTransaction); // If uncommented, membership should be true, otherwise false

        txList.add(new DefiTransaction("me", "you", 6, null));
        txList.add(new DefiTransaction("me", "you", 7, null));
        txList.add(new DefiTransaction("me", "you", 8, null));
        txList.add(new DefiTransaction("me", "you", 9, null));

        MerkleTree mt = new MerkleTree(txList);
        mt.printTree();

        MerkleTreeProof mtp = mt.getProof(myTransaction);

        if(mtp != null && mtp.confirmMembership()){
            System.out.println("Membership confirmed.");
        }else{
            System.out.println("Membership NOT confirmed.");

        }
    }
}
