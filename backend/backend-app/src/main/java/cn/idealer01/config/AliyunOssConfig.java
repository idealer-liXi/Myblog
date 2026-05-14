package cn.idealer01.config;

import cn.idealer01.domain.article.adapter.repository.IImageStorageRepository;
import cn.idealer01.infrastructure.adapter.repository.AliyunOssStorageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOssConfig {

    @Bean
    public IImageStorageRepository imageStorageRepository(AliyunOssProperties properties) {
        return new AliyunOssStorageRepository(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName(),
                properties.getDirPrefix(),
                properties.getPublicDomain()
        );
    }
}
