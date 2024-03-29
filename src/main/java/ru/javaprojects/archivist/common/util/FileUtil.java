package ru.javaprojects.archivist.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@UtilityClass
public class FileUtil {

    public static void upload(MultipartFile multipartFile, String directoryPath, String fileName) {
        if (multipartFile.isEmpty()) {
            throw new IllegalRequestDataException("File should not be empty");
        }
        File dir = new File(directoryPath);
        if (dir.exists() || dir.mkdirs()) {
            File file = new File(directoryPath + fileName);
            try (OutputStream outStream = new FileOutputStream(file)) {
                outStream.write(multipartFile.getBytes());
            } catch (IOException ex) {
                throw new IllegalRequestDataException("Failed to upload file: " + multipartFile.getOriginalFilename() +
                        ": " + ex.getMessage());
            }
        }
    }

    public static Resource download(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalRequestDataException("Failed to download file: " + resource.getFilename());
            }
        } catch (MalformedURLException ex) {
            throw new NotFoundException("File: " + fileLink + " not found");
        }
    }

    public static void delete(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Files.delete(path);
        } catch (IOException ex) {
            throw new IllegalRequestDataException("File " + fileLink + " deletion failed");
        }
    }

    public static void deleteDirIfEmpty(String path) {
        File dir = new File(path);
        if (dir.isDirectory() && Objects.requireNonNull(dir.list()).length == 0) {
            delete(path);
        }
    }

    public static void deleteDir(String path) {
        Path dirPath = Paths.get(path);
        if (Files.isDirectory(dirPath)) {
            try {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            } catch (IOException ex) {
                throw new IllegalRequestDataException("Dir: " + path + " deletion failed");
            }
        }
    }

    public static void moveFile(String file, String newDir) {
        try {
            Path filePath = Paths.get(file);
            Path newDirPath = Paths.get(newDir);
            if (Files.notExists(newDirPath)) {
                Files.createDirectories(newDirPath);
            }
            Files.move(filePath, newDirPath.resolve(filePath.getFileName()), REPLACE_EXISTING);

        } catch (IOException ex) {
            throw new IllegalRequestDataException("Failed to move " + file + " to " + newDir);
        }
    }

    public static List<String> readAllLines(MultipartFile file, boolean toUpperCase) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines().map(line -> toUpperCase ? line.toUpperCase() : line).toList();
        } catch (IOException e) {
            throw new IllegalRequestDataException("Failed to read file for groupSendingTo: " + e.getMessage());
        }
    }
}
