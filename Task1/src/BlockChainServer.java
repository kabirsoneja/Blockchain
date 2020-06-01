/**
 * Author: Kabir Soneja
 * Andrew ID: ksoneja
 * Last Modified: March 06, 2020
 *
 * This program demonstrates a very simple TCP Server for blockchain.
 *
 */

import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.JSONObject;
import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BlockChainServer {
    static HashMap<String, Integer> map = new HashMap<>();

    /**
     * Menu driven main method.
     * It Creates the genesis block and adds it to the chain
     * Takes the user option and performs the requested task
     * Displays the result of the task
     */
    public static void main(String args[]) {
        BlockChain blockChain = new BlockChain();
        Block block = new Block(0, blockChain.getTime(),"Genesis",2);       //Creating the genesis block
        blockChain.addBlock(block);
        System.out.println("Server Starting");                                                  //Indicating the start of the server
        Socket clientSocket = null;
        String s = "";
        int index = 1;
        try {
            int serverPort = 7777; // the server port we are using                                //Specifying the server port

            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);

            /*
             * Block waiting for a new connection request from a client.
             * When the request is received, "accept" it, and the rest
             * the tcp protocol handshake will then take place, making
             * the socket ready for reading and writing.
             */
            while (true) {
                clientSocket = listenSocket.accept();                                   //Socket connection accpet
                // If we get here, then we are now connected to a client.

                // Set up "in" to read from the client socket
                Scanner in;
                in = new Scanner(clientSocket.getInputStream());

                // Set up "out" to write to the client socket
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                /*
                 * Forever,
                 *   read a line from the socket
                 *   print it to the console
                 *   echo it (i.e. write it) back to the client
                 */

                while (true) {
                    if (in.hasNext()) {
                        String data = in.nextLine();                                            //Receiving the request from the client
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = (JSONObject)jsonParser.parse(data);              //Parsing the request

                        String newe = (String)jsonObject.get("e");                                //Storing e
                        String newn = (String)jsonObject.get("n");                                //Storing n
                        String newid = (String)jsonObject.get("id");                              //Storing id
                        String operate = (String)jsonObject.get("input");                         //Storing input
                        String sign = (String)jsonObject.get("sign");                             //Storing sign

                        String verifyinput = "";
                        if (operate.equals(String.valueOf(0)))  {                                  //Verification input for view basic blockchain status
                            verifyinput = newid + "," + newe + "," + newn + "," +operate;
                        }

                        int difficulty = -1;
                        String transaction = "";

                        if (operate.equals(String.valueOf(1))) {                                   //Verification input for adding a transaction
                            difficulty = Integer.parseInt((String)jsonObject.get("difficulty"));
                            transaction = (String)jsonObject.get("transaction");
                            verifyinput = newid + "," + newe + "," + newn + "," +operate + "," + difficulty + "," + transaction;
                        }

                        if (operate.equals(String.valueOf(2))) {                                    //Verification input for verify the blockchain
                            verifyinput = newid + "," + newe + "," + newn + "," +operate;
                        }

                        if (operate.equals(String.valueOf(3))) {                                    //Verification input for view blockchain
                            verifyinput = newid + "," + newe + "," + newn + "," +operate;
                        }
                        int c_id = -1;
                        String c_data = "";
                        if (operate.equals(String.valueOf(4))) {                                    //Verification input for corrupting the blockchain
                            c_id = Integer.parseInt((String)jsonObject.get("c_id"));
                            c_data = (String)jsonObject.get("c_data");
                            s = "00000";
                            verifyinput = newid + "," + newe + "," + newn + "," +operate + "," + c_id + "," + c_data;
                        }

                        if (operate.equals(String.valueOf(5))) {                                    //Verification input for repairing the blockchain
                            verifyinput = newid + "," + newe + "," + newn + "," +operate;
                        }


                        boolean verifyresult = BlockChainServer.verify(verifyinput, sign);            //Verification

                        int newvalue = 0;
                        String result = newe.concat(newn);

                        String serveridhash = BlockChainClient.ComputeSHA_256_as_Hex_String(result);                        //Computing SHA-256
                        String newidvalue = serveridhash.substring(serveridhash.length()-40, serveridhash.length());        //Last 20 bytes

                        if (newidvalue.equals(newid) && verifyresult == true) {                         //Dual verification.
                            String id = newid;
                            String operation = operate;
                            int value = newvalue;

                            if (operation.equals(String.valueOf(0))) {                              //Condition for view basic blockchain status

                                String send = "Current Size of chain: "+blockChain.getChainSize()+ "\n"+"Current hashes per second by this machine: "+blockChain.hashesPerSecond()+"\n"+"Difficulty of most recent block: "+blockChain.getLatestBlock().getDifficulty()+"\n"+ "Nonce for most recent block: "+blockChain.getLatestBlock().getNonce()+"\n"+"Chain hash: "+blockChain.chainHash;
                                Map<String, String> map = new HashMap<>();                          //Map to store the result
                                map.put("send",send);
                                String new_send = new JSONObject(map).toString();
                                out.println(new_send);                                              //Sending the response to the client
                                out.flush();
                            }

                            if (operation.equals(String.valueOf(1))) {                              //Condition for adding a transaction
                                long start = System.currentTimeMillis();                            //Transaction start time
                                Block newblock = new Block(index,block.getTimestamp(),transaction,difficulty);              //Creating a new block
                                index++;
                                blockChain.addBlock(newblock);                                          //Adding the block to the chain
                                long end = System.currentTimeMillis();                                  //Transaction end time
                                String res = "Total execution time to add this block was " + (end - start) + " milliseconds";
                                Map<String, String> map = new HashMap<>();                              //Map to store the result
                                map.put("send",res);
                                String new_send = new JSONObject(map).toString();
                                out.println(new_send);                                                  //Sending the response to the client
                                out.flush();
                            }

                            if (operation.equals(String.valueOf(2))) {                                  //Condition for verifying the blockchain
                                long start = System.currentTimeMillis();
                                String res = "Verifying entire chain " + "\n" + "Chain verification: " + blockChain.isChainValid();
                                long end = System.currentTimeMillis();
                                String new_res = res + "\n"+ "Total execution time required to verify the chain was "+ (end-start) +" milliseconds";
                                Map<String, String> map = new HashMap<>();
                                map.put("send",new_res);
                                String new_send = new JSONObject(map).toString();
                                out.println(new_send);
                                out.flush();
                            }

                            if (operation.equals(String.valueOf(3))) {                             //Condition for view the blockchain
                                String res = blockChain.toString();
                                Map<String, String> map = new HashMap<>();                          //Map to store the result
                                map.put("send",res);
                                String send = new JSONObject(map).toString();
                                out.println(send);                                                  //Sending the response to the client
                                out.flush();
                            }

                            if (operation.equals(String.valueOf(4))) {                              //Condition for Corrupting the blockchain

                                Block corrupt_block = blockChain.blockchain.get(c_id);              //Getting the block to be corrupted
                                corrupt_block.setData(c_data);                                      //Corrupting the data
                                String res = "Block " + c_id + " now holds " + c_data;
                                Map<String, String> map = new HashMap<>();                           //Map to store the result
                                map.put("send",res);
                                String new_send = new JSONObject(map).toString();
                                out.println(new_send);                                               //Sending the response to the client
                                out.flush();
                            }

                            if (operation.equals(String.valueOf(5))) {                              //Condition for repairing the blockchain
                                long start = System.currentTimeMillis();                            //Repair start time
                                String res = "Repairing the entire chain ";
                                blockChain.repairChain();                                           //Reoairing the chain
                                long end = System.currentTimeMillis();                              //Repair end time
                                String new_res = res + "\n"+ "Total execution time required to repair the chain was "+ (end-start) +" milliseconds";
                                Map<String, String> map = new HashMap<>();
                                map.put("send",new_res);
                                String new_send = new JSONObject(map).toString();                   //Map to store the result
                                out.println(new_send);                                              //Sending the response to the client
                                out.flush();
                            }

                            System.out.println("Echoing: " + data);                 //Printing the request on the server console
                        }
                        else {
                            System.out.println("Error in Request");                 //If verification fails then prints error in request
                            out.println("Error in Request");
                            out.flush();
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                    //Verification
                }
            }// Handle exceptions
        } catch(IOException e){
            System.out.println("IO Exception:" + e.getMessage());

            // If quitting (typically by you sending quit signal) clean up sockets
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if (clientSocket != null) {
                    clientSocket.close();                                               //Closing the socket
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }

    }


    //Method for verification (Reference from BabyVerify)
    public static boolean verify(String messageToCheck, String encryptedHashStr)throws Exception  {

        String message[] = messageToCheck.split(",");
        BigInteger e = new BigInteger(message[1]);
        BigInteger n = new BigInteger(message[2]);

        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes("UTF-8");

        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);

        // messageToCheckDigest is a full SHA-256 digest
        // take two bytes from SHA-256 and add a zero byte
        byte[] messageDigest = new byte[messageToCheckDigest.length+1];

        messageDigest[0] = 0;   // most significant set to 0
        int i = 1, j= 0;
        while(i<messageDigest.length) {
            messageDigest[i] = messageToCheckDigest[j]; // take a byte from SHA-256
            i++;
            j++;
        }

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(messageDigest);

        // inform the client on how the two compare
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {

            return true;
        }
        else {
            return false;
        }
    }
}