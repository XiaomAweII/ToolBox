package com.maxiaowei.it.toolbox.service;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
            return Collections.singletonList(originalPdf);
        }

        // 第一次加载文档计算页面大小
        List<Integer> pageSizes = calculatePageSizes(originalPdf);

        // 第二次处理实际拆分
        try (PDDocument sourceDocument = PDDocument.load(originalPdf)) {
            List<byte[]> result = new ArrayList<>();
            PDDocument currentChunk = null;
            int currentSize = 0;
            int pageIndex = 0;

            for (PDPage page : sourceDocument.getPages()) {
                int pageSize = pageSizes.get(pageIndex++);

                // 初始化或创建新文档块
                if (currentChunk == null) {
                    currentChunk = new PDDocument();
                    currentSize = 0;
                }

                // 如果当前块加上新页面会超过大小限制，且当前块已有页面
                if (currentSize + pageSize > maxSizeBytes && currentChunk.getNumberOfPages() > 0) {
                    result.add(saveDocument(currentChunk));
                    currentChunk = new PDDocument();
                    currentSize = 0;
                }

                // 导入页面到当前块
                importPage(page, currentChunk);
                currentSize += pageSize;
            }

            // 添加最后一个块
            if (currentChunk != null && currentChunk.getNumberOfPages() > 0) {
                result.add(saveDocument(currentChunk));
            }

            return result;
        }
    }

    private List<Integer> calculatePageSizes(byte[] pdfData) throws IOException {
        List<Integer> sizes = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(pdfData)) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(doc);

            for (PDDocument pageDoc : pages) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    pageDoc.save(baos);
                    sizes.add(baos.size());
                } finally {
                    pageDoc.close();
                }
            }
        }
        return sizes;
    }

    private void importPage(PDPage sourcePage, PDDocument targetDocument) throws IOException {
        // 更可靠的页面导入方式
        PDPage newPage = new PDPage(sourcePage.getCOSObject());
        targetDocument.importPage(newPage);
    }

    private byte[] saveDocument(PDDocument document) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.save(baos);
            return baos.toByteArray();
        } finally {
            document.close();
        }
    }

    // 在PdfService.java中添加以下方法
    public byte[] mergePdfs(MultipartFile[] files) throws IOException {
        try (PDDocument mergedDoc = new PDDocument()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                        for (PDPage page : doc.getPages()) {
                            mergedDoc.addPage(new PDPage(page.getCOSObject()));
                        }
                    }
                }
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                mergedDoc.save(baos);
                return baos.toByteArray();
            }
        }
    }
}