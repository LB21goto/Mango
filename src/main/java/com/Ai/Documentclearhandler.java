//package com.Ai;
//
//import org.springframework.util.CollectionUtils;
//
//import javax.swing.text.Document;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class Documentclearhandler {
//    public static List<Document> clearDocumentsForFlatSplit(List<Document> documents) {
//        if (CollectionUtils.isEmpty(documents)) {
//            return documents;
//        }
//
//        return documents.stream()
//                .map(doc -> {
//                    if (doc == null || doc.getText() == null) {
//                        return doc;
//                    }
//
//                    String text = doc.getText();
//                    text = text.replaceAll("\\s+", " ").trim();
//                    text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "");
//
//                    return new Document(text, doc.getMetadata());
//                })
//                .collect(Collectors.toList());
//    }
//}
