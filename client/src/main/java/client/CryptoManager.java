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
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import library.Envelope;
import library.Request;
import library.Response;

public class CryptoManager {
	
/////////////////////////////////////
//   							   //
//	 		Sign Methods  		   //
//	   							   //
/////////////////////////////////////
	
	byte[] signRequest(Request request, PrivateKey key) {
		try {
			// Initialize needed structures
			Signature signature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert request to byteArray
			out.writeObject(request);
			out.flush();
			byte[] requestBytes = bos.toByteArray();
			// Sign with private key
			signature.initSign(key);
			signature.update(requestBytes);
			return signature.sign();
		} catch (
			InvalidKeyException		 |
			NoSuchAlgorithmException |
			SignatureException		 |
			IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	byte[] signResponse(Response response, PrivateKey key) {
		try {
			// Initialize needed structures
			Signature signature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(response);
			out.flush();
			byte[] responseBytes = bos.toByteArray();
			// Sign with private key
			signature.initSign(key);
			signature.update(responseBytes);
			return signature.sign();
		} catch (
			InvalidKeyException		 |
			NoSuchAlgorithmException |
			SignatureException		 |
			IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	boolean verifyResponse(Response response, byte[] signature, String username) {
		try {
			// Initialize needed structures
			PublicKey key = getPublicKeyFromKs(username, "server");
			Signature verifySignature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(response);
			out.flush();
			byte[] responseBytes = bos.toByteArray();
			// Verify signature
			verifySignature.initVerify(key);
			verifySignature.update(responseBytes);
			return verifySignature.verify(signature);
		} catch (
			InvalidKeyException 	 |
			NoSuchAlgorithmException |
			SignatureException 		 |
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
