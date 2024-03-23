package ru.javaprojects.archivist.tools.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.error.ZipCreationException;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Content;
import ru.javaprojects.archivist.documents.model.ContentFile;
import ru.javaprojects.archivist.tools.GroupContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping(ToolUIController.TOOLS_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class ToolUIController {
    static final String TOOLS_URL = "/tools";
    static final String ZIP_CONTENT_TYPE = "application/zip";
    static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    static final String CONTENT_DISPOSITION_HEADER_VALUE = "attachment; filename=documents_content.zip";
    static final String OPERATION_RESULT_FILE_NAME = "operation_result.txt";
    private static final String PROCESSED_DOCUMENTS_HEADING = "----Documents whose contents have been downloaded----";
    private static final String NOT_PROCESSED_DOCUMENTS_HEADING = "----Documents whose contents were not found or documents were not found itself----";

    private final DocumentService documentService;

    @Value("${content-path.documents}")
    private String contentPath;

    @GetMapping
    public String showToolsPage() {
        log.info("show tools page");
        return "tools/tools";
    }

    @PostMapping("/group/content/download")
    public void groupContentDownload(@RequestParam("file") @NotNull @TxtFile MultipartFile file,
                                     HttpServletResponse response) throws IOException {
        log.info("group content download");
        GroupContent groupContent = documentService.getLatestContentForDocuments(file);
        response.setContentType(ZIP_CONTENT_TYPE);
        response.setHeader(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_HEADER_VALUE);
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            zos.putNextEntry(new ZipEntry(OPERATION_RESULT_FILE_NAME));
            writeLineToZipEntry(zos, PROCESSED_DOCUMENTS_HEADING);
            groupContent.operationResult().processedDocuments().forEach(document -> writeLineToZipEntry(zos, document));
            writeLineToZipEntry(zos, NOT_PROCESSED_DOCUMENTS_HEADING);
            groupContent.operationResult().notProcessedDocuments().forEach(document -> writeLineToZipEntry(zos, document));
            zos.closeEntry();
            groupContent.contents().forEach(content -> content.getFiles()
                    .forEach(contentFile -> createContentZipEntry(zos, content, contentFile)));
        }
    }

    private void writeLineToZipEntry(ZipOutputStream zos, String line) {
        try {
            zos.write((line + "\n").getBytes());
        } catch (IOException e) {
            throw new ZipCreationException(e.getMessage());
        }
    }

    private void createContentZipEntry(ZipOutputStream zos, Content content, ContentFile contentFile) {
        try {
            zos.putNextEntry(new ZipEntry(Paths.get(content.getDocument().getDecimalNumber(),
                    contentFile.getFileName()).toString()));
            Files.copy(Paths.get(contentPath, contentFile.getFileLink()), zos);
            zos.closeEntry();
        } catch (IOException e) {
            throw new ZipCreationException(e.getMessage());
        }
    }
}
