package node.blockchain.PRISM;
import java.security.KeyPair;
import java.util.ArrayList;

import node.blockchain.PRISM.RecordTypes.Project;

/**
 * An account for the Defi Client
 */
public class Account {
    private String nickname;
    private KeyPair keyPair;
    private ArrayList<Project> projects;

    public Account(String nickname, KeyPair keypair) {
        this.nickname = nickname;
        this.keyPair = keypair;
        this.projects = new ArrayList<Project>();
    }

    public void addProject(Project project) {
        projects.add(project);
    }



    public String getNickname() {
        return nickname;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    

    public String toString() {
        return nickname;
    }
}
