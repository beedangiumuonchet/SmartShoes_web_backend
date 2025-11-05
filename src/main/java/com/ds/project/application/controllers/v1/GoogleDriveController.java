package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.interfaces.IGoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/drive")
@RequiredArgsConstructor
public class GoogleDriveController {

    private final IGoogleDriveService googleDriveService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = googleDriveService.uploadFile(file);
            return ResponseEntity.ok(BaseResponse.success(fileUrl, "Tải lên thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(BaseResponse.error("Lỗi khi tải lên: " + e.getMessage()));
        }
    }
}

