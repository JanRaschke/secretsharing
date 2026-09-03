package secretsharing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShamirSecretSharing} (Tag 1, Aufgabe 2b).
 */
public class ShamirSecretSharingTest {

    private final SecureRandom rng = new SecureRandom();

    @Test
    @DisplayName("Grundlegendes Sharing und Rekonstruktion mit t Shares (t=3, n=5)")
    void testBasicSharingAndReconstruction() {
        int t = 3;
        int n = 5;
        ShamirSecretSharing sss = new ShamirSecretSharing(t, n);
        BigInteger secret = new BigInteger("12345678901234567890");

        ShamirShare[] shares = sss.share(secret);
        assertEquals(n, shares.length, "Es müssen n Shares erzeugt werden.");

        // Rekonstruktion mit den ersten t Shares (1, 2, 3)
        ShamirShare[] subset = Arrays.copyOfRange(shares, 0, t);
        BigInteger reconstructed = sss.combine(subset);

        assertEquals(secret, reconstructed, "Das rekonstruierte Geheimnis muss mit dem Original übereinstimmen.");
    }

    @Test
    @DisplayName("Rekonstruktion funktioniert mit beliebigen Teilmengen der Größe t")
    void testAnySubsetOfSizeTCanReconstruct() {
        int t = 3;
        int n = 5;
        ShamirSecretSharing sss = new ShamirSecretSharing(t, n);
        BigInteger secret = new BigInteger("987654321");

        ShamirShare[] shares = sss.share(secret);

        // Verschiedene Kombinationen von t Shares testen
        ShamirShare[][] subsets = {
            {shares[0], shares[1], shares[2]}, // Shares 1, 2, 3
            {shares[0], shares[2], shares[4]}, // Shares 1, 3, 5
            {shares[1], shares[3], shares[4]}, // Shares 2, 4, 5
            {shares[2], shares[3], shares[4]} // Shares 3, 4, 5
        };

        for (ShamirShare[] subset : subsets) {
            BigInteger reconstructed = sss.combine(subset);
            assertEquals(secret, reconstructed, "Jede Teilmenge von t Shares muss das Geheimnis rekonstruieren.");
        }
    }

    @Test
    @DisplayName("Rekonstruktion funktioniert auch mit mehr als t Shares (bis zu n)")
    void testMoreThanTSharesCanReconstruct() {
        int t = 2;
        int n = 4;
        ShamirSecretSharing sss = new ShamirSecretSharing(t, n);
        BigInteger secret = new BigInteger("42424242");

        ShamirShare[] shares = sss.share(secret);

        // Mit 3 Shares (t < 3 < n)
        ShamirShare[] threeShares = {shares[0], shares[1], shares[2]};
        assertEquals(secret, sss.combine(threeShares));

        // Mit allen 4 Shares (k = n)
        assertEquals(secret, sss.combine(shares));
    }

    @Test
    @DisplayName("Verschiedene (t, n) Parameterkonfigurationen")
    void testDifferentParameters() {
        int[][] configs = {
            {2, 2},
            {2, 4},
            {3, 6},
            {5, 10}
        };

        BigInteger secret = new BigInteger("1337");

        for (int[] cfg : configs) {
            int t = cfg[0];
            int n = cfg[1];
            ShamirSecretSharing sss = new ShamirSecretSharing(t, n);

            ShamirShare[] shares = sss.share(secret);
            assertEquals(n, shares.length);

            // Nimm beliebige t Shares
            ShamirShare[] subset = Arrays.copyOfRange(shares, 0, t);
            assertEquals(secret, sss.combine(subset), String.format("Fehler bei (t=%d, n=%d)", t, n));
        }
    }

    @Test
    @DisplayName("Verschiedene Arten von Geheimnissen (0, 1, große 256-Bit Zahlen)")
    void testVariousSecrets() {
        int t = 3;
        int n = 5;
        ShamirSecretSharing sss = new ShamirSecretSharing(t, n);

        BigInteger[] testSecrets = {
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.valueOf(42),
            new BigInteger(250, rng), // Zufällige 250-Bit Zahl (< 2^256)
            new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935") // Große 256-Bit Zahl
        };

        for (BigInteger secret : testSecrets) {
            ShamirShare[] shares = sss.share(secret);
            ShamirShare[] subset = Arrays.copyOfRange(shares, 0, t);
            assertEquals(secret, sss.combine(subset), "Fehlgeschlagen für Geheimnis: " + secret);
        }
    }
}
