package cn.idealer01.domain;

import cn.idealer01.domain.image.adapter.repository.IImageRepository;
import cn.idealer01.domain.image.adapter.repository.IImageStorageRepository;
import cn.idealer01.domain.project.adapter.repository.IProjectRepository;
import cn.idealer01.domain.project.model.entity.ProjectEntity;
import cn.idealer01.domain.image.service.ImageService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private IImageRepository imageRepository;

    @Mock
    private IImageStorageRepository imageStorageRepository;

    @Mock
    private IProjectRepository projectRepository;

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

    @Test
    public void uploadImage_shouldFallbackToDefaultDirectoryWhenDirectoryIsNull() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "imageRepository", imageRepository);
        ReflectionTestUtils.setField(imageService, "imageStorageRepository", imageStorageRepository);

        byte[] fileData = "image-data".getBytes();
        when(imageStorageRepository.upload(eq(fileData), anyString())).thenReturn("https://cdn.example.com/x.png");

        imageService.uploadImage(fileData, "cover.png", 7L, null);

        verify(imageStorageRepository).upload(eq(fileData), org.mockito.ArgumentMatchers.startsWith("documents/manual-upload/"));
    }

    @Test
    public void getProjectImages_shouldReturnProjectBackedCoverRows() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "projectRepository", projectRepository);

        when(projectRepository.findAll()).thenReturn(Collections.singletonList(
                ProjectEntity.builder()
                        .id(7L)
                        .name("MyBlog")
                        .status("已完成")
                        .coverImage("https://cdn.example.com/project-cover.png")
                        .build()
        ));

        java.util.List<Map<String, Object>> result = imageService.getProjectImages();

        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        org.junit.jupiter.api.Assertions.assertEquals(7L, result.get(0).get("projectId"));
        org.junit.jupiter.api.Assertions.assertEquals("MyBlog", result.get(0).get("projectName"));
        org.junit.jupiter.api.Assertions.assertEquals("已完成", result.get(0).get("projectStatus"));
        org.junit.jupiter.api.Assertions.assertEquals("https://cdn.example.com/project-cover.png", result.get(0).get("coverImage"));
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, result.get(0).get("hasCover"));
    }

    @Test
    public void getProjectImageByProjectId_shouldReturnProjectBackedCoverRow() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "projectRepository", projectRepository);

        when(projectRepository.findById(8L)).thenReturn(ProjectEntity.builder()
                .id(8L)
                .name("Portfolio")
                .status("进行中")
                .coverImage("")
                .build());

        Map<String, Object> result = imageService.getProjectImageByProjectId(8L);

        org.junit.jupiter.api.Assertions.assertEquals(8L, result.get("projectId"));
        org.junit.jupiter.api.Assertions.assertEquals("Portfolio", result.get("projectName"));
        org.junit.jupiter.api.Assertions.assertEquals("进行中", result.get("projectStatus"));
        org.junit.jupiter.api.Assertions.assertEquals("", result.get("coverImage"));
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE, result.get("hasCover"));
    }

    @Test
    public void getProjectImageByProjectId_shouldReturnNullWhenProjectDoesNotExist() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "projectRepository", projectRepository);

        when(projectRepository.findById(88L)).thenReturn(null);

        Map<String, Object> result = imageService.getProjectImageByProjectId(88L);

        org.junit.jupiter.api.Assertions.assertNull(result);
    }

    @Test
    public void clearProjectImage_shouldResetProjectCoverImage() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "projectRepository", projectRepository);

        ProjectEntity project = ProjectEntity.builder()
                .id(9L)
                .name("Demo")
                .coverImage("https://cdn.example.com/demo.png")
                .build();
        when(projectRepository.findById(9L)).thenReturn(project);

        imageService.clearProjectImage(9L);

        org.junit.jupiter.api.Assertions.assertEquals("", project.getCoverImage());
        verify(projectRepository).update(project);
    }

    @Test
    public void clearProjectImage_shouldThrowWhenProjectDoesNotExist() {
        ImageService imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "projectRepository", projectRepository);

        when(projectRepository.findById(99L)).thenReturn(null);

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class,
                () -> imageService.clearProjectImage(99L));

        org.junit.jupiter.api.Assertions.assertEquals(ResponseCode.USER_NOT_EXIST.getCode(), exception.getCode());
        org.junit.jupiter.api.Assertions.assertEquals("项目不存在", exception.getInfo());
        verify(projectRepository, times(0)).update(org.mockito.ArgumentMatchers.any());
    }
}
