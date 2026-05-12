package com.Ai.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.ai.document.Document;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Slf4j
@RestController
@RequestMapping("/rag/tika/spring-ai")
public class SpringAiTikaDemoController {

    /**
     * 用Spring AI的TikaDocumentReader解析文件
     * 一行代码搞定：自动检测格式 → 解析内容 → 封装成Document对象
     */
    @GetMapping("/parse")
    public Map<String, Object> parse(@RequestParam("filePath") String filePath) {
        File file = new File(filePath);
        log.info("Spring AI方式解析文件: {}", filePath);

        // 核心就这一行：TikaDocumentReader自动完成检测+解析+封装
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
        List<Document> documents = reader.get();

        log.info("解析完成，得到 {} 个Document", documents.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filename", file.getName());
        result.put("documentCount", documents.size());
        result.put("documents", documents.stream().map(doc -> {
            Map<String, Object> docMap = new LinkedHashMap<>();
            docMap.put("charCount", doc.getText().length());
            docMap.put("content", doc.getText());
            docMap.put("metadata", doc.getMetadata());
            return docMap;
        }).collect(Collectors.toList()));
        return result;
    }
}