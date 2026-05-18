package cn.idealer01.domain.image.service;

import cn.idealer01.domain.image.adapter.repository.IImageRepository;
import cn.idealer01.domain.image.adapter.repository.IImageStorageRepository;
import cn.idealer01.domain.project.adapter.repository.IProjectRepository;
import cn.idealer01.domain.project.model.entity.ProjectEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService implements IImageService {

    private static final String DEFAULT_DIRECTORY = "documents/manual-upload";

    @Resource
    private IImageRepository imageRepository;

    @Resource
    private IImageStorageRepository imageStorageRepository;

    @Resource
    private IProjectRepository projectRepository;

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
    public List<Map<String, Object>> getProjectImages() {
        return projectRepository.findAll().stream()
                .map(this::toProjectImageRow)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProjectImageByProjectId(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId);
        return project == null ? null : toProjectImageRow(project);
    }

    @Override
    public void clearProjectImage(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId);
        if (project == null) {
            throw new AppException(ResponseCode.USER_NOT_EXIST.getCode(), "项目不存在");
        }

        project.setCoverImage("");
        project.setUpdateTime(new Date());
        projectRepository.update(project);
    }

    @Override
    public void deleteImage(Long imageId) {
        imageRepository.deleteById(imageId);
    }

    private Map<String, Object> toProjectImageRow(ProjectEntity project) {
        Map<String, Object> row = new HashMap<>();
        row.put("projectId", project.getId());
        row.put("projectName", project.getName());
        row.put("projectStatus", project.getStatus());
        row.put("coverImage", defaultString(project.getCoverImage()));
        row.put("hasCover", hasCover(project.getCoverImage()));
        row.put("updatedAt", formatDate(project.getUpdateTime()));
        return row;
    }

    private boolean hasCover(String coverImage) {
        return coverImage != null && !coverImage.trim().isEmpty();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
