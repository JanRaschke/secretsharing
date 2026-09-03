package secretsharing;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * ShamirShare objects contain the two integers representing one share in
 * Shamir's secret sharing scheme: the x-coordinate `x' and the y-coordinate
 * `s'.
 * 
 * @see BigInteger
 * 
 * @author elmar
 * 
 */
public class ShamirShare {

	public ShamirShare(BigInteger x, BigInteger s) {
		this.x = x;
		this.s = s;
	}

	@Override
	public String toString() {
		return String.format("(%s,%s)", this.x, this.s);
	}

	final public BigInteger x;
	final public BigInteger s;

	/**
	 * Writes this share to the supplied stream.
	 * 
	 * @param os
	 * 		Stream to write to
	 */
	public void writeTo(OutputStream os) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(os);
		out.writeObject(x);
		out.writeObject(s);
	}

	/**
	 * Factory method for constructing a share from the supplied stream.
	 * 
	 * @return
	 * 	Share object read from the stream.
	 */
	public static ShamirShare fromStream(InputStream is) throws IOException {
		ObjectInputStream in = new ObjectInputStream(is);
		BigInteger x = null, s = null;
		try {
			x = (BigInteger) in.readObject();
			s = (BigInteger) in.readObject();
		} catch (ClassNotFoundException e) {
			// basically can't happen, BigInteger is in the standard library
			e.printStackTrace();
		}
		return new ShamirShare(x, s);
	}

	/**
	 * Method to serialize the class' content of the form [Length x][Bytes x][Length s][Bytes s]
	 *
	 * @return bytearray with the bytes content of x and s of the form [Length x][Bytes x][Length s][Bytes s]
	 * @throws IOException
	 */
	public byte[] toBytes() throws IOException {
		// TODO on day 2
		return null;
	}

	/**
	 * Method for deserialization
	 *
	 * @param data , bytearray of the form [Length x][Bytes x][Length s][Bytes s]
	 * @return ShamirShare object containing the content of the input data.
	 * @throws IOException
	 */
	public static ShamirShare fromBytes(byte[] data) throws IOException {
		// TODO on day 2
		return null;
	}
}
