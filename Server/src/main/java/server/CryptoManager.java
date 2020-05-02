
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

import Library.Envelope;

public class CryptoManager {

    private HashMap<PublicKey, byte[]> nonces = null;

    protected CryptoManager(){
        nonces = new HashMap<>();
    }

    public PrivateKey getPrivateKey(){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        PrivateKey key = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);
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
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);

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

    public boolean checkHash(Envelope envelope, ObjectOutputStream outStream){

        MessageDigest md ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] hash = decipher(envelope.getHash(), envelope.getRequest().getPublicKey());

        try {
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(envelope.getRequest());
            out.flush();
            byte[] request_bytes = bos.toByteArray();
            return Arrays.equals(md.digest(request_bytes), hash);

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
        if(getNonces().containsKey(key)){
            if(Arrays.equals(getNonces().get(key), nonce)){
                getNonces().remove(key);
                return true;
            }
        }
        return false;
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


    public byte[] cipher(byte[] bytes, PrivateKey key){
        byte[] final_bytes = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final_bytes = cipher.doFinal(bytes);
        } catch (
            InvalidKeyException | 
            BadPaddingException | 
            IllegalBlockSizeException | 
            NoSuchPaddingException | 
            NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return final_bytes;
    }

    public byte[] decipher(byte[] bytes, PublicKey key){
        byte[] final_bytes = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            final_bytes = cipher.doFinal(bytes);
        } catch (
            NoSuchAlgorithmException | 
            NoSuchPaddingException | 
            BadPaddingException | 
            IllegalBlockSizeException | 
            InvalidKeyException e) {
            e.printStackTrace();
        }
        return final_bytes;
    }
}