package lk.dc.dfs.filesharingapplication.domain.service.core;


import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileManager {

    private static final Logger LOG = Logger.getLogger(FileManager.class.getName());
    private static FileManager fileManager;

    private final Map<String, String> files;
    private final String userName;
    private final String rootFolder;
    private final String fileSeparator = System.getProperty("file.separator");

    private FileManager(String userName) {
        this.userName = Objects.requireNonNull(userName, "Username cannot be null");
        this.rootFolder = "." + fileSeparator + this.userName;
        this.files = new HashMap<>();

        initializeFileStorage();
    }

    public static synchronized FileManager getInstance(String userName) {
        if (fileManager == null) {
            fileManager = new FileManager(userName);
        }
        return fileManager;
    }

    private void initializeFileStorage() {
        List<String> availableFiles = readFileNamesFromResources();
        if (!availableFiles.isEmpty()) {
            Random random = new Random();
            for (int i = 0; i < Math.min(5, availableFiles.size()); i++) {
                String randomFile = availableFiles.get(random.nextInt(availableFiles.size()));
                files.put(randomFile, "");
            }
            printFileNames();
        } else {
            LOG.warning("No files found in resources");
        }
    }

    public boolean addFile(String fileName, String filePath) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        files.put(fileName.trim(), filePath != null ? filePath.trim() : "");
        return true;
    }

    public Set<String> searchForFile(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String searchTerm = query.trim().toLowerCase();
        return files.keySet().stream()
                .filter(fileName -> fileName.toLowerCase().contains(searchTerm))
                .collect(Collectors.toSet());
    }

    private List<String> readFileNamesFromResources() {
        List<String> fileNames = new ArrayList<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Constants.FILE_NAMES);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    fileNames.add(line.trim());
                }
            }
        } catch (IOException | NullPointerException e) {
            LOG.log(Level.SEVERE, "Error reading file names from resources", e);
        }

        return fileNames;
    }

    private void printFileNames() {
        LOG.info("Total files: " + files.size());
        LOG.info("++++++++++++++++++++++++++");
        files.keySet().forEach(fileName -> {
            LOG.info(fileName);
            createFile(fileName);
        });
    }

    public List<String> getFileNames() {
        return new ArrayList<>(files.keySet());
    }

    public void createFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(rootFolder, fileName);
            Files.createDirectories(filePath.getParent());

            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
                LOG.fine("File created: " + filePath);

                try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw")) {
                    file.setLength(1024 * 1024 * 8); // 8MB file size
                }
            } else {
                LOG.fine("File already exists: " + filePath);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to create file: " + fileName, e);
        }
    }

    public File getFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        return Paths.get(rootFolder, fileName.trim()).toFile();
    }
}
