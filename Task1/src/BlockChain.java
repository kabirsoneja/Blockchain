/**
 * Author: Kabir Soneja
 * Andrew ID: ksoneja
 * Last Modified: March 05, 2020
 * This program demonstrates a simple block chain.
 */

import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

public class BlockChain {

    List<Block> blockchain;                                                             //Array List to store all the blocks in the block chain.
    public String chainHash;                                                          //Variable to store the chain hash

    BlockChain(){                                                                       //Constructor to initialize the chain
        blockchain = new ArrayList<>();
        this.chainHash = "" ;
    }

    /**
     * Menu driven main method.
     * It Creates the genesis block and adds it to the chain
     * Takes the user option and performs the requested task
     * Displays the result of the task
     */
    public static void main(String args[]) {

        BlockChain blockChain = new BlockChain();
        Block block = new Block(0, blockChain.getTime(),"Genesis",2);                 //Creation of genesis block
        blockChain.addBlock(block);                                                                         //Adding the block to the blockchain
        Scanner sc = new Scanner(System.in);
        int index = 1;
        while (true) {
            //Menu driven options
            System.out.println(" ");
            System.out.println("0. View basic blockchain status.");
            System.out.println("1. Add a transaction to blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain");
            System.out.println("5. Hide the corruption by recomputing hashes.");
            System.out.println("6. Exit");
            int input = Integer.parseInt(sc.nextLine());

            if (input == 0) {                                                                       //View the blockchain
                System.out.println("Current Size of chain: "+ blockChain.getChainSize());
                System.out.println("Current hashes per second by this machine: "+blockChain.hashesPerSecond());
                System.out.println("Difficulty of most recent block: "+blockChain.getLatestBlock().getDifficulty());
                System.out.println("Nonce for most recent block: "+ blockChain.getLatestBlock().getNonce());
                System.out.println("Chain hash: "+ blockChain.chainHash);
            }
            else if (input == 1) {                                                                  //Add a transaction
                System.out.println("Enter difficulty > 0");                                         //Difficulty input
                int diff = Integer.parseInt(sc.nextLine());
                System.out.println("Enter Transaction");                                            //Input data
                String transaction = sc.nextLine();
                long start = System.currentTimeMillis();                                            //Start time
                Block newblock = new Block(index,block.getTimestamp(),transaction,diff);
                blockChain.addBlock(newblock);
                long end = System.currentTimeMillis();                                              //End time
                System.out.println("Total execution time to add this block was " + (end - start) + " milliseconds");
            }
            else if (input == 2) {                                                                  //Blockcahin verification
                System.out.println("Verifying entire chain ");
                long start = System.currentTimeMillis();                                            //Verification start time
                System.out.println("Chain verification: "+ blockChain.isChainValid());
                long end = System.currentTimeMillis();                                              //Verification end time
                System.out.println("Total execution time required to verify the chain was "+ (end-start) +" milliseconds");
            }
            else if (input == 3) {                                                                  //Blockchain view
                System.out.println(blockChain.toString());
            }
            else if (input == 4) {                                                                   //Corrupt the blockchain
                System.out.println("Corrupt the Blockchain");
                System.out.println("Enter the block ID of block to Corrupt");                           //ID of block to be corrupted
                int corrupt_id = Integer.parseInt(sc.nextLine());
                System.out.println("Enter the new data for block " + corrupt_id);                       //Corrupt data to be added
                String corrupt_data = sc.nextLine();
                Block corrupt_block = blockChain.blockchain.get(corrupt_id);
                corrupt_block.setData(corrupt_data);
                System.out.println("Block " + corrupt_id + " now holds " + corrupt_data);
            }
            else if (input == 5) {                                                                  //Blockchain repair
                System.out.println("Repairing the entire chain");
                long start = System.currentTimeMillis();                                             //Repair start time
                blockChain.repairChain();
                long end = System.currentTimeMillis();                                               //Repair end time
                System.out.println("Total execution time to repair the chain was " + (end - start) + " milliseconds");
            }
            else if (input == 6) {                                                                   //Exit
                System.exit(0);
            }

        }

    }

    /**
     * Getter for timestamp.
     */
    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Getter for latest block.
     */
    public Block getLatestBlock() {
        return blockchain.get(blockchain.size()-1);
    }

    /**
     * Getter for chainsize.
     */
    public int getChainSize() {
        return blockchain.size();
    }
    /**
     * Method to Calculate the hashes per second.
     */
    public int hashesPerSecond() {
        String message = "00000000";
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        int count = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");                                //SHA-256 Hash calculation
            while(endTime - startTime < 1000)
            {
                byte[] byteHash = md.digest(message.getBytes("UTF-8"));
                endTime = System.currentTimeMillis();
                count++;
            }

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        int result = (int) (endTime - startTime);
        result = count/result;
        return result;                                                                              //Time calculation
    }

    /**
     * Utility Method to support hashes per second.
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
     * Method to add a new block to the chain.
     */
    public void addBlock(Block newBlock) {
        newBlock.setPreviousHash(chainHash);                                                //Setting the previous hash of the nextblock
        String pow = newBlock.proofOfWork();                                                //Calculating proof of work
        blockchain.add(newBlock);                                                           //Adding the new block to the chain
        this.chainHash = pow;                                                               //Updating the chainhash
    }

    /**
     * Method to check the validity of the chain.
     */
    public boolean isChainValid() {
        Block currentBlock;
        Block nextBlock;

        if (blockchain.size() == 1) {                                                   //Check validity if there is only one block in the chain
            currentBlock = blockchain.get(0);
            String currentHash = currentBlock.calculateHash();                          //Calculating the hash

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < currentBlock.getDifficulty(); i++){
                sb.append("0");
            }

            boolean noncefound = currentHash.substring(0, currentBlock.getDifficulty()).equals(sb.toString());      //Calculating the zeros
            if (noncefound == true) {
                if(chainHash.equals(currentHash)) {                                                                 //Checking current hash and chainhash
                    return true;
                }
                else {
                    System.out.println("Improper hash on node "+ 0 +" Does not begin with "+sb.toString());
                    return false;
                }
            }
            else {
                System.out.println("Improper hash on node "+ 0 +" Does not begin with "+sb.toString());
                return false;
            }

        }
        else {                                                                                                //Check validity if more than one block is there in the chain
            for (int i = 0; i < blockchain.size()-1; i++) {
                currentBlock = blockchain.get(i);
                nextBlock = blockchain.get(i+1);
                String currentHash = currentBlock.calculateHash();

                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < currentBlock.getDifficulty(); j++){
                    sb.append("0");
                }

                boolean noncefound = currentHash.substring(0, currentBlock.getDifficulty()).equals(sb.toString());      //checking the nonce found
                if (noncefound == true) {
                    if (!nextBlock.getPreviousHash().equals(currentHash)) {
                        System.out.println("Improper hash on node "+ i +" Does not begin with "+sb.toString());
                        return false;
                    }

                }
                else {
                    System.out.println("Improper hash on node "+ i +" Does not begin with "+sb.toString());
                    return false;
                }
            }

            //Validity for the last block in the chain
            Block lastBlock = blockchain.get(blockchain.size()-1);
            String lastHash = lastBlock.calculateHash();

            StringBuilder sb = new StringBuilder();
            for(int j = 0; j < lastBlock.getDifficulty(); j++){
                sb.append("0");
            }

            boolean noncefound = lastHash.substring(0, lastBlock.getDifficulty()).equals(sb.toString());
            if (noncefound == true) {
                if (!this.chainHash.equals(lastHash)) {
                    System.out.println("Improper hash on node "+ (blockchain.size()-1) +" Does not begin with "+sb.toString());
                    return false;
                }
            }
            else {
                System.out.println("Verification 6 failed");
                return false;
            }
        }
        return true;
    }

    /**
     * Method to repair the chain.
     */
    public void repairChain() {

        int startRepair = -1;                                                               //Block id to start the repair
        Block currentBlock;
        Block nextBlock;

        if (blockchain.size() == 1) {                                                       //Checking for first block
            currentBlock = blockchain.get(0);
            String currentHash = currentBlock.calculateHash();

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < currentBlock.getDifficulty(); i++){
                sb.append("0");
            }

            boolean noncefound = currentHash.substring(0, currentBlock.getDifficulty()).equals(sb.toString());
            if (noncefound == true) {
                if(!chainHash.equals(currentHash)) {
                    startRepair = 0;
                }
            }
            else {
                startRepair = 0;
            }
        }

        else {                                                                                          //Checking if there are more than one blocks in the chain
            for (int i = 0; i < blockchain.size()-1; i++) {
                currentBlock = blockchain.get(i);
                nextBlock = blockchain.get(i+1);
                String currentHash = currentBlock.calculateHash();

                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < currentBlock.getDifficulty(); j++){
                    sb.append("0");
                }

                boolean noncefound = currentHash.substring(0, currentBlock.getDifficulty()).equals(sb.toString());
                if (noncefound == true) {
                    if (!nextBlock.getPreviousHash().equals(currentHash)) {
                        startRepair = i;
                        break;
                    }
                }
                else {
                    startRepair = i;
                    break;
                }
            }

            //Validity for the last block
            Block lastBlock = blockchain.get(blockchain.size()-1);
            String lastHash = lastBlock.calculateHash();

            StringBuilder sb = new StringBuilder();
            for(int j = 0; j < lastBlock.getDifficulty(); j++){
                sb.append("0");
            }

            boolean noncefound = lastHash.substring(0, lastBlock.getDifficulty()).equals(sb.toString());
            if (noncefound == true) {
                if (!this.chainHash.equals(lastHash)) {
                    startRepair = blockchain.size()-1;
                }
            }
            else {
                startRepair = blockchain.size()-1;
            }
        }

        for (int i = startRepair; i < blockchain.size()-1; i++) {                       //Rebuilding the entire chain
            String repairPOW = blockchain.get(i).proofOfWork();                         //Re calculating the proof of work
            blockchain.get(i+1).setPreviousHash(repairPOW);                             //Re setting the previous hash
        }

        String LastRepairPOW = blockchain.get(blockchain.size()-1).proofOfWork();
        chainHash = LastRepairPOW;                                                      //Updating the chainhash

    }
    /**
     * Method to convert block chain to string.
     */
    public java.lang.String toString() {

        StringBuilder sb = new StringBuilder("[");
        Map<String, String> map = new HashMap<>();                                      //Map to store the values
        for (int i = 0; i < blockchain.size()-1; i++) {
            sb.append(blockchain.get(i).toString());
            sb.append(",");
        }
        sb.append(blockchain.get(blockchain.size()-1));
        sb.append("]");
        map.put("ds_chain",sb.toString());
        map.put("chainHash",this.chainHash);

        String json = new JSONObject(map).toString();                                  //Converting json object to string
        String temp = json.replaceAll("\\\\","");
        return temp;
    }
}