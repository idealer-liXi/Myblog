package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String status;
    private String sentiment;
    private String createdAt;
    private String updatedAt;
}
