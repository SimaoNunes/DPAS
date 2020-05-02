
package server;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.MessageDigest;
import java.security.KeyStore;

import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap; 

import library.Envelope;
import library.Response;

public class CryptoManager {

    private HashMap<PublicKey, byte[]> nonces = null;

    protected CryptoManager(){
        nonces = new HashMap<>();
    }
    
    //////////////////////////////////////////
    //										//
    //            Check Methods             //
    //    									//
    //////////////////////////////////////////

    public String checkKey(PublicKey publicKey){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/keystore"), passphrase);

            @SuppressWarnings("rawtypes")
			Enumeration aliases = ks.aliases();

            for (; aliases.hasMoreElements(); ) {

                String alias = (String) aliases.nextElement();

                if (ks.isCertificateEntry(alias)) {
                    PublicKey key = ks.getCertificate(alias).getPublicKey();
                    if (key.equals(publicKey)) {
                        return alias;
                    }
                }
            }
        } catch (
            KeyStoreException | 
            CertificateException | 
            NoSuchAlgorithmException | 
            IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean checkHash(Envelope envelope){

        MessageDigest md ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] hash = decipher(envelope.getHash(), envelope.getRequest().getPublicKey());

        try {
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(envelope.getRequest());
            out.flush();
            byte[] requestBytes = bos.toByteArray();
            return Arrays.equals(md.digest(requestBytes), hash);

        } catch (
            NoSuchAlgorithmException | 
            IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /////////////////////////////////////////////////////////
    //										               //
    //            Nonce Manipulation Methods               //
    //										               //
    /////////////////////////////////////////////////////////

    private HashMap<PublicKey, byte[]> getNonces(){
        return nonces;
    }

    public boolean checkNonce(PublicKey key, byte[] nonce){
        return getNonces().containsKey(key) && Arrays.equals(getNonces().get(key), nonce);
    }    

    public byte[] generateRandomNonce(PublicKey key){
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        getNonces().put(key, nonce);
        return nonce;
    }

    public byte[] generateRandomNonce(){
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
    }

    /////////////////////////////////////////////////////////
    //										               //
    //             Cipher and Decipher methods             //
    //										               //
    /////////////////////////////////////////////////////////


    public byte[] cipher(Response response, PrivateKey key) throws IOException{
        MessageDigest md;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        Cipher cipher;
        byte[] finalBytes = null;
        try {
        	// Hash
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(response);
            out.flush();
            byte[] response_bytes = bos.toByteArray();
            byte[] response_hash = md.digest(response_bytes);
            // Cipher
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            finalBytes = cipher.doFinal(response_hash);
        } catch (
        	IOException 				|	
            InvalidKeyException 		| 
            BadPaddingException 		| 
            IllegalBlockSizeException	| 
            NoSuchPaddingException		| 
            NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return finalBytes;
    }

    public byte[] decipher(byte[] bytes, PublicKey key){
        byte[] finalBytes = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            finalBytes = cipher.doFinal(bytes);
        } catch (
            NoSuchAlgorithmException | 
            NoSuchPaddingException | 
            BadPaddingException | 
            IllegalBlockSizeException | 
            InvalidKeyException e) {
            e.printStackTrace();
        }
        return finalBytes;
    }
    
///////////////////////////////////////////
//   									 //
//	 Methods to get Keys from Keystore   //
//   									 //
///////////////////////////////////////////
    
    public PrivateKey getPrivateKey(){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        PrivateKey key = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/keystore"), passphrase);
            key = (PrivateKey) ks.getKey("server", passphrase);
        } catch (
            NoSuchAlgorithmException | 
            UnrecoverableEntryException | 
            KeyStoreException | 
            CertificateException |
            IOException e) {
            e.printStackTrace();
        }
        return key;
    }
    
}