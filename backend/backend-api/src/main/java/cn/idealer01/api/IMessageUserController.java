package cn.idealer01.api;

import cn.idealer01.api.dto.MessageRequestDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.response.Response;

import java.util.List;

public interface IMessageUserController {
    Response<MessageResponseDTO> createMessage(MessageRequestDTO request);

    Response<List<MessageResponseDTO>> getMyMessages();

    Response<Void> deleteOwnMessage(Long messageId);
}
