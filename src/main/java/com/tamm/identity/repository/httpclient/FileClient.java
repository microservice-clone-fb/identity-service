package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.UploadFileRequest;
import com.tamm.identity.dto.response.FileResponse;

@FeignClient(
        name = "file-service",
        url = "${app.services.file}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {

    /**
     * Upload media file
     *
     * @param file    multipart file
     * @param request metadata of file (json)
     * @return wrapped FileResponse
     */
    @PostMapping(value = "/internal/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> uploadMedia(
            @RequestPart("file") MultipartFile file, @RequestPart("request") UploadFileRequest request);

    /**
     * Download media file
     *
     * @param fileName file name on server
     * @return file as Resource
     */
    @GetMapping(value = "/internal/media/download/{fileName}")
    ResponseEntity<Resource> downloadMedia(@PathVariable("fileName") String fileName);
}
