package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageResponseDTO {
    private List<MessageResponseDTO> items;
    private Integer page;
    private Integer pageSize;
    private Long total;
}
