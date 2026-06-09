package com.Ai.Controller;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.ArrayList;

@RestController
public class DataInitController {
    @Autowired
    private VectorStore vectorStore;

    @GetMapping("/init-data")
    public String initData() {
        try {
            // 1. 直接交给 Tika 解析资源文件
            ClassPathResource resource = new ClassPathResource("学生手册答案.docx");
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.get();

            // 2. 再切分
            TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 10000, true);
            List<Document> chunks = splitter.apply(documents);

            // 3. 分批入向量库（每批最多 10 个）
            int batchSize = 10;
            for (int i = 0; i < chunks.size(); i += batchSize) {
                int end = Math.min(i + batchSize, chunks.size());
                List<Document> batch = chunks.subList(i, end);
                vectorStore.add(batch);
            }

            return "恭喜！Tika 解析并导入了 " + chunks.size() + " 条知识碎片。";

        } catch (Exception e) {
            e.printStackTrace();
            return "导入失败：" + e.getMessage();
        }
    }
}

