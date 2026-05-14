package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.article.adapter.repository.IImageRepository;
import cn.idealer01.infrastructure.dao.IImageRecordDao;
import cn.idealer01.infrastructure.dao.po.ImageRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageRepository implements IImageRepository {

    @Resource
    private IImageRecordDao imageRecordDao;

    @Override
    public List<Map<String, Object>> findAll() {
        return imageRecordDao.queryAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> findById(Long id) {
        return toMap(imageRecordDao.queryById(id));
    }

    @Override
    public Map<String, Object> save(String fileName, String ossUrl, long fileSize, Long uploaderId) {
        ImageRecord record = ImageRecord.builder()
                .fileName(fileName)
                .ossUrl(ossUrl)
                .fileSize(fileSize)
                .uploaderId(uploaderId)
                .build();
        imageRecordDao.insert(record);
        return toMap(record);
    }

    @Override
    public void deleteById(Long id) {
        imageRecordDao.deleteById(id);
    }

    private Map<String, Object> toMap(ImageRecord record) {
        if (null == record) {
            return null;
        }

        Map<String, Object> image = new HashMap<>();
        image.put("id", record.getId());
        image.put("fileName", record.getFileName());
        image.put("url", record.getOssUrl());
        image.put("fileSize", record.getFileSize());
        image.put("uploadedAt", record.getCreateTime());
        return image;
    }
}
