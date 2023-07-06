import node.blockchain.prescription.Events.FillScript;
import node.blockchain.prescription.Events.Algorithm;
import node.blockchain.prescription.Events.Prescription;
import node.blockchain.prescription.ValidationResult;

public class PtTransactionAuthenticator {
    private Algorithm algorithm;

    public void ptTransactionValidatorAlgorithmSeed(int algorithmSeed) {
        this.algorithm = new Algorithm(algorithmSeed);
    }

    public ValidationResult validate(Object[] objects) {
        PtTransaction transaction = (PtTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Prescription")){
            int algorithmSeed = algorithm.getAlgorithmSeed();
            ptTransactionValidatorAlgorithmSeed(algorithmSeed);
            boolean isValid = algorithm.runAlgorithm(transaction);
            return new ValidationResult(isValid, algorithmSeed);

        }else if (transaction.getEvent().getAction().name().equals("FillScript")){
            int algorithmSeed = algorithm.getAlgorithmSeed();
            ptTransactionValidatorAlgorithmSeed(algorithmSeed);
            boolean isValid = algorithm.runAlgorithm(transaction);
            return new ValidationResult(isValid, algorithmSeed)
        } else if (transaction.getEvent().getAction().name().equals("Algorithm")){
            return new ValidationResult(true, 0); 

        } else {
            return new ValidationResult(false, -1);
        }
      
    }