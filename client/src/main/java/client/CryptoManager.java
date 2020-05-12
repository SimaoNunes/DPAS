package client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import java.util.Arrays;
import java.util.HashMap;

import library.Pair;
import library.Quadruplet;
import library.Request;
import library.Response;
import org.json.simple.JSONObject;

public class CryptoManager {
	
	String username = null;
	private HashMap<PublicKey, byte[]> nonces = null;
	
    public CryptoManager(String username) {
		this.username = username;
		this.nonces = new HashMap<>();
    }
	
/////////////////////////////////////
//   							   //
//	 		Sign Methods  		   //
//	   							   //
/////////////////////////////////////

	byte[] signRequest(Request request) {
		try {
			PrivateKey key = getPrivateKeyFromKs();
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

	byte[] signMessage(Quadruplet<String, Integer, String, int[]> message){
    	try{
			PrivateKey key = getPrivateKeyFromKs();
			// Initialize needed structures
			Signature signature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(message);
			out.flush();
			byte[] objectBytes = bos.toByteArray();
			// Sign with private key
			signature.initSign(key);
			signature.update(objectBytes);
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

	boolean verifyMessage(JSONObject object, byte[] signature){

		System.out.println(object.toJSONString());
		PublicKey keyFrom = getPublicKeyFromKs((String) object.get("user"));

		Quadruplet<String, Integer, String, int[]> quadruplet =
				new Quadruplet( object.get("username"),
						 object.get("ts"), object.get("message")
						,  object.get("ref"));

		try {
			// Initialize needed structures
			Signature verifySignature = Signature.getInstance("SHA256withRSA");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Convert response to byteArray
			out.writeObject(quadruplet);
			out.flush();
			byte[] messageBytes = bos.toByteArray();
			// Verify signature
			verifySignature.initVerify(keyFrom);
			verifySignature.update(messageBytes);
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

	byte[] signResponse(Response response) {
		try {
			PrivateKey key = getPrivateKeyFromKs();
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
	
	boolean verifyResponse(Response response, byte[] signature, PublicKey keyFrom) {
		try {
			// Initialize needed structures
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


    
    
/////////////////////////////////////////////////////////
//										               //
//            Nonce Manipulation Methods               //
//										               //
/////////////////////////////////////////////////////////
    
	public byte[] getNonce(PublicKey key){
		return nonces.get(key);
	}

	public boolean checkNonce(PublicKey key, byte[] nonce) {
        if(nonces.containsKey(key) && Arrays.equals(nonces.get(key), nonce)) {
        	nonces.put(key, null);
        	return true;
        }
        return false;
    }    

    public void generateRandomNonce(PublicKey key) {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
		random.nextBytes(nonce);
		nonces.put(key, nonce);
    }

///////////////////////////////////////////
//									     //
//   Methods to get Keys from Keystore   //
//									     //
///////////////////////////////////////////
    
    PrivateKey getPrivateKeyFromKs(){
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
    
    PublicKey getPublicKeyFromKs(String entity){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystores/" + username + "_keystore"), passphrase);
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

	HashMap<PublicKey, Integer> initiateServersPorts(int nServers){

		HashMap<PublicKey, Integer> result = new HashMap<>();

		int i = 0;

		char[] passphrase = "changeit".toCharArray();
		KeyStore ks = null;

		try{

			ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream("keystores/" + username + "_keystore"), passphrase);

		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while(i < nServers){
			try {
				result.put(ks.getCertificate("server" + String.valueOf(9000 + i)).getPublicKey(), 9000 + i);
				i++;
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
