package secretsharing;

import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

import java.util.Random;

public class ErrorCorrectingCode {

    public static int[] encode(byte[] data, int maxErrors) {
        // TODO
        return null;
    }

    public static byte[] decode(int[] encodedData, int maxErrors) {
        // TODO
        return null;
    }

    public static void introduceErrors(int[] data, int errors) {
        Random random = new Random();
        for (int i = 0; i < errors; i++) {
            int index = random.nextInt(data.length);
            data[index] = random.nextInt(256);
        }
    }

    public static byte[] toByteArray(int[] ints) {
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte) ints[i];
        }
        return result;
    }
}