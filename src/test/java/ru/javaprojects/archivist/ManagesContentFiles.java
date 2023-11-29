package ru.javaprojects.archivist;

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public interface ManagesContentFiles {
    String getContentPath();

    @BeforeEach
    default void generateTestDataFiles() throws IOException {
        Path contentPath = Paths.get(getContentPath());
        deleteContent(contentPath);
        createContent(contentPath);
    }

    private void deleteContent(Path contentPath) throws IOException {
        Path contentTestDataPath = Paths.get(getContentPath(), "test-data");
        Files.walkFileTree(contentPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return (dir.equals(contentTestDataPath)) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    if (!dir.equals(contentPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    private void createContent(Path contentPath) throws IOException {
        Path contentTestDataPath = Paths.get(getContentPath(), "test-data");
        Files.walkFileTree(contentTestDataPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path contentDir = contentPath.resolve(contentTestDataPath.relativize(dir));
                try {
                    Files.copy(dir, contentDir);
                } catch (FileAlreadyExistsException e) {
                    if (!Files.isDirectory(contentDir)) {
                        throw e;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, contentPath.resolve(contentTestDataPath.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
