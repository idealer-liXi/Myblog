package cn.idealer01.domain.article.service;

import java.util.List;
import java.util.Map;

public interface IImageService {

    Map<String, String> uploadImage(byte[] fileData, String fileName, Long uploaderId, String directory);

    List<Map<String, Object>> getImages();

    List<Map<String, Object>> getProjectImages();

    /**
     * Returns the project image data for the given project, or {@code null} when the project does not exist.
     */
    Map<String, Object> getProjectImageByProjectId(Long projectId);

    void clearProjectImage(Long projectId);

    void deleteImage(Long imageId);
}
