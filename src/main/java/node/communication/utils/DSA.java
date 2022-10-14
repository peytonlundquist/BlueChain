package node.communication.utils;

import node.communication.Address;

import java.io.*;
import java.security.*;

public class DSA {
    public static KeyPair generateDSAKeyPair(){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            return pair;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writePubKeyToRegistry(Address myAddress, PublicKey key){
        String path = ".\\src\\main\\java\\node\\nodeRegistry\\";
        String file = path + myAddress.getHost() + "_" + String.valueOf(myAddress.getPort()) + ".txt";
        byte[] keyBytes = key.getEncoded();
        try {
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(keyBytes);
            fout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void signFile(PrivateKey k){
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(k);
            /* Update and sign the data */
            FileInputStream fis = new FileInputStream("F:\\Digital Signature Demo\\digital.txt");
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0)
            {
                len = bufin.read(buffer);
                dsa.update(buffer, 0, len);
            };
            bufin.close();
            dsa.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
