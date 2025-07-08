package com.maxiaowei.it.toolbox.controller;

import com.maxiaowei.it.toolbox.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 功能描述:
 * <p>
 * 作者: maxiaowei
 */
@Controller
@RequestMapping("/pdf/merge")
public class PdfMergeController {

    private final PdfService pdfService;

    @Autowired
    public PdfMergeController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String mergeForm() {
        return "pdf-merge";  // 对应模板文件名
    }

    @PostMapping
    public String mergePdfs(
            @RequestParam MultipartFile[] files,
            @RequestParam String fileOrder,
            HttpServletResponse response) throws IOException {

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("请至少选择一个PDF文件");
        }

        try {
            // 按指定顺序排序文件
            List<MultipartFile> orderedFiles = orderFiles(files, fileOrder);
            byte[] mergedPdf = pdfService.mergePdfs(orderedFiles);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"merged.pdf\"");
            response.getOutputStream().write(mergedPdf);
        } finally {
            // 显式关闭所有文件流
            for (MultipartFile file : files) {
                try {
                    if (file != null && !file.isEmpty()) {
                        file.getInputStream().close();
                    }
                } catch (IOException e) {
                    // 忽略关闭错误
                }
            }
        }

        return null;
    }

    private List<MultipartFile> orderFiles(MultipartFile[] files, String fileOrder) {
        if (fileOrder == null || fileOrder.isEmpty()) {
            return Arrays.asList(files);
        }

        List<String> order = Arrays.asList(fileOrder.split(","));
        Map<String, MultipartFile> fileMap = new HashMap<>();

        for (MultipartFile file : files) {
            fileMap.put(file.getOriginalFilename(), file);
        }

        List<MultipartFile> orderedFiles = new ArrayList<>();
        for (String filename : order) {
            if (fileMap.containsKey(filename)) {
                orderedFiles.add(fileMap.get(filename));
            }
        }

        // 添加未排序的文件（如果有）
        for (MultipartFile file : files) {
            if (!order.contains(file.getOriginalFilename())) {
                orderedFiles.add(file);
            }
        }

        return orderedFiles;
    }
}
