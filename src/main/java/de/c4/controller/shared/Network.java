package main.java.de.c4.controller.shared;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.crypto.KeyGenerator;

import main.java.de.c4.model.messages.Alert;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.model.messages.file.FileChunk;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.BlowfishSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.minlog.Log;

// This class is a convenient place to keep things common to both the client and server.
public class Network {
	static public final int TCP_PORT = 55555;
	static public final int UDP_PORT = 54555;
	static Set<Integer> usedPorts = new HashSet<Integer>();
	static public final int DEFAULT_TIMEOUT = 5000;
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	// This registers objects that are going to be sent over the network.
	static public void register (EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		byte[] key = null;
		try {
			key = KeyGenerator.getInstance("Blowfish").generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			Log.error("Could not generate Key for Encryption: "+e.getMessage());
			System.exit(2);
		}

		
		kryo.register(OnlineStateChange.class, new BlowfishSerializer(new FieldSerializer<OnlineStateChange>(kryo, OnlineStateChange.class), key));
		kryo.register(RequestKnownOnlineClients.class, new BlowfishSerializer(new FieldSerializer<RequestKnownOnlineClients>(kryo, RequestKnownOnlineClients.class), key));
		kryo.register(ContactDto.class, new BlowfishSerializer(new FieldSerializer<ContactDto>(kryo, ContactDto.class), key));
		kryo.register(ContactDto[].class, new BlowfishSerializer(new FieldSerializer<ContactDto[]>(kryo, ContactDto[].class), key));
		kryo.register(ContactListDto.class, new BlowfishSerializer(new FieldSerializer<ContactListDto>(kryo, ContactListDto.class), key));
		kryo.register(EOnlineState.class, new BlowfishSerializer(new FieldSerializer<EOnlineState>(kryo, EOnlineState.class), key));
		kryo.register(String[].class, new BlowfishSerializer(new FieldSerializer<String[]>(kryo, String[].class), key));
		kryo.register(ChatMessage.class, new BlowfishSerializer(new FieldSerializer<ChatMessage>(kryo, ChatMessage.class), key));
		kryo.register(FileTransferAnswer.class, new BlowfishSerializer(new FieldSerializer<FileTransferAnswer>(kryo, FileTransferAnswer.class), key));
		kryo.register(FileTransferRequest.class, new BlowfishSerializer(new FieldSerializer<FileTransferRequest>(kryo, FileTransferRequest.class), key));
		kryo.register(Alert.class, new BlowfishSerializer(new FieldSerializer<Alert>(kryo, Alert.class), key));

	}

	public static void registerFileTransfer(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(FileChunk.class);
		kryo.register(byte[].class);
	}

	public static int getFreePort() {
		int r;
		do {
			// get a free port between UDP-Port and TCP-Port
			r = UDP_PORT +RANDOM.nextInt(TCP_PORT-UDP_PORT-11);
		} while (usedPorts.contains(Integer.valueOf(r)));
		usedPorts.add(Integer.valueOf(r));
		return r;
	}

	public static void freePort(int port) {
		usedPorts.remove(Integer.valueOf(port));
	}

}