package com.maxiaowei.it.toolbox.service;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 * <p>
 * 作者: maxiaowei
 */
@Service
public class ZipService {
    // 安全限制：最大解压后总大小
    private static final long MAX_TOTAL_SIZE = 2L * 1024 * 1024 * 1024; // 2GB
    // 安全限制：最大单个文件大小
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    // 安全限制：最大文件数量
    private static final int MAX_FILE_COUNT = 10000;

    public List<byte[]> splitZip(byte[] zipData, long maxSizeMB) throws IOException {
        long maxSizeBytes = maxSizeMB * 1024L * 1024L; // 注意这里强制转为long，避免溢出
        List<byte[]> result = new ArrayList<>();

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ByteArrayOutputStream currentPart = new ByteArrayOutputStream();
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(currentPart);

            ZipArchiveEntry entry;
            long currentPartSize = 0;
            long totalSize = 0;
            int fileCount = 0;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (entry.getSize() > MAX_FILE_SIZE) {
                    throw new IOException("文件 " + entry.getName() + " 过大，最大支持100MB");
                }
                if (++fileCount > MAX_FILE_COUNT) {
                    throw new IOException("压缩包内文件数量过多，最大支持10000个文件");
                }
                totalSize += entry.getSize();
                if (totalSize > MAX_TOTAL_SIZE) {
                    throw new IOException("解压后总大小超过2GB限制");
                }

                if (currentPartSize + entry.getSize() > maxSizeBytes && currentPartSize > 0) {
                    zos.close();
                    result.add(currentPart.toByteArray());
                    currentPart = new ByteArrayOutputStream();
                    zos = new ZipArchiveOutputStream(currentPart);
                    currentPartSize = 0;
                }

                ZipArchiveEntry newEntry = new ZipArchiveEntry(entry.getName());
                zos.putArchiveEntry(newEntry);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                    currentPartSize += len;
                }
                zos.closeArchiveEntry();
            }

            if (currentPartSize > 0) {
                zos.close();
                result.add(currentPart.toByteArray());
            }
            zos.close();
        }

        return result;
    }
}
