/**
 * 
 */
package secretsharing;

import java.security.SecureRandom;
import java.util.Random;

/**
 * This class implements the simple XOR-based (n,n) secret sharing.
 * 
 * Secrets and shares are both represented as byte[] arrays.
 * 
 * Randomness is taken from a {@link java.security.SecureRandom} object.
 * 
 * @see SecureRandom
 * 
 * @author elmar
 * 
 */
public class XorSecretSharing {

	/**
	 * Creates a XOR secret sharing object for n shares
	 * 
	 * @param n
	 *            number of shares to use. Needs to fulfill n >= 2.
	 */
	public XorSecretSharing(int n) {
		assert (n >= 2);
		this.n = n;
		this.rng = new SecureRandom();
	}

	/**
	 * Shares the secret into n parts.
	 * 
	 * @param secret
	 *            The secret to share.
	 * 
	 * @return An array of the n shares.
	 */
	public byte[][] share(final byte[] secret) {

		// TODO: implement this

		return null;
	}

	/**
	 * Recombines the given shares into the secret.
	 * 
	 * @param shares
	 *            The complete set of n shares for this secret.
	 * 
	 * @return The reconstructed secret.
	 */
	public byte[] combine(final byte[][] shares) {

		// TODO: implement this

		return null;
	}

	private int n;

	public int getN() {
		return n;
	}

	private Random rng;
}
