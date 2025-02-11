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
//
//			Sign Methods
//
/////////////////////////////////////
    
    byte[] signRequest(Request request) {
		try {
			// Initialize needed structures
			PrivateKey myKey = getPrivateKeyFromKs();
			Signature signature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert request to byteArray
			out.writeObject(request);
			out.flush();
			byte[] requestBytes = bos.toByteArray();
			// Sign with private key
			signature.initSign(myKey);
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
    
	byte[] signResponse(Response response) {
		try {
			// Initialize needed structures
			PrivateKey myKey = getPrivateKeyFromKs();
			Signature signature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(response);
			out.flush();
			byte[] responseBytes = bos.toByteArray();
			// Sign with private key
			signature.initSign(myKey);
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

	boolean verifyRequest(Request request, byte[] signature, PublicKey keyFrom) {
    	try {
			// Initialize needed structures
			Signature verifySignature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(request);
			out.flush();
			byte[] requestBytes = bos.toByteArray();
			// Verify signature
			verifySignature.initVerify(keyFrom);
			verifySignature.update(requestBytes);
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
	
	boolean verifyResponse(Response response, byte[] signature, String from) {
		try {
			// Initialize needed structures
			PublicKey keyFrom = getPublicKeyFromKs(from);
			Signature verifySignature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(response);
			out.flush();
			byte[] responseBytes = bos.toByteArray();
			// Verify signature
			verifySignature.initVerify(keyFrom);
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
//
//           Get Alias By Key
//
//////////////////////////////////////////

    public String checkKey(PublicKey publicKey) {
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
//
//            Nonce Manipulation Methods
//
/////////////////////////////////////////////////////////

	public byte[] getServerNonce(PublicKey clientKey) {
		return nonces.get(clientKey);
	}

    public boolean checkNonce(PublicKey clientKey, byte[] nonce) {
    	if(nonces.containsKey(clientKey) && Arrays.equals(nonces.get(clientKey), nonce)) {
        	nonces.put(clientKey, null);
        	return true;
        }
        return false;
    }    

    public void generateRandomNonce(PublicKey clientKey) {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        nonces.put(clientKey, nonce);
    }

	// este metodo é para os testes do old envelope acho eu
    public byte[] generateRandomNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
    }
    
	
///////////////////////////////////////////
//
//	 Methods to get Keys from Keystore
//
///////////////////////////////////////////
    
    public PrivateKey getPrivateKeyFromKs() {
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
    
    PublicKey getPublicKeyFromKs(String alias) {
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/port_" + port + "/keystore"), passphrase);
            return ks.getCertificate(alias).getPublicKey();
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