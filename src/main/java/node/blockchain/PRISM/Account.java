package node.blockchain.PRISM;
import java.security.KeyPair;
import java.util.ArrayList;

import node.blockchain.PRISM.TransactionTypes.Project;

/**
 * An account for the Defi Client
 */
public class Account {
    private String nickname;
    private KeyPair keyPair;
    private int balance;
    private ArrayList<Project> projects;

    public Account(String nickname, KeyPair keypair) {
        this.nickname = nickname;
        this.keyPair = keypair;
        this.projects = new ArrayList<Project>();
        balance = 0;
    }

    public void addProject(Project project) {
        projects.add(project);
    }

    public void updateBalance(int change) {
        balance += change;
    }

    public String getNickname() {
        return nickname;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public int getBalance() {
        return balance;
    }

    public String toString() {
        return nickname + ": " + balance;
    }
}
