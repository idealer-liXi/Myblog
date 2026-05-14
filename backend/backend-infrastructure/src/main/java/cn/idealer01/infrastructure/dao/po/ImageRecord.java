package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRecord {

    private Long id;
    private String fileName;
    private String ossUrl;
    private Long fileSize;
    private Long uploaderId;
    private Date createTime;
}
