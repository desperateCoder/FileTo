package main.java.de.c4.controller.shared;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import main.java.de.c4.model.messages.Alert;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.model.messages.file.FileChunk;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;

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
		kryo.register(OnlineStateChange.class);
		kryo.register(RequestKnownOnlineClients.class);
		kryo.register(ContactDto.class);
		kryo.register(ContactDto[].class);
		kryo.register(ContactListDto.class);
		kryo.register(EOnlineState.class);
		kryo.register(String[].class);
		kryo.register(ChatMessage.class);
		kryo.register(FileTransferAnswer.class);
		kryo.register(FileTransferRequest.class);
		kryo.register(Alert.class);
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