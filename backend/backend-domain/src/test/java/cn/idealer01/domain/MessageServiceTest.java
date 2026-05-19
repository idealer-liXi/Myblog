package cn.idealer01.domain;

import cn.idealer01.api.dto.MessageRequestDTO;
import cn.idealer01.domain.message.adapter.repository.IMessageRepository;
import cn.idealer01.domain.message.adapter.repository.IMessageSettingsRepository;
import cn.idealer01.domain.message.model.entity.MessageEntity;
import cn.idealer01.domain.message.model.entity.MessageSettingsEntity;
import cn.idealer01.domain.message.service.MessageService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private IMessageRepository messageRepository;

    @Mock
    private IMessageSettingsRepository messageSettingsRepository;

    @Test
    public void createMessage_shouldUsePublishedWhenAutoPublishEnabled() {
        MessageService messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "messageRepository", messageRepository);
        ReflectionTestUtils.setField(messageService, "messageSettingsRepository", messageSettingsRepository);

        when(messageSettingsRepository.getSettings()).thenReturn(MessageSettingsEntity.builder().reviewMode("AUTO_PUBLISH").build());
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> {
            MessageEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return 11L;
        });

        MessageRequestDTO request = new MessageRequestDTO();
        request.setContent("这是一个新留言");

        org.junit.jupiter.api.Assertions.assertEquals("PUBLISHED", messageService.createMessage(7L, "tester", request).getStatus());
    }

    @Test
    public void createMessage_shouldUsePendingWhenManualReviewEnabled() {
        MessageService messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "messageRepository", messageRepository);
        ReflectionTestUtils.setField(messageService, "messageSettingsRepository", messageSettingsRepository);

        when(messageSettingsRepository.getSettings()).thenReturn(MessageSettingsEntity.builder().reviewMode("MANUAL_REVIEW").build());
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(12L);

        MessageRequestDTO request = new MessageRequestDTO();
        request.setContent("需要审核的留言");

        org.junit.jupiter.api.Assertions.assertEquals("PENDING", messageService.createMessage(7L, "tester", request).getStatus());
    }

    @Test
    public void deleteOwnMessage_shouldRejectDeletingOthers() {
        MessageService messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "messageRepository", messageRepository);

        when(messageRepository.findById(8L)).thenReturn(MessageEntity.builder().id(8L).userId(99L).status("PUBLISHED").build());

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class,
                () -> messageService.deleteOwnMessage(8L, 7L));

        org.junit.jupiter.api.Assertions.assertEquals(ResponseCode.ILLEGAL_PARAMETER.getCode(), exception.getCode());
        org.junit.jupiter.api.Assertions.assertEquals("无权删除他人留言", exception.getInfo());
        verify(messageRepository, never()).update(any(MessageEntity.class));
    }

    @Test
    public void getPublicMessages_shouldReturnPublishedRowsOnly() {
        MessageService messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "messageRepository", messageRepository);

        when(messageRepository.findPublishedPage(0, 10)).thenReturn(Arrays.asList(
                MessageEntity.builder().id(1L).userId(7L).username("tester").content("公开留言").status("PUBLISHED").build()
        ));
        when(messageRepository.countPublished()).thenReturn(1L);

        org.junit.jupiter.api.Assertions.assertEquals(1, messageService.getPublicMessages(1, 10).getItems().size());
        org.junit.jupiter.api.Assertions.assertEquals("公开留言", messageService.getPublicMessages(1, 10).getItems().get(0).getContent());
    }

    @Test
    public void publishMessage_shouldSwitchPendingToPublished() {
        MessageService messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "messageRepository", messageRepository);

        MessageEntity entity = MessageEntity.builder().id(5L).userId(7L).username("tester").content("待审核留言").status("PENDING").build();
        when(messageRepository.findById(5L)).thenReturn(entity);

        org.junit.jupiter.api.Assertions.assertEquals("PUBLISHED", messageService.publishMessage(5L).getStatus());
        verify(messageRepository).update(entity);
    }
}
