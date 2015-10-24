package main.java.de.c4.controller.shared;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

// This class is a convenient place to keep things common to both the client and server.
public class Network {
	static public final int TCP_PORT = 54555;
	static public final int UDP_PORT = 54566;
	static public final int DEFAULT_TIMEOUT = 5000;

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
		kryo.register(UpdateNames.class);
		kryo.register(ChatMessage.class);
	}


	static public class UpdateNames {
		public String[] names;
	}

	static public class ChatMessage {
		public String text;
	}
}