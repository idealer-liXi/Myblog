package cn.idealer01.api;

import cn.idealer01.api.response.Response;

import java.util.List;
import java.util.Map;

public interface IImageAdminController {
    Response<Map<String, String>> uploadImage(byte[] fileData, String fileName, String directory);
    Response<List<Map<String, Object>>> getImages();
    Response<List<Map<String, Object>>> getProjectImages();
    Response<Map<String, Object>> getProjectImageByProjectId(Long projectId);
    Response<Void> clearProjectImage(Long projectId);
    Response<Void> deleteImage(Long imageId);
}
