/**
 * 
 */
package secretsharing;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.function.Function;

import com.macasaet.fernet.Validator;

/**
 * Custom Fernet validator for byte arrays
 * 
 * @author elmar
 *
 */
public interface BytesValidator extends Validator<byte[]> {

	/**
	 * Interpret bytes as bytes (no transformation)
	 */
	@Override
	default Function<byte[], byte[]> getTransformer() {
		return Function.identity();
	}

	/**
	 * Allow decryption any time after encryption.
	 * By default, Fernet limits this to 60s.
	 */
	@Override
	default TemporalAmount getTimeToLive() {
		return Duration.ofSeconds(Instant.MAX.getEpochSecond());
	}
}
