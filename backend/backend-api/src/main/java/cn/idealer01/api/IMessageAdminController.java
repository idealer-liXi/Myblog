package cn.idealer01.api;

import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.dto.MessageSettingsRequestDTO;
import cn.idealer01.api.dto.MessageSettingsResponseDTO;
import cn.idealer01.api.response.Response;

public interface IMessageAdminController {
    Response<MessagePageResponseDTO> getMessages(String status, Integer page, Integer pageSize);

    Response<MessageResponseDTO> publishMessage(Long messageId);

    Response<MessageResponseDTO> rejectMessage(Long messageId);

    Response<Void> deleteMessage(Long messageId);

    Response<MessageSettingsResponseDTO> getSettings();

    Response<MessageSettingsResponseDTO> updateSettings(MessageSettingsRequestDTO request);
}
