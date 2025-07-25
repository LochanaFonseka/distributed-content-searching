package lk.dc.dfs.filesharingapplication.domain.service.comms;

import lk.dc.dfs.filesharingapplication.domain.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

@Component
public class BSClient {
    private static final Logger LOG = Logger.getLogger(BSClient.class.getName());
    private final String BS_IPAddress;
    private final int BS_Port;
    private final DatagramSocket datagramSocket;

    public BSClient(
            @Value("${bootstrap.server.ip}") String bsIpAddress,
            @Value("${bootstrap.server.port}") int bsPort) throws SocketException {
        this.BS_IPAddress = Objects.requireNonNull(bsIpAddress, "Bootstrap server IP cannot be null");
        this.BS_Port = bsPort;
        this.datagramSocket = new DatagramSocket();
    }

    public List<InetSocketAddress> register(String userName, String ipAddress, int port) throws IOException {
        String request = buildRequest(Constants.REG_FORMAT, ipAddress, port, userName);
        String response = sendOrReceive(request);
        return processBSResponse(response);
    }

    public boolean unRegister(String userName, String ipAddress, int port) throws IOException {
        String request = buildRequest(Constants.UNREG_FORMAT, ipAddress, port, userName);
        String response = sendOrReceive(request);
        return processBSUnregisterResponse(response);
    }

    private String buildRequest(String format, String ipAddress, int port, String userName) {
        String payload = String.format(format, ipAddress, port, userName);
        return String.format(Constants.MSG_FORMAT, payload.length() + 5, payload);
    }

    private List<InetSocketAddress> processBSResponse(String response) {
        String[] tokens = response.split(" ");
        String status = tokens[1];

        if (!Constants.REGOK.equals(status)) {
            throw new IllegalStateException(Constants.REGOK + " not received");
        }

        int nodesCount = Integer.parseInt(tokens[2]);
        List<InetSocketAddress> nodes = new ArrayList<>();

        switch (nodesCount) {
            case 0:
                LOG.fine("Successful - No other nodes in the network");
                break;
            case 1:
            case 2:
                LOG.fine("No of nodes found: " + nodesCount);
                for (int i = 3; i < tokens.length; i += 2) {
                    nodes.add(new InetSocketAddress(tokens[i], Integer.parseInt(tokens[i+1])));
                }
                break;
            case 9999:
                LOG.severe("Failed. There are errors in your command");
                break;
            case 9998:
                LOG.severe("Failed, already registered to you, unRegister first");
                break;
            case 9997:
                LOG.severe("Failed, registered to another user, try a different IP and port");
                break;
            case 9996:
                LOG.severe("Failed, can't register. BS full.");
                break;
            default:
                throw new IllegalStateException("Invalid status code: " + nodesCount);
        }

        return nodes;
    }

    private boolean processBSUnregisterResponse(String response) {
        String[] tokens = response.split(" ");
        String status = tokens[1];

        if (!Constants.UNROK.equals(status)) {
            throw new IllegalStateException(Constants.UNROK + " not received");
        }

        int code = Integer.parseInt(tokens[2]);
        switch (code) {
            case 0:
                LOG.fine("Successfully unregistered");
                return true;
            case 9999:
                LOG.severe("Error while un-registering. IP and port may not be in the registry or command is incorrect");
                return false;
            default:
                LOG.warning("Unknown unregister response code: " + code);
                return false;
        }
    }

    private String sendOrReceive(String request) throws IOException {
        InetAddress address = InetAddress.getByName(BS_IPAddress);
        DatagramPacket sendingPacket = new DatagramPacket(
                request.getBytes(),
                request.length(),
                address,
                BS_Port
        );

        datagramSocket.setSoTimeout(Constants.TIMEOUT_REG);
        datagramSocket.send(sendingPacket);

        byte[] buffer = new byte[65536];
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(receivedPacket);

        return new String(
                receivedPacket.getData(),
                0,
                receivedPacket.getLength()
        );
    }
}
