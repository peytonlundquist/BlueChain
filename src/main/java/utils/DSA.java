package utils;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * The DSA class provides utility methods for generating DSA key pairs, signing and verifying
 * digital signatures, and handling public key operations.
 */
public class DSA {

    /**
     * Generates a DSA key pair.
     *
     * @return The generated DSA key pair.
     * @throws RuntimeException If an error occurs during key pair generation.
     */
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

    /**
     * Writes the public key to a registry file.
     *
     * @param myAddress The address associated with the public key.
     * @param key       The public key to be written to the registry.
     * @throws RuntimeException If an error occurs while writing the public key to the registry.
     */
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

    /**
     * Signs a hash using the provided private key.
     *
     * @param hash The hash to be signed.
     * @param key  The private key used for signing.
     * @return The digital signature.
     * @throws RuntimeException If an error occurs during the signing process.
     */
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

    /**
     * Verifies a digital signature using the provided public key.
     *
     * @param hash       The hash to be verified.
     * @param signature  The digital signature to be verified.
     * @param publicKey  The public key used for verification.
     * @return True if the signature is valid, false otherwise.
     * @throws RuntimeException If an error occurs during the verification process.
     */
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

    /**
     * Verifies a digital signature using the public key obtained from the registry.
     *
     * @param hash    The hash to be verified.
     * @param signature  The digital signature to be verified.
     * @param address The address associated with the public key in the registry.
     * @return True if the signature is valid, false otherwise.
     * @throws RuntimeException If an error occurs during the verification process.
     */
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

    /**
     * Converts a Base64-encoded string to a byte array.
     *
     * @param byteString The Base64-encoded string.
     * @return The corresponding byte array.
     */
    public static byte[] stringToBytes(String byteString){
        return Base64.getDecoder().decode(byteString);
    }

    /**
     * Converts a byte array to a Base64-encoded string.
     *
     * @param bytes The byte array to be encoded.
     * @return The Base64-encoded string.
     */
    public static String bytesToString(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }
}
