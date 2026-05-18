package cn.idealer01.domain.image.adapter.repository;

public interface IImageStorageRepository {

    String upload(byte[] fileData, String objectKey);
}
