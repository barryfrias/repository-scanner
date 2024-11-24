package org.blfrias.apiscanner.service;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.blfrias.apiscanner.dto.ApiInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final JavaFileParserService javaFileParserService;
    private final List<ApiInfo> apiInfos = new ArrayList<>();

    @Value("${app.repository.id}")
    private String repositoryId;

    private final GoogleDriveService googleDriveService;

    public void scanRepository() {
        try {
            final var query = "'" + repositoryId + "' in parents";
            FileList result = googleDriveService.getInstance().files().list()
                .setQ(query)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
            result.getFiles().forEach(file -> walkFolder(file.getId()));
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void walkFolder(String folderId) {
        try {
            final var result = googleDriveService.getInstance().files().list()
                .setQ("'" + folderId + "' in parents")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, mimeType)")
                .execute();

            result.getFiles().parallelStream().forEach(file -> {
                if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                    walkFolder(file.getId());
                } else if (file.getName().endsWith("Controller.java")) {
                    this.readFile(file);
                }
            });
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public void readFile(File file) {
        try {
            final var discoveredApiInfos = javaFileParserService.parseApiInfo(googleDriveService.getInstance().files().get(file.getId()).executeMediaAsInputStream());
            apiInfos.addAll(discoveredApiInfos);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<ApiInfo> getApiInfos(String httpMethod, String path) {
        return this.apiInfos.stream()
            .filter(apiInfo -> httpMethod == null || apiInfo.getHttpMethod().equalsIgnoreCase(httpMethod))
            .filter(apiInfo -> path == null || apiInfo.getPath().contains(path))
            .collect(Collectors.toList());
    }
}