package cn.idealer01.domain.article.service;

import java.util.List;
import java.util.Map;

public interface IImageService {

    Map<String, String> uploadImage(byte[] fileData, String fileName, Long uploaderId, String directory);

    List<Map<String, Object>> getImages();

    void deleteImage(Long imageId);
}
