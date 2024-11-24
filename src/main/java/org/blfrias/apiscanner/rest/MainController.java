package org.blfrias.apiscanner.rest;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.blfrias.apiscanner.dto.ApiInfo;
import org.blfrias.apiscanner.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scan")
public class MainController {
    private final FileService fileService;

    @PostMapping({"/repository"})
    public ResponseEntity<Map<String, String>> scanRepository() {
        fileService.scanRepository();
        return ResponseEntity.ok(ImmutableMap.of("message", "Scanning done."));
    }

    @GetMapping({"/results/repository"})
    public ResponseEntity<List<ApiInfo>> showResults(
        @RequestParam(name="httpMethod", required = false) String httpMethod,
        @RequestParam(name="path", required = false) String path
    ) {
        var apiInfos = fileService.getApiInfos(httpMethod, path);
        return ResponseEntity.ok(apiInfos);
    }
}