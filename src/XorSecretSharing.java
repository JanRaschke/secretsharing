package secretsharing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
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
     * @param n number of shares to use. Needs to fulfill n >= 2.
     */
    public XorSecretSharing(int n) {
        assert (n >= 2);
        this.n = n;
        this.rng = new SecureRandom();
    }

    /**
     * Shares the secret into n parts.
     *
     * @param secret The secret to share.
     *
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
     * Splits a file F into n share files F-1, F-2, ..., F-n. (Aufgabe 1b)
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

    /**
     * CLI and demonstration program for Aufgabe 1.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // Run self-test and demonstration
            runSelfTest();
            return;
        }

        String command = args[0];
        try {
            if ("split".equalsIgnoreCase(command) && args.length == 3) {
                File file = new File(args[1]);
                int n = Integer.parseInt(args[2]);
                XorSecretSharing xor = new XorSecretSharing(n);
                xor.splitFile(file);
                System.out.printf("File '%s' successfully split into %d shares: %s-1 to %s-%d%n",
                        file.getName(), n, file.getName(), file.getName(), n);
            } else if ("combine".equalsIgnoreCase(command) && args.length >= 4) {
                int n = args.length - 2;
                File[] shareFiles = new File[n];
                for (int i = 0; i < n; i++) {
                    shareFiles[i] = new File(args[i + 1]);
                }
                File outputFile = new File(args[args.length - 1]);
                XorSecretSharing xor = new XorSecretSharing(n);
                xor.reconstructFile(shareFiles, outputFile);
                System.out.printf("%d shares successfully combined into '%s'%n", n, outputFile.getName());
            } else {
                printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java secretsharing.XorSecretSharing split <file> <n>");
        System.out.println("  java secretsharing.XorSecretSharing combine <share1> <share2> ... <shareN> <outputFile>");
        System.out.println("  java secretsharing.XorSecretSharing (with no args to run demo/self-test)");
    }

    private static void runSelfTest() {
        System.out.println("=== Running XOR Secret Sharing Self-Test ===");
        try {
            // Test 1: In-memory byte array
            String secretText = "Streng geheimes Passwort 1234! 🚀";
            byte[] secretBytes = secretText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int n = 5;
            XorSecretSharing xor = new XorSecretSharing(n);

            byte[][] shares = xor.share(secretBytes);
            System.out.printf("[Test 1] Original Secret (%d bytes): \"%s\"%n", secretBytes.length, secretText);
            for (int i = 0; i < n; i++) {
                System.out.printf("  Share %d: length=%d bytes%n", i + 1, shares[i].length);
            }

            byte[] reconstructedBytes = xor.combine(shares);
            String reconstructedText = new String(reconstructedBytes, java.nio.charset.StandardCharsets.UTF_8);
            boolean success = Arrays.equals(secretBytes, reconstructedBytes);
            System.out.printf("[Test 1] Reconstructed Secret: \"%s\" (Match: %b)%n", reconstructedText, success);
            if (!success) {
                throw new AssertionError("In-memory reconstruction failed!");
            }

            // Test 2: File splitting & reconstruction (Aufgabe 1b)
            File tempInput = File.createTempFile("secret_file_", ".txt");
            tempInput.deleteOnExit();
            Files.writeString(tempInput.toPath(), "Dies ist der Inhalt einer geheimen Datei für Aufgabe 1b.");

            xor.splitFile(tempInput);
            File[] shareFiles = new File[n];
            for (int i = 0; i < n; i++) {
                shareFiles[i] = new File(tempInput.getPath() + "-" + (i + 1));
                shareFiles[i].deleteOnExit();
            }

            File tempOutput = File.createTempFile("reconstructed_file_", ".txt");
            tempOutput.deleteOnExit();
            xor.reconstructFile(shareFiles, tempOutput);

            String reconstructedFileContent = Files.readString(tempOutput.toPath());
            boolean fileSuccess = reconstructedFileContent.equals("Dies ist der Inhalt einer geheimen Datei für Aufgabe 1b.");
            System.out.printf("[Test 2] File Split & Reconstruction Match: %b%n", fileSuccess);
            if (!fileSuccess) {
                throw new AssertionError("File reconstruction failed!");
            }

            System.out.println("=== All Tests Passed Successfully! ===");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
