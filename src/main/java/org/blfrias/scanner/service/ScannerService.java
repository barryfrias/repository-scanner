package org.blfrias.scanner.service;

import lombok.RequiredArgsConstructor;
import org.blfrias.scanner.dto.ApiInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScannerService {
    public static final int PAGE_SIZE = 100;

    private final ApiDiscoveryService apiDiscoveryService;
    private final GoogleDriveService googleDriveService;

    private final Set<ApiInfo> apiInfos = new HashSet<>();

    @Value("${app.repository.id}")
    private String repositoryId;

    public void scanRepository() {
        try {
            apiInfos.clear();
            final var result = googleDriveService.getInstance().files().list()
                .setQ("'" + repositoryId + "' in parents and not name contains 'webapp'")
                .setPageSize(PAGE_SIZE)
                .setFields("nextPageToken, files(id)")
                .execute();
            result.getFiles().parallelStream().forEach(file -> walk(file.getId()));
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void walk(final String fileId) {
        try {
            final var result = googleDriveService.getInstance().files().list()
                .setQ(
                    "'" + fileId + "' in parents " +
                    "and name != '.mvn' " +
                    "and name != 'test' " +
                    "and name != 'resources' " +
                    "and name != 'services' " +
                    "and name != 'domain' " +
                    "and name != 'config' " +
                    "and name != 'utils' " +
                    "and name != 'Dockerfile' " +
                    "and name != '.gitignore' " +
                    "and name != 'pom.xml' " +
                    "and name != '.DS_Store' " +
                    "and name != '.project' " +
                    "and name != '.classpath' " +
                    "and not name contains 'README' " +
                    "and not name contains 'LICENSE' " +
                    "and not name contains 'mvnw' "
                )
                .setPageSize(PAGE_SIZE)
                .setFields("nextPageToken, files(id, name, mimeType)")
                .execute();

            result.getFiles().parallelStream().forEach(file -> {
                if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                    walk(file.getId());
                } else if (file.getName().endsWith("Controller.java")) {
                    readFile(file.getId());
                }
            });
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void readFile(final String fileId) {
        try {
            final var discoveredApiInfos = apiDiscoveryService.listApis(
                googleDriveService.getInstance().files().get(fileId).executeMediaAsInputStream()
            );
            apiInfos.addAll(discoveredApiInfos);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<ApiInfo> getApiInfos(final String httpMethod, final String path) {
        return this.apiInfos.stream()
            .filter(apiInfo -> httpMethod == null || apiInfo.getHttpMethod().equalsIgnoreCase(httpMethod))
            .filter(apiInfo -> path == null || apiInfo.getPath().contains(path))
            .collect(Collectors.toSet());
    }
}