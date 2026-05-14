package cn.idealer01.domain.article.service;

import cn.idealer01.domain.article.adapter.repository.IImageRepository;
import cn.idealer01.domain.article.adapter.repository.IImageStorageRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService implements IImageService {

    private static final String DEFAULT_DIRECTORY = "documents/manual-upload";

    @Resource
    private IImageRepository imageRepository;

    @Resource
    private IImageStorageRepository imageStorageRepository;

    @Override
    public Map<String, String> uploadImage(byte[] fileData, String fileName, Long uploaderId, String directory) {
        String extension = "";
        int dotIndex = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
        }

        String generatedName = UUID.randomUUID().toString() + extension;
        String objectKey = normalizeDirectory(directory) + "/" + generatedName;
        String ossUrl = imageStorageRepository.upload(fileData, objectKey);
        imageRepository.save(fileName, ossUrl, fileData.length, uploaderId);
        Map<String, String> result = new HashMap<>();
        result.put("url", ossUrl);
        return result;
    }

    private String normalizeDirectory(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            return DEFAULT_DIRECTORY;
        }

        String normalized = directory.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            return DEFAULT_DIRECTORY;
        }

        String[] segments = normalized.split("/");
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(segment);
        }

        return builder.length() == 0 ? DEFAULT_DIRECTORY : builder.toString();
    }

    @Override
    public List<Map<String, Object>> getImages() {
        return imageRepository.findAll();
    }

    @Override
    public void deleteImage(Long imageId) {
        imageRepository.deleteById(imageId);
    }
}
