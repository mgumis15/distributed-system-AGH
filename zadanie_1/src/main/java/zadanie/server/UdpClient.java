package zadanie.server;

import java.net.InetAddress;
import java.util.Objects;

public record UdpClient(InetAddress address, int port) {

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpClient udpClient = (UdpClient) o;
        return port == udpClient.port && Objects.equals(address, udpClient.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
