package cn.idealer01.domain.message.service;

import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.api.dto.MessageRequestDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.dto.MessageSettingsRequestDTO;
import cn.idealer01.api.dto.MessageSettingsResponseDTO;

import java.util.List;

public interface IMessageService {
    MessageResponseDTO createMessage(Long userId, String username, MessageRequestDTO request);

    MessagePageResponseDTO getPublicMessages(int page, int pageSize);

    List<MessageResponseDTO> getMyMessages(Long userId);

    void deleteOwnMessage(Long messageId, Long currentUserId);

    MessagePageResponseDTO getAdminMessages(String status, int page, int pageSize);

    MessageResponseDTO publishMessage(Long messageId);

    MessageResponseDTO rejectMessage(Long messageId);

    void deleteAdminMessage(Long messageId);

    MessageSettingsResponseDTO getSettings();

    MessageSettingsResponseDTO updateSettings(MessageSettingsRequestDTO request);
}
