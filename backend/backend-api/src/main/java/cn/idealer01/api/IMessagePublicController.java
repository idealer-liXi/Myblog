package cn.idealer01.api;

import cn.idealer01.api.dto.MessagePageResponseDTO;

public interface IMessagePublicController {
    MessagePageResponseDTO getMessages(Integer page, Integer pageSize, Boolean latestOnly, Integer limit);
}
