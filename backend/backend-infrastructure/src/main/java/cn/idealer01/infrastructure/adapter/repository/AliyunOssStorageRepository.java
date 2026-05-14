package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.article.adapter.repository.IImageStorageRepository;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AliyunOssStorageRepository implements IImageStorageRepository {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final String dirPrefix;
    private final String publicDomain;

    public AliyunOssStorageRepository(String endpoint,
                                      String accessKeyId,
                                      String accessKeySecret,
                                      String bucketName,
                                      String dirPrefix,
                                      String publicDomain) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.dirPrefix = dirPrefix;
        this.publicDomain = publicDomain;
    }

    @Override
    public String upload(byte[] fileData, String objectKey) {
        validateProperties();

        String normalizedKey = normalizeObjectKey(objectKey);
        OSS ossClient = new OSSClientBuilder().build(
                buildSdkEndpoint(),
                accessKeyId,
                accessKeySecret
        );

        try {
            ossClient.putObject(
                    bucketName,
                    normalizedKey,
                    new ByteArrayInputStream(fileData)
            );
            return buildPublicUrl(normalizedKey);
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "上传图片到 OSS 失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateProperties() {
        if (StringUtils.isAnyBlank(
                endpoint,
                accessKeyId,
                accessKeySecret,
                bucketName)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "阿里云 OSS 配置不完整，请先在 application-dev.yml 中填写 aliyun.oss 配置");
        }
    }

    private String normalizeObjectKey(String objectKey) {
        String prefix = StringUtils.defaultString(dirPrefix).trim();
        if (StringUtils.isBlank(prefix)) {
            return objectKey;
        }
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        return normalizedPrefix + objectKey;
    }

    private String buildPublicUrl(String objectKey) {
        String encodedObjectKey = encodeObjectKeyForUrl(objectKey);
        if (StringUtils.isNotBlank(publicDomain)) {
            return removeTrailingSlash(publicDomain) + "/" + encodedObjectKey;
        }
        return String.format(
                "https://%s.%s/%s",
                bucketName,
                endpoint,
                encodedObjectKey
        );
    }

    private String encodeObjectKeyForUrl(String objectKey) {
        String[] segments = objectKey.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(urlEncode(segments[i]));
        }
        return builder.toString();
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "编码 OSS 对象路径失败", e);
        }
    }

    private String buildSdkEndpoint() {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "https://" + endpoint;
    }

    private String removeTrailingSlash(String domain) {
        return domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
    }
}
