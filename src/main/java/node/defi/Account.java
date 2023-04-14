package node.defi;

import java.security.Key;
import java.security.KeyPair;

public class Account {
    private String nickname;
    private KeyPair keyPair;
    private int balance;

    public Account(String nickname, KeyPair keypair){
        this.nickname = nickname;
        this.keyPair = keypair;
        balance = 0;
    }

    public void updateBalance(int change){
        balance+=change;
    }

    public String getNickname(){
        return nickname;
    }

    public KeyPair getKeyPair(){
        return keyPair;
    }
}
