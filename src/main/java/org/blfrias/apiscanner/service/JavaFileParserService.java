package org.blfrias.apiscanner.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.blfrias.apiscanner.dto.ApiInfo;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class JavaFileParserService {
    public List<ApiInfo> parseApiInfo(InputStream inputStream) {
        final CompilationUnit cu = StaticJavaParser.parse(inputStream);
        final List<ApiInfo> apiInfoList = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(method -> method.getAnnotations()
            .forEach(annotation -> {
                final String annotationName = annotation.getNameAsString();
                if (annotationName.equals("RequestMapping")) {
                    final var apiInfo = new ApiInfo();
                    for (com.github.javaparser.ast.expr.MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
                        if (pair.getNameAsString().equals("method")) {
                            apiInfo.setHttpMethod(pair.getValue().asFieldAccessExpr().getNameAsString());
                        } else if (pair.getNameAsString().equals("value")) {
                            apiInfo.setPath(pair.getValue().asStringLiteralExpr().asString());
                        }
                    }
                    apiInfoList.add(apiInfo);
                } else if(
                    annotationName.equalsIgnoreCase("GetMapping") ||
                    annotationName.equalsIgnoreCase("PostMapping") ||
                    annotationName.equalsIgnoreCase("DeleteMapping") ||
                    annotationName.equalsIgnoreCase("PutMapping")
                ) {
                    apiInfoList.add(
                        ApiInfo.builder().httpMethod(getHttpMethod(annotation.getNameAsString()))
                            .path(annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString())
                            .build()
                    );
                }
            }));
        return apiInfoList;
    }

    public String getHttpMethod(String annotationName) {
        return switch (annotationName) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "DeleteMapping" -> "DELETE";
            case "PutMapping" -> "PUT";
            default -> "Unknown";
        };
    }
}