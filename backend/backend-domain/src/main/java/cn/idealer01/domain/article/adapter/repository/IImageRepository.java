package cn.idealer01.domain.article.adapter.repository;

import java.util.List;
import java.util.Map;

public interface IImageRepository {

    List<Map<String, Object>> findAll();

    Map<String, Object> findById(Long id);

    Map<String, Object> save(String fileName, String ossUrl, long fileSize, Long uploaderId);

    void deleteById(Long id);
}
