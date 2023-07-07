package node.blockchain.prescription.Events;

import java.util.Random;

import node.blockchain.prescription.Event;
import node.blockchain.prescription.PtTransaction;

public class Algorithm extends Event{

    private int algorithmSeed;

    public int getAlgorithmSeed() {
        return algorithmSeed;
    }

    public Algorithm(int algorithmSeed) {
        super(Action.Algorithm);
        this.algorithmSeed = algorithmSeed;
    }

    public boolean runAlgorithm(PtTransaction transaction){
        Random random = new Random(algorithmSeed);
        int flip = random.nextInt(2);

        if(flip == 0){
            return true;
        }

        return false;
    }
}