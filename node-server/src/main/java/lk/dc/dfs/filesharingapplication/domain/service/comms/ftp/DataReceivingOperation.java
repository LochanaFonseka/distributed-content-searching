package lk.dc.dfs.filesharingapplication.domain.service.comms.ftp;


import java.io.*;
import java.net.Socket;

public class DataReceivingOperation implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private static final Logger LOGGER = Logger.getLogger(DataReceivingOperation.class.getName());

    private final Socket serverSock;
    private final String fileName;
    private BufferedReader in;

    public DataReceivingOperation(Socket serverSock, String fileName) {
        this.serverSock = Objects.requireNonNull(serverSock, "Server socket cannot be null");
        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(serverSock.getInputStream()));
            DataOutputStream dOut = new DataOutputStream(serverSock.getOutputStream());

            // Send requested file name
            dOut.writeUTF(fileName);
            dOut.flush();

            // Receive the file
            receiveFile();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during file reception", e);
        } finally {
            closeResources();
        }
    }

    public void receiveFile() {
        try (DataInputStream serverData = new DataInputStream(serverSock.getInputStream());
             OutputStream output = new FileOutputStream(serverData.readUTF())) {

            long size = serverData.readLong();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while (size > 0 &&
                    (bytesRead = serverData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            LOGGER.info("File " + fileName + " received successfully");

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "File reception failed", ex);
            throw new RuntimeException("File reception failed", ex);
        }
    }

    private void closeResources() {
        try {
            if (in != null) {
                in.close();
            }
            if (serverSock != null && !serverSock.isClosed()) {
                serverSock.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing resources", e);
        }
    }
}
