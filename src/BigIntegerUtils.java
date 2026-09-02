package secretsharing;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Utility functions for unsigned BigIntegers (as opposed to Java's standard
 * two's complement).
 * 
 * @author elmar
 *
 */
public class BigIntegerUtils {

	/**
	 * Creates a new BigInteger from unsigned byte array representation.
	 * 
	 * @param bytes byte array with the byte sequence
	 * @return BigInteger corresponding to the unsigned bytes
	 */
	static BigInteger fromUnsignedByteArray(byte[] bytes) {
		return new BigInteger(1, bytes);
	}

	/**
	 * Converts a BigInteger to an unsigned byte array.
	 * 
	 * @param a the BigInteger to convert.
	 * @return the unsigned byte array corresponding to a.
	 */
	static byte[] toUnsignedByteArray(final BigInteger a) {
		byte[] aBytes = a.toByteArray();
		if (aBytes[0] == 0 && aBytes.length != 1) {
			aBytes = Arrays.copyOfRange(aBytes, 1, aBytes.length);
		}
		return aBytes;
	}

}
