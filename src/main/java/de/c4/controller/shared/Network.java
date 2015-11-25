package main.java.de.c4.controller.shared;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.EndPoint;

import main.java.de.c4.model.messages.Alert;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.PubKey;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.model.messages.file.FileChunk;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;

// This class is a convenient place to keep things common to both the client and server.
public class Network {
	static public final int TCP_PORT = 55555;
	static public final int UDP_PORT = 54555;
	static Set<Integer> usedPorts = new HashSet<Integer>();
	static public final int DEFAULT_TIMEOUT = 3000;
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	// This registers objects that are going to be sent over the network.
	static public void register (EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();

		kryo.register(byte[].class);
		kryo.register(PubKey.class);
		kryo.register(OnlineStateChange.class, new AESerializer<OnlineStateChange>(new FieldSerializer<OnlineStateChange>(kryo, OnlineStateChange.class)));
		kryo.register(RequestKnownOnlineClients.class, new AESerializer<RequestKnownOnlineClients>(new FieldSerializer<RequestKnownOnlineClients>(kryo, RequestKnownOnlineClients.class)));
		kryo.register(ContactDto.class);
		kryo.register(String[].class, new AESerializer<String[]>(new DefaultArraySerializers.StringArraySerializer()));
		kryo.register(ContactDto[].class, new AESerializer<Object[]>(new DefaultArraySerializers.ObjectArraySerializer(kryo, ContactDto[].class)));
		kryo.register(ContactListDto.class, new AESerializer<ContactListDto>(new FieldSerializer<ContactListDto>(kryo, ContactListDto.class)));
		kryo.register(ChatMessage.class, new AESerializer<ChatMessage>(new FieldSerializer<ChatMessage>(kryo, ChatMessage.class)));
		kryo.register(FileTransferAnswer.class, new AESerializer<FileTransferAnswer>(new FieldSerializer<FileTransferAnswer>(kryo, FileTransferAnswer.class)));
		kryo.register(FileTransferRequest.class, new AESerializer<FileTransferRequest>(new FieldSerializer<FileTransferRequest>(kryo, FileTransferRequest.class)));
		kryo.register(Alert.class, new AESerializer<Alert>(new FieldSerializer<Alert>(kryo, Alert.class)));

	}

	public static void registerFileTransfer(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(PubKey.class);
		kryo.register(byte[].class);
		kryo.register(FileChunk.class, new AESerializer<FileChunk>(new FieldSerializer<FileChunk>(kryo, FileChunk.class)));
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