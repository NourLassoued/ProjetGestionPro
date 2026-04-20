package com.example.gestionprojeet.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControlleurTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String UPLOAD_DIR = "C:/xampppidev/htdocs/img/";

    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("File uploaded successfully")));

        Path path = Paths.get(UPLOAD_DIR + "test-image.jpg");
        Files.deleteIfExists(path);
    }

    @Test
    void testGetImage_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/auth/get-image/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetImage_Success() throws Exception {
        Path path = Paths.get(UPLOAD_DIR + "test-image.jpg");
        Files.createDirectories(path.getParent());
        Files.write(path, "Test image content".getBytes());

        mockMvc.perform(get("/api/v1/auth/get-image/test-image.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        Files.deleteIfExists(path);
    }
}