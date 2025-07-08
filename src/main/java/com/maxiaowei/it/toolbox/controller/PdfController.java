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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 功能描述: 
 *
 * 作者: maxiaowei
 */
@Controller
@RequestMapping("/pdf")
public class PdfController {

    private final PdfService pdfService;

    @Autowired
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String pdfForm() {
        return "pdf";
    }

    @PostMapping("/split")
    public String splitPdf(
            @RequestParam MultipartFile file,
            @RequestParam int maxSizeMB,
            HttpServletResponse response) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择PDF文件");
        }

        List<byte[]> splitPdfs = pdfService.splitPdf(file.getBytes(), maxSizeMB);

        if (splitPdfs.size() == 1) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"original.pdf\"");
            response.getOutputStream().write(splitPdfs.get(0));
            return null;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"split_pdfs.zip\"");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < splitPdfs.size(); i++) {
                ZipEntry entry = new ZipEntry("part_" + (i+1) + ".pdf");
                zipOut.putNextEntry(entry);
                zipOut.write(splitPdfs.get(i));
                zipOut.closeEntry();
            }
        }

        return null;
    }
}
