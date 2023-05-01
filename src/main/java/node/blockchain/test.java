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
        String myHash = Hashing.getSHAString(myTransaction.getUID());

        if(!hashList.get(0).equals(myHash) && !hashList.get(1).equals(myHash)){
            System.out.println("my hash not where expected");
        }


        String hash1 = hashList.get(0);
        String hash2 = hashList.get(1);    
        String growingHash = Hashing.getSHAString(hash1 + hash2);

        for(int i = 2; i < hashList.size(); i++){
            String hash = hashList.get(i);
            growingHash = Hashing.getSHAString(growingHash + hash);

            Hashing.getSHAString(hash1 + hash2);
        }
    }
}
