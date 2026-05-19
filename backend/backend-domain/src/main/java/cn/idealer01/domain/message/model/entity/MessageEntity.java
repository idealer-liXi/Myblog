package cn.idealer01.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String status;
    private String sentiment;
    private Date createTime;
    private Date updateTime;
}
