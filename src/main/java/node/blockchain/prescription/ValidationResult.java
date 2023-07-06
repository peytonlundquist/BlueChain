package node.blockchain.prescription;

public class ValidationResult {
    private boolean isValid;
    private int algorithmSeed;

    public ValidationResult(boolean isValid, int algorithmSeed) {
        this.isValid = isValid;
        this.algorithmSeed = algorithmSeed;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getAlgorithmSeed() {
        return algorithmSeed;
    }
}