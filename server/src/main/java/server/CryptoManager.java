package server;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

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

import library.Request;
import library.Response;

public class CryptoManager {

    private HashMap<PublicKey, byte[]> nonces = null;
    private int port = 0;

    protected CryptoManager(int port){
        this.nonces = new HashMap<>();
        this.port = port;
    }
    
    
/////////////////////////////////////
//								   //
//			Sign Methods  		   //
//	  							   //
/////////////////////////////////////
    
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
	
	boolean verifyRequest(Request request, byte[] signature) {
		try {
			// Initialize needed structures
			PublicKey key = request.getPublicKey();
			Signature verifySignature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(request);
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
	
	boolean verifyResponse(Response response, byte[] signature) {
		try {
			// Initialize needed structures
			PublicKey key = response.getServerKey();
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
    
	
//////////////////////////////////////////
//										//
//           Get Alias By Key           //
//    									//
//////////////////////////////////////////

    public String checkKey(PublicKey publicKey){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/port_" + port + "/keystore"), passphrase);

            Enumeration<String> aliases = ks.aliases();

            for (; aliases.hasMoreElements(); ) {

                String alias = aliases.nextElement();

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

/////////////////////////////////////////////////////////
//										               //
//            Nonce Manipulation Methods               //
//										               //
/////////////////////////////////////////////////////////

    private HashMap<PublicKey, byte[]> getNonces(){
        return nonces;
    }

    public boolean checkNonce(PublicKey key, byte[] nonce) {
        if(getNonces().containsKey(key) && Arrays.equals(getNonces().get(key), nonce)) {
        	getNonces().put(key, null);
        	return true;
        }
        return false;
    }    

    public byte[] generateRandomNonce(PublicKey key) {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        getNonces().put(key, nonce);
        return nonce;
    }

    public byte[] generateRandomNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
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
            ks.load(new FileInputStream("keystores/port_" + port + "/keystore"), passphrase);
            key = (PrivateKey) ks.getKey("server", passphrase);
        } catch (
            NoSuchAlgorithmException	| 
            UnrecoverableEntryException | 
            KeyStoreException 			| 
            CertificateException 		|
            IOException e) {
            e.printStackTrace();
        }
        return key;
    }
    
    PublicKey getPublicKey() {
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/port_" + port + "/keystore"), passphrase);
            return ks.getCertificate("server").getPublicKey();
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