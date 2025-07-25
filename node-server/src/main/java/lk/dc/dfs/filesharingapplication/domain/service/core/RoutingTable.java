package lk.dc.dfs.filesharingapplication.domain.service.core;




import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.logging.Logger;


public class RoutingTable {

    private final Logger tableLogger = Logger.getLogger(RoutingTable.class.getSimpleName());
    private final List<Neighbour> neighborList;
    private final String localAddress;
    private final int localPort;

    public RoutingTable(String localAddress, int localPort) {
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.neighborList = new ArrayList<>();
    }

    public synchronized int addNeighbor(String neighborAddress, int neighborPort, int clientPort) {
        Optional<Neighbour> existing = findNeighbor(neighborAddress, neighborPort);

        if (existing.isPresent()) {
            existing.get().updatePing();
            return neighborList.size();
        }

        if (neighborList.size() >= Constants.MAX_NEIGHBOURS) {
            return 0;
        }

        neighborList.add(new Neighbour(neighborAddress, neighborPort, clientPort));
        tableLogger.log(Level.FINE, "Added new neighbor: {0}:{1}",
                new Object[]{neighborAddress, neighborPort});

        return neighborList.size();
    }

    public synchronized int removeNeighbor(String neighborAddress, int neighborPort) {
        Optional<Neighbour> toRemove = findNeighbor(neighborAddress, neighborPort);

        if (toRemove.isPresent()) {
            neighborList.remove(toRemove.get());
            return neighborList.size();
        }
        return 0;
    }

    public synchronized int getNeighborCount() {
        return neighborList.size();
    }

    public synchronized void displayRoutingInfo() {
        System.out.println("Current Neighbors: " + neighborList.size());
        System.out.println("Local Node: " + localAddress + ":" + localPort);
        System.out.println("--------------------------");
        neighborList.forEach(neighbor ->
                System.out.printf("Address: %s Port: %d Pings: %d%n",
                        neighbor.getAddress(),
                        neighbor.getPort(),
                        neighbor.getPingPongs())
        );
    }

    @Override
    public synchronized String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Neighbor Count: ").append(neighborList.size()).append("\n");
        builder.append("Local Node: ").append(localAddress).append(":").append(localPort).append("\n");
        builder.append("--------------------------\n");

        neighborList.forEach(neighbor ->
                builder.append(String.format("Address: %s Port: %d Pings: %d%n",
                        neighbor.getAddress(),
                        neighbor.getPort(),
                        neighbor.getPingPongs()))
        );

        return builder.toString();
    }

    public synchronized List<String> getNeighborStrings() {
        return neighborList.stream()
                .map(Neighbour::toString)
                .collect(Collectors.toList());
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public List<Neighbour> getAllNeighbors() {
        return new ArrayList<>(neighborList);
    }

    public boolean containsNeighbor(String neighborAddress, int neighborPort) {
        return findNeighbor(neighborAddress, neighborPort).isPresent();
    }

    public List<String> getOtherNeighbors(String excludeAddress, int excludePort) {
        return neighborList.stream()
                .filter(n -> !n.equals(excludeAddress, excludePort))
                .map(Neighbour::toString)
                .collect(Collectors.toList());
    }

    private Optional<Neighbour> findNeighbor(String address, int port) {
        return neighborList.stream()
                .filter(n -> n.equals(address, port))
                .findFirst();
    }
}
