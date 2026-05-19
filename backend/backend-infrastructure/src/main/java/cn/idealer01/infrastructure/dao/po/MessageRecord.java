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
public class MessageRecord {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String status;
    private String sentiment;
    private Date createTime;
    private Date updateTime;
}
