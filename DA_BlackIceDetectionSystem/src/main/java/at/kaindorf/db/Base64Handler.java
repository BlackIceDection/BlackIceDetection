package at.kaindorf.db;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Handles encoding and decoding of Base64 Strings. Used for the camera images.
 * 
 * @author Nico Baumann
 */
public class Base64Handler{
	
	private static final Base64.Encoder ENCODER = Base64.getEncoder(); // Base64 encoder instance.
	private static final Base64.Decoder DECODER = Base64.getDecoder(); // Base64 decoder instance.
	
	/**
	 * Encodes plain UTF-8 string to Base64.
	 * @param raw The unencrypted UTF-8 plaintext.
	 * @return The Base64 encrypted string.
	 */
	public static String encodeToString(String raw){
		return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Encodes plain UTF-8 plaintext to byte array containing Base64 data.
	 * @param raw The unencrypted plaintext.
	 * @return The Base64 byte array.
	 */
	public static byte[] encodeToByteArr(String raw){
		return ENCODER.encode(raw.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Decodes Base64 string to UTF-8 plaintext.
	 * @param base64 The Base64 string.
	 * @return The UTF-8 plaintext.
	 */
	public static String decodeToString(String base64){
		return new String(DECODER.decode(base64.getBytes(StandardCharsets.UTF_8)));
	}
	
	/**
	 * Decodes Base64 string to UTF-8 byte array.
	 * @param base64 The Base64 encrypted string.
	 * @return The UTF-8 string as bytes.
	 */
	public static byte[] decodeToByteArr(String base64){
		return DECODER.decode(base64.getBytes(StandardCharsets.UTF_8));
	}
	
}
