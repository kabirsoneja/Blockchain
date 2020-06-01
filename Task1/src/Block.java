/**
 * Author: Kabir Soneja
 * Andrew ID: ksoneja
 * Last Modified: March 05, 2020
 * This program demonstrates a simple creation of a block in block chain.
 */

import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Block {
    /**
     * Private variables of each block.
     */
    private int index;
    private Timestamp timestamp;
    private String data;
    private int difficulty;
    private String previousHash;
    private BigInteger nonce;

    /**
     * Constructor to intialize a new block.
     */
    Block(int index, Timestamp timestamp, String data, int difficulty) {

        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = new BigInteger("0");
        this.previousHash = "";
    }
    public static void main(String args[]) {

    }

    /**
     * Method to calculate the hash
     */
    public String calculateHash() {
        String message = String.valueOf(index).concat(timestamp.toString()).concat(data).concat(getPreviousHash()).concat(getNonce().toString()).concat(String.valueOf(getDifficulty())); //Values need to be added
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");                                    //SHA-256 Hash algorithm
            byte[] byteHash = md.digest(message.getBytes("UTF-8"));
            String hexValue = javax.xml.bind.DatatypeConverter.printHexBinary(byteHash);
            return hexValue;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Utility Method for calculate the hash.
     * Converts byte array to hex string
     */
    public static String bytesToHex(final byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Getter for Nonce.
     */
    public BigInteger getNonce() {
        return this.nonce;
    }

    /**
     * Method to calculate the proof of work.
     */
    public String proofOfWork() {

        boolean noncefound = false;                                             //boolean variable to check if nonce is found or not
        String  hashresult = "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < this.difficulty; i++){
            sb.append("0");
        }

        while (!noncefound) {
            this.nonce = this.nonce.add(new BigInteger("1"));                           //Updating the nonce
            hashresult = calculateHash();                                                   //Calculating the hash
            noncefound = hashresult.substring(0, this.difficulty).equals(sb.toString());    //Nonce found true or false
        }
        return hashresult;
    }

    /**
     * Getter for difficulty.
     */
    public int getDifficulty() {
        return this.difficulty;
    }

    /**
     * Setter for Difficulty.
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Setter for Previous Hash.
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    /**
     * Getter for Previous Hash.
     */
    public String getPreviousHash() {
        return this.previousHash;
    }
    /**
     * Getter for Index.
     */
    public int getIndex() {
        return this.index;
    }
    /**
     * Setter for Index.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * Setter for TimeStamp.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * Getter for TimeStamp.
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }
    /**
     * Getter for Transaction Data.
     */
    public String getData() {
        return this.data;
    }
    /**
     * Setter for Transaction Data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Method to convert block to string.
     */
    public java.lang.String toString() {

        Map<String, String> map = new HashMap<>();                                              //Map to store all the values of the block
        map.put("index",String.valueOf(this.index));
        map.put("time stamp",this.timestamp.toString());
        map.put("Tx",String.valueOf(this.data));
        map.put("difficulty",String.valueOf(this.difficulty));
        map.put("PrevHash",this.previousHash);
        map.put("nonce",this.nonce.toString());

        String json = new JSONObject(map).toString();                                           //Converting json object to string
        return json;
    }
}
