package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blockchain.Block;

/**
 * The Hashing class provides utility methods for creating SHA-256 hashes and generating block hashes.
 */
public class Hashing {

    /**
     * Computes the SHA-256 hash of the given input string.
     *
     * @param input The input string to be hashed.
     * @return The SHA-256 hash as a byte array.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available.
     */
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param hash The byte array representing the hash.
     * @return The hexadecimal representation of the hash.
     */
    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    /**
     * Computes the SHA-256 hash of the given input string and returns it as a hexadecimal string.
     *
     * @param input The input string to be hashed.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    public static String getSHAString(String input) {
        try {
            return toHexString(getSHA(input));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Computes the hash of a block based on its transactions, previous block hash, block ID, and nonce.
     *
     * @param block The block for which to compute the hash.
     * @param nonce The nonce value used in the mining process.
     * @return The SHA-256 hash of the block.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available.
     */
    public static String getBlockHash(Block block, int nonce) throws NoSuchAlgorithmException {
        List<String> txList = new ArrayList<>(block.getTxList().keySet());
        Collections.sort(txList);
        String txString = "";
        for(String key : txList){
            txString = txString.concat(key);
        }
        String blockString = block.getPrevBlockHash().concat(String.valueOf(block.getBlockId())).concat(String.valueOf(nonce).concat(txString));
        return getSHAString(blockString);
    }
}
