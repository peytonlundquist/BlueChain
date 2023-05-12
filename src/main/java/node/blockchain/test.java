package node.blockchain;

import java.util.ArrayList;

import node.communication.utils.Hashing;
import node.defi.Transaction;

public class test {
    public static void main(String[] args) {
        ArrayList<Transaction> txList = new ArrayList<>();
        Transaction myTransaction = new Transaction("me", "you", 5, null);

        txList.add(new Transaction("me", "you", 1, null));
        txList.add(new Transaction("me", "you", 2, null));
        txList.add(new Transaction("me", "you", 3, null));
        txList.add(new Transaction("me", "you", 4, null));
        txList.add(myTransaction);
        txList.add(new Transaction("me", "you", 6, null));
        txList.add(new Transaction("me", "you", 7, null));
        txList.add(new Transaction("me", "you", 8, null));
        txList.add(new Transaction("me", "you", 9, null));

        MerkleTree mt = new MerkleTree(txList);
        mt.printTree();

        MerkleTreeProof mtp = mt.getProof(myTransaction);
        ArrayList<String> hashList = mtp.getHashes();
        String rootHash = mtp.getRootHash();

        if(MerkleTreeProof.confirmMembership(hashList, myTransaction, rootHash)){
            System.out.println("Membership confirmed.");
        }else{
            System.out.println("Membership NOT confirmed.");

        }
    }
}
