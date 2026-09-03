package secretsharing;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class implements Shamir's (t,n) secret sharing.
 *
 * Secrets are represented as BigInteger objects, shares as ShamirShare objects.
 *
 * Randomness is taken from a {@link java.security.SecureRandom} object.
 *
 * @see ShamirShare
 * @see BigInteger
 * @see SecureRandom
 *
 * @author elmar
 *
 */
public class ShamirSecretSharing {

	/**
	 * Creates a (t,n) Shamir secret sharing object for n shares with threshold
	 * t.
	 * 
	 * @param t
	 *          threshold: any subset of t <= i <= n shares can recover the
	 *          secret.
	 * @param n
	 *          number of shares to use. Needs to fulfill n >= 2.
	 */
	public ShamirSecretSharing(int t, int n) {
		assert (t >= 2);
		assert (n >= t);

		this.t = t;
		this.n = n;
		this.rng = new SecureRandom();

		// use p = 2^256 + 297
		this.p = BigInteger.ONE.shiftLeft(256).add(BigInteger.valueOf(297));
		assert (this.p.isProbablePrime(2));
	}

	/**
	 * Shares the secret into n parts.
	 * 
	 * @param secret
	 *               The secret to share.
	 * 
	 * @return An array of the n shares.
	 */
	public ShamirShare[] share(BigInteger secret) {

		// TODO: implement this
		// Polynom aufstellen
		BigInteger[] a = new BigInteger[t];
		a[0] = secret; // a[0] s = f(0)
		for (int i = 1; i < t; i++) { // Random Zahlen berechnen für Koeff Bitlänge p
			BigInteger koeffizient;
			do {
				koeffizient = new BigInteger(this.p.bitLength(), rng);
			} while (koeffizient.compareTo(this.p) >= 0); // Bedingung sicherstellen sonst wiederholen
			a[i] = koeffizient;
		}

		ShamirShare[] shares = new ShamirShare[n];
		// s_i Share berechnen
		for (int i = 0; i < n; i++) {
			BigInteger x = BigInteger.valueOf(i + 1); // Start bei 1
			// Koords mit Horner Schema ausrechnen
			BigInteger y = horner(x, a);
			// Share s_i erzeugen
			shares[i] = new ShamirShare(x, y);
		}

		return shares;
	}

	/**
	 * Evaluates the polynomial a[0] + a[1]*x + ... + a[t-1]*x^(t-1) modulo p at
	 * point x using Horner's rule.
	 * 
	 * @param x
	 *          point at which to evaluate the polynomial
	 * @param a
	 *          array of coefficients
	 * @return value of the polynomial at point x
	 */
	private BigInteger horner(BigInteger x, BigInteger[] a) {

		// TODO: implement this
		// Horner Schema, Polynom ausklammern
		// Start bei höchsten Koeffizienten und rückwärts rechnen

		BigInteger result = a[a.length - 1];
		for (int i = a.length - 2; i >= 0; i--) {
			result = result.multiply(x).add(a[i]).mod(p);
		}

		return result;
	}

	/**
	 * Recombines the given shares into the secret.
	 * 
	 * @param shares
	 *               A set of at least t out of the n shares for this secret.
	 * 
	 * @return The reconstructed secret.
	 */
	public BigInteger combine(ShamirShare[] shares) {

		// TODO: implement this

		int k = shares.length; // K-Shares
		BigInteger secret = BigInteger.ZERO;

		for (int i = 0; i < k; i++) { // Summe K-Shared
			BigInteger x_i = shares[i].x;
			BigInteger y_i = shares[i].s;

			BigInteger prod_i = BigInteger.ONE; // Produkt über x_i und j
			for (int j = 0; j < k; j++) {
				if (i == j) {
					continue;
				}
				BigInteger x_j = shares[j].x;
				BigInteger zaehler = x_j.negate().mod(p);
				BigInteger nenner = x_i.subtract(x_j).mod(p);

				// LaGrange Division mit Inv
				BigInteger division = zaehler.multiply(nenner.modInverse(p));
				prod_i = prod_i.multiply(division).mod(p);
			}
			// Multiplikation mit y_i
			BigInteger term = y_i.multiply(prod_i).mod(p);
			secret = secret.add(term).mod(p); // Gesamtsumme

		}

		return secret;
	}

	public int maxSecretLength() {
		return this.p.bitLength() / 8;
	}

	public int getT() {
		return t;
	}

	public int getN() {
		return n;
	}

	private int t;
	private int n;
	private SecureRandom rng;
	private BigInteger p;

}
