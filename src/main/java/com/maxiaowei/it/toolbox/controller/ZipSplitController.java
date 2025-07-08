package com.maxiaowei.it.toolbox.controller;

import com.maxiaowei.it.toolbox.service.ZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
 * <p>
 * 作者: maxiaowei
 */
@Controller
@RequestMapping("/zip")
public class ZipSplitController {

    private final ZipService zipService;

    @Autowired
    public ZipSplitController(ZipService zipService) {
        this.zipService = zipService;
    }

    @GetMapping("/split")
    public String splitForm() {
        return "zip-split";
    }

    @PostMapping("/split")
    public String splitZip(
            @RequestParam MultipartFile file,
            @RequestParam long maxSizeMB,
            HttpServletResponse response) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择ZIP文件");
        }

        // 安全限制：最大处理500MB文件
        if (file.getSize() > 500 * 1024 * 1024) {
            throw new IllegalArgumentException("文件过大，最大支持500MB");
        }

        // 调用服务拆分ZIP
        List<byte[]> splitZips = zipService.splitZip(file.getBytes(), maxSizeMB);

        // 如果只拆分成一个文件，直接返回原文件
        if (splitZips.size() == 1) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"original.zip\"");
            response.getOutputStream().write(splitZips.get(0));
            return null;
        }

        // 返回拆分后的ZIP列表
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"split_zips.zip\"");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < splitZips.size(); i++) {
                ZipEntry entry = new ZipEntry("part_" + (i + 1) + ".zip");
                zipOut.putNextEntry(entry);
                zipOut.write(splitZips.get(i));
                zipOut.closeEntry();
            }
        }

        return null;
    }
}
