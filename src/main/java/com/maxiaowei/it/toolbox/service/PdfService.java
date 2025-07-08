package com.maxiaowei.it.toolbox.service;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述: PDF处理服务
 * <p>
 * 作者: maxiaowei
 */
@Service
public class PdfService {
    private static final float BYTES_PER_MB = 1024 * 1024;

    public List<byte[]> splitPdf(byte[] originalPdf, int maxSizeMB) throws IOException {
        float maxSizeBytes = maxSizeMB * BYTES_PER_MB;

        if (originalPdf.length <= maxSizeBytes) {
            return Collections.singletonList(originalPdf);  // 替换List.of()
        }

        try (PDDocument document = PDDocument.load(originalPdf)) {
            Splitter splitter = new Splitter();
            List<PDDocument> splitDocuments = splitter.split(document);
            List<byte[]> result = new ArrayList<>();

            for (PDDocument doc : splitDocuments) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    doc.save(baos);
                    result.add(baos.toByteArray());
                }
                doc.close();
            }

            return result;
        }
    }
}