package main.java.de.c4.controller.shared;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

public class AESerializer<T> extends Serializer<T>{
	private final Serializer<T> serializer;
	private static final int BUFFER_SIZE = 256;
	private static final byte[] salt = "ThisIsASecretKey".getBytes();
	private static final SecretKeySpec keySpec = new SecretKeySpec(salt, 0, 16, "AES");
	private static final IvParameterSpec IV = new IvParameterSpec("1234567812345678".getBytes());
	public AESerializer (Serializer<T> serializer) {
		this.serializer = serializer;
	}

	@SuppressWarnings("unchecked")
	public void write (Kryo kryo, Output output, Object object) {
		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
		CipherOutputStream cipherStream = new CipherOutputStream(new BASE64EncoderStream(output), cipher);
		Output cipherOutput = new Output(cipherStream, BUFFER_SIZE) {
			public void close () throws KryoException {
				// Don't allow the CipherOutputStream to close the output.
			}
		};
		serializer.write(kryo, cipherOutput, (T)object);
		cipherOutput.flush();
		try {
			cipherStream.close();
		} catch (IOException ex) {
			throw new KryoException(ex);
		}
	}

	public T read (Kryo kryo, Input input, Class<T> type) {
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
		
		CipherInputStream cipherInput = new CipherInputStream(new BASE64DecoderStream(input), cipher);
		return serializer.read(kryo, new Input(cipherInput, BUFFER_SIZE), type); 
	}

	@SuppressWarnings("unchecked")
	public Object copy (Kryo kryo, Object original) {
		return serializer.copy(kryo, (T)original);
	}

	static private Cipher getCipher (int mode) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(mode, keySpec, IV);
			return cipher;
		} catch (Exception ex) {
			throw new KryoException(ex);
		}
	}
}
