package secretsharing;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;

import java.io.File;
import java.io.IOException;
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
        // TODO
        byte[] bytes = new byte[32]; // 256 Bit Key
        rng.nextBytes(bytes); // Array füllen mit Random Werten
        return new Key(bytes);
    }

    /**
     * 
     * @param inputFile Die zu verschlüsselnde Datei F
     * @param key       Der Fernet-Schlüssel
     * @return Die erzeugte Datei F.enc
     * @throws IOException Wenn beim Lesen oder Schreiben ein Fehler auftritt
     */
    public static File encrypt(File inputFile, Key key) throws IOException {
        // TODO
        byte[] data = Files.readAllBytes(inputFile.toPath()); // File als Bytes einlesen
        Token token = Token.generate(rng, key, data); // mit Fernet Verschlüsseln
        File encFile = new File(inputFile.getPath() + ".enc"); // neue Datei erzeugen
        Files.writeString(encFile.toPath(), token.serialise()); // versclüsselte Token als String in Datei
        return encFile;
    }

    /**
     * 
     * @param encFile Die verschlüsselte Datei F.enc
     * @param key     Der Fernet-Schlüssel
     * @return Die erzeugte Datei F.dec
     * @throws IOException Wenn beim Lesen oder Schreiben ein Fehler auftritt
     */
    public static File decrypt(File encFile, Key key) throws IOException {
        // TODO
        String tokenString = Files.readString(encFile.toPath()); // Token aus Datei einlesen
        BytesValidator validator = new BytesValidator() {
        };// Bytes statt Strings benutzen
        Token token = Token.fromString(tokenString);// Text zu Token
        byte[] decryptedData = token.validateAndDecrypt(key, validator);// Entschlüsseln
        File decFile = new File(encFile.getPath() + ".dec");
        Files.write(decFile.toPath(), decryptedData);// Entschlüsselte Bytes in Datei schreiben
        return decFile;
    }

    public static void main(String[] args) throws IOException {
        // TODO
        File file = new File("test.txt");
        Files.writeString(file.toPath(), "Geheimer Text");
        Key key = generateKey();
        File enc = encrypt(file, key);
        File dec = decrypt(enc, key);
        boolean match = Arrays.equals(Files.readAllBytes(file.toPath()), Files.readAllBytes(dec.toPath()));
        System.out.println("Test erfolgreich: " + match);
    }
}
