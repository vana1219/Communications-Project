package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ServerApp.Server.Server;

class TestServer {
	private Server server;
	
	
	@Test
	void testServerConstructor() {
		server=new Server();
		assertNotNull(server);
	}

}
