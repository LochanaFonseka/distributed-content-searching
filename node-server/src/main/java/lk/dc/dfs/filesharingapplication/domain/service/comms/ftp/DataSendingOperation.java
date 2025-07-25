package lk.dc.dfs.filesharingapplication.domain.service.comms.ftp;


import lk.dc.dfs.filesharingapplication.domain.service.core.FileManager;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class DataSendingOperation implements Runnable {

    private static final Logger LOG = Logger.getLogger(DataSendingOperation.class.getName());

    private final Socket clientSocket;
    private final String userName;
    private BufferedReader in;

    public DataSendingOperation(Socket clientSocket, String userName) {
        this.clientSocket = Objects.requireNonNull(clientSocket, "Client socket cannot be null");
        this.userName = Objects.requireNonNull(userName, "Username cannot be null");
    }

    @Override
    public void run() {
        try (DataInputStream dIn = new DataInputStream(clientSocket.getInputStream())) {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String fileName = dIn.readUTF();

            if (fileName != null) {
                File requestedFile = FileManager.getInstance("").getFile(fileName);
                if (requestedFile.exists()) {
                    sendFile(requestedFile);
                } else {
                    LOG.severe("Requested file not found: " + fileName);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error during file transfer operation", e);
        } finally {
            closeResources();
        }
    }

    public void sendFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = clientSocket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os)) {

            byte[] fileBytes = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileBytes, 0, fileBytes.length);

            dos.writeUTF(file.getName());
            dos.writeLong(fileBytes.length);
            dos.write(fileBytes, 0, fileBytes.length);
            dos.flush();

            LOG.fine("File " + file.getName() + " sent successfully to " + userName);
        } catch (FileNotFoundException e) {
            LOG.severe("File not found: " + file.getName());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error sending file: " + file.getName(), e);
        }
    }

    private void closeResources() {
        try {
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error closing resources", e);
        }
    }
}
