package cn.idealer01.domain.article.adapter.repository;

public interface IImageStorageRepository {

    String upload(byte[] fileData, String objectKey);
}
