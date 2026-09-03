package secretsharing;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;

public class FernetFileEncryption {

    private static SecureRandom rng = new SecureRandom();

    /**
     * Erzeugt einen zufälligen 256-Bit (32-Byte) Fernet-Schlüssel
     *
     * @return Ein neues Fernet Key-Objekt
     */
    public static Key generateKey() {
        byte[] bytes = new byte[32]; // 256 Bit Key
        rng.nextBytes(bytes); // Array füllen mit Random Werten
        return new Key(bytes);
    }

    /**
     *
     * @param inputFile Die zu verschlüsselnde Datei F
     * @param key Der Fernet-Schlüssel
     * @return Die erzeugte Datei F.enc
     * @throws IOException Wenn beim Lesen oder Schreiben ein Fehler auftritt
     */
    public static File encrypt(File inputFile, Key key) throws IOException {
        byte[] data = Files.readAllBytes(inputFile.toPath()); // File als Bytes einlesen
        Token token = Token.generate(rng, key, data); // mit Fernet Verschlüsseln
        File encFile = new File(inputFile.getPath() + ".enc"); // neue Datei erzeugen
        Files.writeString(encFile.toPath(), token.serialise()); // versclüsselte Token als String in Datei
        return encFile;
    }

    /**
     *
     * @param encFile Die verschlüsselte Datei F.enc
     * @param key Der Fernet-Schlüssel
     * @return Die erzeugte Datei F.dec
     * @throws IOException Wenn beim Lesen oder Schreiben ein Fehler auftritt
     */
    public static File decrypt(File encFile, Key key) throws IOException {
        String tokenString = Files.readString(encFile.toPath()); // Token aus Datei einlesen
        BytesValidator validator = new BytesValidator() {
        };// Bytes statt Strings benutzen
        Token token = Token.fromString(tokenString);// Text zu Token
        byte[] decryptedData = token.validateAndDecrypt(key, validator);// Entschlüsseln
        File decFile = new File(encFile.getPath() + ".dec");
        Files.write(decFile.toPath(), decryptedData);// Entschlüsselte Bytes in Datei schreiben
        return decFile;
    }

    // =========================================================================
    // Anpassungen für Aufgabe 4: Kombination von Secret Sharing mit Verschlüsselung
    // =========================================================================
    /**
     * Aufgabe 4a: Verschlüsselt die Datei F zur Datei F.enc und teilt den
     * 32-Byte-Schlüssel k mittels Shamirs Secret-Sharing in n Shares auf,
     * welche in den Dateien F.key.1 bis F.key.n gespeichert werden.
     *
     * @param inputFile Die zu verschlüsselnde Originaldatei F
     * @param t Schwellenwert (Threshold): Mindestanzahl an Shares zur
     * Rekonstruktion
     * @param n Gesamtanzahl zu erzeugender Shares (n >= t >= 2)
     * @return Ein Array der n erzeugten Share-Dateien
     * @throws IOException Wenn beim Lesen oder Schreiben der Dateien ein Fehler
     * auftritt
     */
    public static File[] encryptAndShare(File inputFile, int t, int n) throws IOException {
        // 1. Zufälligen 32-Byte (256-Bit) Schlüssel k als Byte-Array erzeugen
        byte[] keyBytes = new byte[32];
        rng.nextBytes(keyBytes);
        Key key = new Key(keyBytes);

        // 2. Datei F mit dem erzeugten Fernet-Schlüssel verschlüsseln -> F.enc
        encrypt(inputFile, key);

        // 3. Wandlung des Schlüssels in einen BigInteger über die Hilfsklasse BigIntegerUtils
        BigInteger secret = BigIntegerUtils.fromUnsignedByteArray(keyBytes);

        // 4. Schlüssel mit Shamirs Secret Sharing in n Teile aufteilen
        ShamirSecretSharing sss = new ShamirSecretSharing(t, n);
        ShamirShare[] shares = sss.share(secret);

        // 5. Die n Shares in den Dateien F.key.1 bis F.key.n abspeichern
        File[] shareFiles = new File[n];
        for (int i = 0; i < n; i++) {
            File shareFile = new File(inputFile.getPath() + ".key." + (i + 1));
            // Serialisierung über die in ShamirShare vorgegebene Methode writeTo(OutputStream)
            try (OutputStream os = new FileOutputStream(shareFile)) {
                shares[i].writeTo(os);
            }
            shareFiles[i] = shareFile;
        }

        return shareFiles;
    }

    /**
     * Aufgabe 4a: Rekonstruiert den Schlüssel aus einer geeigneten Anzahl k von
     * Share-Dateien (Bonuspunkt: beliebige Anzahl k zwischen t und n) und
     * entschlüsselt die Datei F.enc.
     *
     * @param encFile Die verschlüsselte Datei F.enc
     * @param shareFiles Array von k Share-Dateien (mit t <= k <= n)
     * @return Die entschlüsselte Datei F.dec
     * @throws IOException Wenn beim Lesen, Deserialisieren oder Entschlüsseln
     * Fehler auftreten
     */
    public static File reconstructAndDecrypt(File encFile, File[] shareFiles) throws IOException {
        if (shareFiles == null || shareFiles.length < 2) {
            throw new IllegalArgumentException("Es werden mindestens 2 Share-Dateien zur Rekonstruktion benötigt.");
        }

        int k = shareFiles.length; // Anzahl der übergebenen Shares
        ShamirShare[] shares = new ShamirShare[k];

        // 1. Shares aus den Dateien mittels ShamirShare.fromStream(InputStream) deserialisieren
        for (int i = 0; i < k; i++) {
            try (InputStream is = new FileInputStream(shareFiles[i])) {
                shares[i] = ShamirShare.fromStream(is);
            }
        }

        // 2. Schlüssel rekonstruieren:
        // Da die Lagrange-Interpolation nur die Punkte benötigt, initialisieren wir ShamirSecretSharing
        // mit t=k. Damit kann jede Anzahl von Shares zwischen t und n verarbeitet werden (Bonuspunkt).
        ShamirSecretSharing sss = new ShamirSecretSharing(k, k);
        BigInteger reconstructedSecret = sss.combine(shares);

        // 3. Wandlung des BigIntegers zurück in das Byte-Array über BigIntegerUtils
        byte[] rawBytes = BigIntegerUtils.toUnsignedByteArray(reconstructedSecret);

        // Sicherstellen, dass das Byte-Array genau 32 Bytes lang ist (Auffüllen führender Nullen, falls MSBs 0 waren)
        byte[] keyBytes = new byte[32];
        System.arraycopy(rawBytes, 0, keyBytes, 32 - rawBytes.length, rawBytes.length);

        // 4. Datei F.enc mit dem wiederhergestellten Schlüssel entschlüsseln
        Key key = new Key(keyBytes);
        return decrypt(encFile, key);
    }
}
