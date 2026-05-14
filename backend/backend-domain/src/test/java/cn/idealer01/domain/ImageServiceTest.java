package cn.idealer01.domain;

import cn.idealer01.domain.article.adapter.repository.IImageRepository;
import cn.idealer01.domain.article.adapter.repository.IImageStorageRepository;
import cn.idealer01.domain.article.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private IImageRepository imageRepository;

    @Mock
    private IImageStorageRepository imageStorageRepository;

    @Test
    public void uploadImage_shouldPersistUrlReturnedByStorageRepository() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "imageRepository", imageRepository);
        ReflectionTestUtils.setField(imageService, "imageStorageRepository", imageStorageRepository);

        byte[] fileData = "image-data".getBytes();
        String uploadedUrl = "https://cdn.example.com/blog-images/fixed-cover.png";
        when(imageStorageRepository.upload(eq(fileData), anyString())).thenReturn(uploadedUrl);

        Map<String, String> result = imageService.uploadImage(fileData, "cover.png", 7L, "documents/my-article");

        org.junit.jupiter.api.Assertions.assertEquals(uploadedUrl, result.get("url"));
        verify(imageRepository).save(eq("cover.png"), eq(uploadedUrl), eq((long) fileData.length), eq(7L));
    }

    @Test
    public void uploadImage_shouldPrefixObjectKeyWithDirectory() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "imageRepository", imageRepository);
        ReflectionTestUtils.setField(imageService, "imageStorageRepository", imageStorageRepository);

        byte[] fileData = "image-data".getBytes();
        when(imageStorageRepository.upload(eq(fileData), anyString())).thenReturn("https://cdn.example.com/x.png");

        imageService.uploadImage(fileData, "cover.png", 7L, "documents/my-article");

        verify(imageStorageRepository).upload(eq(fileData), org.mockito.ArgumentMatchers.startsWith("documents/my-article/"));
    }
}
