package node.blockchain.PRISM;

import node.communication.Address;

public class RepData {

    float currentReputation;
    int blocksParticipated;
    float timeSummation;
    float accurarySummation;
    int accuracyCount;

    public RepData(int blocksParticipated, float timeSummation, float accurarySummation, int accuracyCount) {
        this.currentReputation = 0;
        this.blocksParticipated = blocksParticipated;
        this.timeSummation = timeSummation;
        this.accurarySummation = accurarySummation;
        this.accuracyCount = accuracyCount;
    }
    
    public void addTimeSummation(float time){
        this.timeSummation += time;
    }
    public void addAccuracySummation(float accuracy){
        this.accurarySummation += accuracy;
    }
    public void addBlocksParticipated(){
        this.blocksParticipated++;
    }
    public void addAccuracyCount(){
        this.accuracyCount++;
    }
    public float getCurrentReputation() {
        return currentReputation;
    }
    public void setCurrentReputation(float getCurrentReputation) {
        this.currentReputation = getCurrentReputation;
    }
    public int getBlocksParticipated() {
        return blocksParticipated;
    }
    public void setBlocksParticipated(int blocksParticipated) {
        this.blocksParticipated = blocksParticipated;
    }
    public float getTimeSummation() {
        return timeSummation;
    }
    public void setTimeSummation(float timeSummation) {
        this.timeSummation = timeSummation;
    }
    public float getAccurarySummation() {
        return accurarySummation;
    }
    public void setAccurarySummation(float accurarySummation) {
        this.accurarySummation = accurarySummation;
    }
    public int getAccuracyCount() {
        return accuracyCount;
    }
    public void setAccuracyCount(int accuracyCount) {
        this.accuracyCount = accuracyCount;
    }  
}
