package fr.masterdapm.toulon.MyKnox.lib;

import java.util.Arrays;

public class PasswordLog {
	private String siteName;
	private byte[] crypto;
	
	/**
	 * @param siteName
	 * @param crypto
	 */
	public PasswordLog(String siteName, byte[] crypto) {
		this.siteName = siteName;
		this.crypto = Arrays.copyOf(crypto, crypto.length);
	}
	
	/**
	 * @return the siteName
	 */
	public String getSiteName() {
		return siteName;
	}
	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	/**
	 * @return the crypto
	 */
	public byte[] getCrypto() {
		return this.crypto;
	}
	/**
	 * @param crypto the crypto to set
	 */
	public void setCrypto(byte[] crypto) {
		this.crypto = Arrays.copyOf(crypto, crypto.length);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return siteName;
	}
	
	
	
}
