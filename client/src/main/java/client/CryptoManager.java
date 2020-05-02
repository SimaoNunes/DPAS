package client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import library.Envelope;
import library.Request;

public class CryptoManager {
	
/////////////////////////////////////
//								   //
//   Cipher and decipher Methods   //
//	      						   //
/////////////////////////////////////
	
    byte[] cipherRequest(Request request, PrivateKey key){

        MessageDigest md;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;

        Cipher cipher;

        try {
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(request);
            out.flush();
            byte[] requestBytes = bos.toByteArray();
            byte[] requestHash = md.digest(requestBytes);

            cipher = Cipher.getInstance("RSA/None/OAEPWITHSHA-256ANDMGF1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(requestHash);

        } catch (
            IOException | 
            NoSuchAlgorithmException | 
            InvalidKeyException | 
            NoSuchPaddingException | 
            BadPaddingException | 
            IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    byte[] decipher(byte[] bytes, PublicKey key){
        byte[] final_bytes = null;
        Cipher cipher;

        try {
            cipher = Cipher.getInstance("RSA/None/OAEPWITHSHA-256ANDMGF1PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);
            final_bytes = cipher.doFinal(bytes);
        } catch (
            NoSuchAlgorithmException | 
            InvalidKeyException | 
            NoSuchPaddingException | 
            BadPaddingException | 
            IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return final_bytes;
    }

//////////////////////////////
//							//
//	Check Envelope's Hash	//
//							//
//////////////////////////////
    
    public boolean checkHash(Envelope envelope, String userName){
        MessageDigest md;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;

        byte[] hash = this.decipher(envelope.getHash(), getPublicKeyFromKs(userName, "server"));

        try {
            md = MessageDigest.getInstance("SHA-256");

            out = new ObjectOutputStream(bos);
            out.writeObject(envelope.getResponse());
            out.flush();
            byte[] response_bytes = bos.toByteArray();

            return Arrays.equals(md.digest(response_bytes), hash);
        } catch (
            NoSuchAlgorithmException | 
            IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
//////////////////////////////
//							//
//		Nonce Methods		//
//							//
//////////////////////////////
    
    byte[] generateClientNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
    }

    
///////////////////////////////////////////
//									     //
//   Methods to get Keys from Keystore   //
//									     //
///////////////////////////////////////////
    
    PrivateKey getPrivateKeyFromKs(String username){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        PrivateKey key = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/" + username + "_keystore"), passphrase);
            key = (PrivateKey) ks.getKey(username, passphrase);
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
    
    PublicKey getPublicKeyFromKs(String userName, String entity){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/" + userName + "_keystore"), passphrase);
            return ks.getCertificate(entity).getPublicKey();
        } catch (
            CertificateException | 
            NoSuchAlgorithmException | 
            IOException | 
            KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }
}
