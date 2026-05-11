package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class BindExistingAccountRequestDTO {
    private String pendingToken;
    private String username;
    private String password;
}
