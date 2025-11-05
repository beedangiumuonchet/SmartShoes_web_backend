package com.ds.project.business.v1.services;

import com.ds.project.common.interfaces.IGoogleDriveService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService implements IGoogleDriveService {

    private static final String APPLICATION_NAME = "SmartShoesDriveUploader";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String FOLDER_ID = "1xqcSdNRVUkf1-GRBc0zgfbgPNofS5ehf";

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private Drive driveService;

    public GoogleDriveService() throws IOException, GeneralSecurityException {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport);
        this.driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final com.google.api.client.http.HttpTransport httpTransport)
            throws IOException {

        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Không tìm thấy file: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8080)
                .setCallbackPath("/drive/oauth2/callback") // THÊM DÒNG NÀY
                .build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        java.io.File tempFile = java.io.File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        File fileMetadata = new File();
        fileMetadata.setName(multipartFile.getOriginalFilename());

        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        FileContent mediaContent = new FileContent(multipartFile.getContentType(), tempFile);
        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, webContentLink")
                .execute();

        // Cho phép public đọc file
        makeFilePublic(uploadedFile.getId());

        return uploadedFile.getWebViewLink();
    }

    @Override
    public String makeFilePublic(String fileId) throws IOException {
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        driveService.permissions().create(fileId, permission).execute();

        File file = driveService.files().get(fileId)
                .setFields("webViewLink")
                .execute();

        return file.getWebViewLink();
    }
}
