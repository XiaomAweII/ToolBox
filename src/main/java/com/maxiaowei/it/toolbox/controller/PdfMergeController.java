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
            HttpServletResponse response) throws IOException {

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("请至少选择一个PDF文件");
        }

        byte[] mergedPdf = pdfService.mergePdfs(files);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"merged.pdf\"");
        response.getOutputStream().write(mergedPdf);

        return null;
    }
}
