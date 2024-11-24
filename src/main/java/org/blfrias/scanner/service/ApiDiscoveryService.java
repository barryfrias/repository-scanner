package org.blfrias.scanner.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.blfrias.scanner.dto.ApiInfo;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class ApiDiscoveryService {
    public Set<ApiInfo> listApis(final InputStream inputStream) {
        final CompilationUnit cu = StaticJavaParser.parse(inputStream);
        final Set<ApiInfo> apiInfoSet = new HashSet<>();
        cu.findAll(MethodDeclaration.class).forEach(method -> method.getAnnotations()
            .forEach(annotation -> {
                final String annotationName = annotation.getNameAsString();
                if (annotationName.equals("RequestMapping")) {
                    final var apiInfo = new ApiInfo();
                    annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (pair.getNameAsString().equals("method")) {
                            apiInfo.setHttpMethod(pair.getValue().asFieldAccessExpr().getNameAsString());
                        } else if (pair.getNameAsString().equals("value")) {
                            apiInfo.setPath(pair.getValue().asStringLiteralExpr().asString());
                        }
                    });
                    apiInfoSet.add(apiInfo);
                } else if(
                    annotationName.equals("GetMapping") ||
                    annotationName.equals("PostMapping") ||
                    annotationName.equals("DeleteMapping") ||
                    annotationName.equals("PutMapping")
                ) {
                    apiInfoSet.add(
                        ApiInfo.builder().httpMethod(getHttpMethod(annotation.getNameAsString()))
                            .path(annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString())
                            .build()
                    );
                }
            }));
        return apiInfoSet;
    }

    private static String getHttpMethod(final String annotationName) {
        return switch (annotationName) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "DeleteMapping" -> "DELETE";
            case "PutMapping" -> "PUT";
            default -> "Unknown";
        };
    }
}