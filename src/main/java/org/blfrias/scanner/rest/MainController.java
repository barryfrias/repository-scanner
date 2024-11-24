package org.blfrias.scanner.rest;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.blfrias.scanner.dto.ApiInfo;
import org.blfrias.scanner.service.ScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scan")
public class MainController {
    private final ScannerService scannerService;

    @PostMapping({"/repository"})
    public ResponseEntity<Map<String, String>> scanRepository() {
        scannerService.scanRepository();
        return ResponseEntity.ok(ImmutableMap.of("message", "Scanning done."));
    }

    @GetMapping({"/results/repository"})
    public ResponseEntity<Set<ApiInfo>> showResults(
        final @RequestParam(name="httpMethod", required = false) String httpMethod,
        final @RequestParam(name="path", required = false) String path
    ) {
        var apiInfos = scannerService.getApiInfos(httpMethod, path);
        return ResponseEntity.ok(apiInfos);
    }
}