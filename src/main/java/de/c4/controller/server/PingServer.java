package main.java.de.c4.controller.server;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

import main.java.de.c4.controller.shared.Network;

/**
 * Einfacher Thread, in dem der UDP-Server laeuft, der host discoverys beantwortet
 * @author Artur Dawtjan
 *
 */
public class PingServer extends Thread{
	
	private Server server;

	public PingServer () throws IOException {
		server = new Server() {
			protected Connection newConnection () {
				return new PingConnection();
			}
		};

		// eigentlich gehts nur um den udp-Port.
		server.bind(Network.UDP_PORT-1, Network.UDP_PORT);
		
	}

	@Override
	public void run() {
		server.start();
	}

	/**
	 * Dummy, braucht halt Irgendwas...
	 * @author Artur Dawtjan
	 *
	 */
	static class PingConnection extends Connection {
	}
}