package com.ds.project.common.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IGoogleDriveService {
    /**
     * Upload file lên Drive, trả về fileId (hoặc webViewLink tuỳ implement)
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * (Tùy chọn) Đặt file public (anyone with link) và trả về webViewLink
     */
    String makeFilePublic(String fileId) throws IOException;
}
