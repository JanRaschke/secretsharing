package secretsharing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    public XorSecretSharing(int n) {
        assert (n >= 2);
        this.n = n;
        this.rng = new SecureRandom();
    }

    /**
     * Aufgabe 1a)
     *
     * @param secret The secret to share.
     * @return An array of the n shares.
     */
    public byte[][] share(final byte[] secret) {
        if (secret == null) {
            throw new IllegalArgumentException("Secret must not be null.");
        }
        int len = secret.length;
        byte[][] shares = new byte[n][len];

        // 1. Generate n-1 random shares
        for (int i = 0; i < n - 1; i++) {
            rng.nextBytes(shares[i]);
        }

        // 2. Set s_n = secret
        for (int j = 0; j < len; j++) {
            byte val = secret[j];
            for (int i = 0; i < n - 1; i++) {
                val ^= shares[i][j];
            }
            shares[n - 1][j] = val;
        }

        return shares;
    }

    /**
     * Recombines the given shares into the secret.
     *
     * @param shares The complete set of n shares for this secret.
     *
     * @return The reconstructed secret.
     */
    public byte[] combine(final byte[][] shares) {
        if (shares == null) {
            throw new IllegalArgumentException("Shares must not be null.");
        }
        if (shares.length != n) {
            throw new IllegalArgumentException(String.format("Expected %d shares, but got %d.", n, shares.length));
        }
        if (shares.length == 0 || shares[0] == null) {
            throw new IllegalArgumentException("Shares array contains null or is empty.");
        }
        int len = shares[0].length;
        for (int i = 1; i < shares.length; i++) {
            if (shares[i] == null || shares[i].length != len) {
                throw new IllegalArgumentException("All shares must be non-null and have identical length.");
            }
        }

        byte[] secret = new byte[len];
        // Reconstruct secret: s = s_1 ^ s_2 ^ ... ^ s_n
        for (int j = 0; j < len; j++) {
            byte val = 0;
            for (int i = 0; i < n; i++) {
                val ^= shares[i][j];
            }
            secret[j] = val;
        }

        return secret;
    }

    /**
     * Aufgabe 1b)
     *
     * @param inputFile The input file to split.
     * @throws IOException If reading or writing fails.
     */
    public void splitFile(File inputFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(inputFile.toPath());
        byte[][] shares = share(fileContent);
        for (int i = 0; i < n; i++) {
            File shareFile = new File(inputFile.getPath() + "-" + (i + 1));
            Files.write(shareFile.toPath(), shares[i]);
        }
    }

    /**
     * Reconstructs a file from n share files F-1, F-2, ..., F-n. (Aufgabe 1b)
     *
     * @param shareFiles Array of share files.
     * @param outputFile The destination file for the reconstructed content.
     * @throws IOException If reading or writing fails.
     */
    public void reconstructFile(File[] shareFiles, File outputFile) throws IOException {
        if (shareFiles == null || shareFiles.length != n) {
            throw new IllegalArgumentException(String.format("Expected %d share files, but got %d.", n,
                    shareFiles == null ? 0 : shareFiles.length));
        }
        byte[][] shares = new byte[n][];
        for (int i = 0; i < n; i++) {
            shares[i] = Files.readAllBytes(shareFiles[i].toPath());
        }
        byte[] reconstructed = combine(shares);
        Files.write(outputFile.toPath(), reconstructed);
    }

    private int n;

    public int getN() {
        return n;
    }

    private Random rng;
}
