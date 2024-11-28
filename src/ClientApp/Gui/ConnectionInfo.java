// New class to hold connection information
package ClientApp.Gui;

public class ConnectionInfo {
    private final String serverIP;
    private final int port;

    public ConnectionInfo(String serverIP, int port) {
        this.serverIP = serverIP;
        this.port = port;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getPort() {
        return port;
    }
}