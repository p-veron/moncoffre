package fr.toulon.masterdapm.MonCoffre.lib;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;



public class MyCipher {
	private static int ITER = 10000;
	private static int SIZE = 128;

	private SecretKey key;
	/* nullIV utilis'e pour chiffrer le IV en mode CBC */
	private static byte[] nullIV = {(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0};

	/* utile uniquement pour le debuggage */
	/* pas optimise */
	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}

	/**
	 * Crypt a message with a key derivated from a password
	 * 
	 * @param password
	 *            The password use to generate the key
	 * @param plaintext
	 *            The text to crypt
	 * @return The cryptogramm in byte
	 */
	public byte[] chiffre(char[] password, byte[] plaintext) {

		// Variables Declaration
		byte[] sel = new byte[16];
		SecureRandom random = new SecureRandom();
		Cipher cipherCBC = null;
		Cipher cipherCBCwithNullIV = null;
		byte[] iV = null;
		byte[] encryptedMessage = null;
		byte[] encryptedIV = null;

		// Creation of the CBC Cipher for the message
		try {
			cipherCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre",
					"NoSuchAlgorithmException on Creation of CBC Cipher");
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "NoSuchPaddingException on Creation of CBC Cipher");
			return null;
		}

		// Creation of the CBC Cipher for the IV
		// normalement le IV peut etre chiffre en ECB
		// mais cela ne fonctionne pas avec la librairie javascript WebCrypto 20170126
		// https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API
		// Cette librairie ne propose pas l'AES/ECB/Nopadding
		// C'est pourquoi on chiffre ici le IV en CBC/PKCS5Padding
        // afin d'assurer la compatibilite de l'appli Android avec le client Web
		try {
			cipherCBCwithNullIV = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre",
					"NoSuchAlgorithmException on Creation of CBC Cipher for the IV");
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "NoSuchPaddingException on Creation of CBC Cipher for the IV");
			return null;
		}

		random.nextBytes(sel);
		key = genAESKeyFromPass(password, sel, ITER, SIZE);
		//Log.d("masterdapm.MonCoffre",new String(password)+" "+byteArrayToHex(sel)+" "+byteArrayToHex(key.getEncoded()));

		// Initialisation of the CBC Cipher for the message
		try {
			cipherCBC.init(Cipher.ENCRYPT_MODE, key, random);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "InvalidKeyException on init of CBC Cipher for the message");
			return null;
		}

		// Initialisation of the CBC Cipher for the IV
		// on chiffre le IV en mode CBC avec un vecteur initial nul
		IvParameterSpec ivspec = new IvParameterSpec(nullIV);
		try {
			cipherCBCwithNullIV.init(Cipher.ENCRYPT_MODE, key,ivspec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "InvalidKeyException on init of CBC Cipher for the IV");
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "InvalidParameterException on init of CBC Cipher for the IV");
		}

		// Encryption of the message
		try {
			encryptedMessage = cipherCBC.doFinal(plaintext);
			//Log.d("masterdapm.MonCoffre","crypto "+byteArrayToHex(encryptedMessage));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "IllegalBlockSize on encryption of message");
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "BadPadding on encryption of message");
			return null;
		}

		iV = cipherCBC.getIV();

		// Encryption of the IV
		try {
			encryptedIV = cipherCBCwithNullIV.doFinal(iV);
			Log.d("masterdapm.MonCoffre", "Clair IV : "+byteArrayToHex(iV));
			Log.d("masterdapm.MonCoffre", "Crypto IV : "+byteArrayToHex(encryptedIV));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "IllegalBlockSize on encryption of iv");
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "BadPadding on encryption of iv");
			return null;
		}

		// Create the cryptogram
		ByteBuffer buffer = ByteBuffer.allocate(encryptedMessage.length
				+ encryptedIV.length + sel.length);
		buffer.put(encryptedIV);
		buffer.put(sel);
		buffer.put(encryptedMessage);

		return buffer.array();
	}

	/**
	 * Decrypt a cryptogram.
	 * 
	 * @param password
	 *            The password to generate the key
	 * @param cryptogram
	 *            The cryptogramm to decrypt
	 * @return The message in a byte array
	 */
	public byte[] dechiffre(char[] password, byte[] cryptogram) {
		ByteBuffer buffer = ByteBuffer.wrap(cryptogram);

		int size = 16;

		byte[] iV = null;
		byte[] encryptedIV = new byte[2*size];
		buffer.get(encryptedIV);

		byte[] sel = new byte[size];
		buffer.get(sel);

		byte[] message = null;
		byte[] encryptedMessage = new byte[buffer.capacity() - encryptedIV.length - sel.length];
		buffer.get(encryptedMessage);

		Cipher cipherCBC = null;
		Cipher cipherCBCwithIVNull = null;

		key = genAESKeyFromPass(password, sel, ITER, SIZE);

		// Creation of the CBC Cipher for the message
		try {
			cipherCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}

		// Creation of the CBC Cipher for the IV
		try {
			cipherCBCwithIVNull = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}

		// Decryption of the IV

		IvParameterSpec iPS = new IvParameterSpec(nullIV);
		try {
			cipherCBCwithIVNull.init(Cipher.DECRYPT_MODE, key, iPS);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		try {
			iV = cipherCBCwithIVNull.doFinal(encryptedIV);
			Log.d("masterdapm.MonCoffre","IV decipher"+byteArrayToHex(iV));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}

		iPS = new IvParameterSpec(iV);

		// Decryption of the message
		try {
			cipherCBC.init(Cipher.DECRYPT_MODE, key, iPS);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}

		try {
			message = cipherCBC.doFinal(encryptedMessage);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}

		return message;
	}

	/**
	 * Derivate an AES Key from a password
	 * 
	 * @param password
	 *            The password to derivate
	 * @param sel
	 *            The salt use for derivate the password
	 * @param iter
	 *            The number of iteration of the PKCS5 algorithm
	 * @param taille
	 *            The size of the key
	 * @return The Key or null if Exception occured
	 */
	public SecretKey genAESKeyFromPass(char[] password, byte[] sel, int iter,
			int taille) {
		PBEKeySpec pks = new PBEKeySpec(password, sel, iter, taille);
		SecretKey key = null;
		try {
			key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
					.generateSecret(pks);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "InvalidKeySpec on key generation");
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e("masterdapm.MonCoffre", "NoSuchAlgorithm on key generation");
			return null;
		}
		SecretKeySpec sks = new SecretKeySpec(key.getEncoded(), "AES");

		return sks;
	}

	/**
	 * @return the key
	 */
	public SecretKey getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(SecretKey key) {
		this.key = key;
	}

}
