package node.utils;

import node.blockchain.Block;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hashing {

    //toHexString(getSHA(s3))
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static String getSHAString(String input) throws NoSuchAlgorithmException {
        return toHexString(getSHA(input));
    }

    public static String getBlockHash(Block block) throws NoSuchAlgorithmException {
        String blockString = block.getPrevBlockHash().concat(String.valueOf(block.getBlockId()));
        return getSHAString(blockString);
    }
}
