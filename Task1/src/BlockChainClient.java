/**
 * Author: Kabir Soneja
 * Andrew ID: ksoneja
 * Last Modified: March 06, 2020
 *
 * This program demonstrates a very simple TCP client for blockchain.
 */

import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.JSONObject;
import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

//Reference from BabyHash, BabyVerify, BabySign, RSAExample

public class BlockChainClient {

    static BigInteger n; // n is the modulus for both the private and public keys
    static BigInteger e; // e is the exponent of the public key
    static BigInteger d; // d is the exponent of the private key


    /**
     * Menu driven main method.
     * Takes the user option and performs the requested task
     * Displays the result of the task
     */
    public static void main(String args[]) {
        JSONParser jsonParser = new JSONParser();
        BlockChain blockChain = new BlockChain();
        // arguments supply hostname
        Socket clientSocket = null;
        try {
            int serverPort = 7777;                                                                          //Specifying the serverport
            clientSocket = new Socket("localhost", serverPort);

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));               //Reader
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));      //Writer


            Scanner sc = new Scanner(System.in);
            String id = null;
            int value = 0;
            String clientpacket = null;


            Random rnd = new Random();

            // Step 1: Generate two large random primes.
            // We use 400 bits here, but best practice for security is 2048 bits.
            // Change 400 to 2048, recompile, and run the program again and you will
            // notice it takes much longer to do the math with that many bits.
            BigInteger p = new BigInteger(400,100,rnd);
            BigInteger q = new BigInteger(400,100,rnd);

            // Step 2: Compute n by the equation n = p * q.
            n = p.multiply(q);

            // Step 3: Compute phi(n) = (p-1) * (q-1)
            BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

            // Step 4: Select a small odd integer e that is relatively prime to phi(n).
            // By convention the prime 65537 is used as the public exponent.
            e = new BigInteger("65537");

            // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
            d = e.modInverse(phi);

            // Encode a simple message. For example the letter 'A' in UTF-8 is 65
            BigInteger m = new BigInteger("65");

            // Step 8: To encrypt a message M compute C = M^e (mod n)
            BigInteger c = m.modPow(e, n);

            // Step 9: To decrypt a message C compute M = C^d (mod n)
            BigInteger clear = c.modPow(d, n);
            //System.out.println("Cypher text = " + c);
            //System.out.println("Clear text = " + clear); // Should be "65"

            // Step 8 (reprise) Encrypt the string 'RSA is way cool.'
            String s = "RSA is way cool.";
            m = new BigInteger(s.getBytes()); // m is the original clear text
            c = m.modPow(e, n);     // Do the encryption, c is the cypher text

            // Step 9 (reprise) Decrypt...
            clear = c.modPow(d, n); // Decrypt, clear is the resulting clear text
            String clearStr = new String(clear.toByteArray());  // Decode to a string

            String result = e.toString().concat(n.toString());
            String privatekey = d.toString().concat((n.toString()));

            String hash = ComputeSHA_256_as_Hex_String(result);                                     //Computing Hash
            String babyHash = hash.substring(hash.length()-40, hash.length());
            id = babyHash;
            while(true) {
                //Menu driven blockchain client application
                System.out.println(" ");
                System.out.println("0. View basic blockchain status.");
                System.out.println("1. Add a transaction to blockchain.");
                System.out.println("2. Verify the blockchain.");
                System.out.println("3. View the blockchain.");
                System.out.println("4. Corrupt the chain");
                System.out.println("5. Hide the corruption by recomputing hashes.");
                System.out.println("6. Exit");
                int input = Integer.parseInt(sc.nextLine());

                if (input == 6) {                                                                   //Condition for exit
                    System.exit(1);
                }
                Map<String, String> map = new HashMap<>();                                          //Map to store values
                if (input == 0) {                                                                   //Condition for view basic blockchain status

                    clientpacket = id + "," + e + "," + n + "," + input ;                           //id,e,n,input packet
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                   //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();                                  //Creating client packet

                }

                if (input == 1) {                                                                   //Condition for adding a transaction
                    System.out.println("Enter difficulty > 0");                                     //Difficulty input
                    int diff = Integer.parseInt(sc.nextLine());
                    System.out.println("Enter Transaction");                                        //Input data
                    String transaction = sc.nextLine();

                    clientpacket = id + "," + e + "," + n + "," + input+"," + diff + "," + transaction ;              //id,e,n,operation,value
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                                       //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("difficulty",String.valueOf(diff));
                    map.put("transaction",transaction);
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();                                          //Setting the new clientpacket

                }
                if (input == 2) {                                                                               //Condition for verifying the blockchain
                    clientpacket = id + "," + e + "," + n + "," + input ;                           //id,e,n,operation,value
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                   //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();                                      //Setting the new clientpacket
                }

                if (input == 3) {                                                                               //Condition for view the blockchain
                    clientpacket = id + "," + e + "," + n + "," + input ;              //id,e,n,operation,value
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                   //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();                                              //Setting the new clientpacket
                }

                if (input == 4) {                                                                           //Condition for Corrupting the blockchain
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter the block ID of block to Corrupt");                           //ID of block to be corrupted
                    int corrupt_id = Integer.parseInt(sc.nextLine());
                    System.out.println("Enter the new data for block "+ corrupt_id);                                     //Corrupt data to be added
                    String corrupt_data = sc.nextLine();
                    clientpacket = id + "," + e + "," + n + "," + input+"," + corrupt_id + "," + corrupt_data ;              //id,e,n,operation,value
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                   //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("c_id",String.valueOf(corrupt_id));
                    map.put("c_data",corrupt_data);
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();                                          //Setting the new clientpacket

                }

                if (input == 5) {                                                                                           //Condition for repairing the blockchain
                    clientpacket = id + "," + e + "," + n + "," + input ;                       //id,e,n,operation,value
                    String signinput = BlockChainClient.sign(clientpacket, d, n);                   //Signature
                    clientpacket = clientpacket + ":" + signinput;
                    map.put("id",id);
                    map.put("e",e.toString());
                    map.put("n",n.toString());
                    map.put("input", String.valueOf(input));
                    map.put("sign",signinput);
                    clientpacket = new JSONObject(map).toString();
                }


                out.println(clientpacket);                                                          //Sending the request to the server
                out.flush();
                String data = in.readLine();
                JSONObject js = (JSONObject)jsonParser.parse(data);                                 //Parsing the json object
                System.out.println(js.get("send"));                                                 //Printing the returned value

            }

        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    //Method for signature (Reference BabySign)
    public static String sign(String message, BigInteger d, BigInteger n) throws Exception {

        // compute the digest with SHA-256
        byte[] bytesOfMessage = message.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we only want two bytes of the hash for BabySign
        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[bigDigest.length+1];

        messageDigest[0] = 0;   // most significant set to 0
        int i = 1, j= 0;
        while(i<messageDigest.length) {
            messageDigest[i] = bigDigest[j]; // take a byte from SHA-256
            i++;
            j++;
        }
        // take a byte from SHA-256

        // The message digest now has three bytes. Two from SHA-256
        // and one is 0.

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this as a big integer string
        return c.toString();
    }

    //Method for computing (SHA-256)
    public static String ComputeSHA_256_as_Hex_String(String text) {

        try {
            // Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            // allocate room for the result of the hash
            byte[] hashBytes;
            // perform the hash
            digest.update(text.getBytes("UTF-8"), 0, text.length());
            // collect result
            hashBytes = digest.digest();
            return convertToHex(hashBytes);
        }
        catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        }
        catch (UnsupportedEncodingException uee ) {
            System.out.println("Unsupported encoding exception thrown " + uee);
        }
        return null;
    }

    //Method to convert bytes to String
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

}