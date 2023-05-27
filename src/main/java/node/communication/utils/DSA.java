package node.communication.utils;

import node.blockchain.Block;
import node.communication.Address;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class DSA {
    /* Used https://www.javatpoint.com/java-digital-signature */
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
        String path = "./src/main/java/node/nodeRegistry/";
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

    public static byte[] signHash(String hash, PrivateKey key) {
        try {
            byte[] hashBytes = hash.getBytes();
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(key);
            dsa.update(hashBytes);
            return dsa.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(String hash, byte[] signature, byte[] publicKey){
        try {
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pubKey);
            byte[] hashBytes = hash.getBytes();
            sig.update(hashBytes);
            return sig.verify(signature);
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException |
                 InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignatureFromRegistry(String hash, byte[] signature, Address address){
        try {
            String path = "./src/main/java/node/nodeRegistry/";
            String file = path + address.getHost() + "_" + String.valueOf(address.getPort()) + ".txt";
            FileInputStream keyfis = new FileInputStream(file);
            byte[] encKey = new byte[keyfis.available()];  //byte array converted into the encoded public key bytes
            keyfis.read(encKey);
            keyfis.close();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pubKey);

            byte[] hashBytes = hash.getBytes();
            sig.update(hashBytes);
            return sig.verify(signature);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException |
                 InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] stringToBytes(String byteString){
        return Base64.getDecoder().decode(byteString);
    }

    public static String bytesToString(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }
}
