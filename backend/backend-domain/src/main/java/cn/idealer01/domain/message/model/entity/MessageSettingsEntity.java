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
public class MessageSettingsEntity {
    private Long id;
    private String reviewMode;
    private Date createTime;
    private Date updateTime;
}
