package cn.idealer01.api;

import cn.idealer01.api.response.Response;

import java.util.List;
import java.util.Map;

public interface IImageAdminController {
    Response<Map<String, String>> uploadImage(byte[] fileData, String fileName, String directory);
    Response<List<Map<String, Object>>> getImages();
    Response<Void> deleteImage(Long imageId);
}
