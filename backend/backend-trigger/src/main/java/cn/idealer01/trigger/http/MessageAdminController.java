package cn.idealer01.trigger.http;

import cn.idealer01.api.IMessageAdminController;
import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.dto.MessageSettingsRequestDTO;
import cn.idealer01.api.dto.MessageSettingsResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.message.service.IMessageService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin")
public class MessageAdminController implements IMessageAdminController {

    @Resource
    private IMessageService messageService;

    @Override
    @GetMapping("/messages")
    public Response<MessagePageResponseDTO> getMessages(@RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Response.<MessagePageResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(messageService.getAdminMessages(status, page, pageSize))
                .build();
    }

    @Override
    @PutMapping("/messages/{messageId}/publish")
    public Response<MessageResponseDTO> publishMessage(@PathVariable Long messageId) {
        try {
            return Response.<MessageResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(messageService.publishMessage(messageId))
                    .build();
        } catch (AppException e) {
            return Response.<MessageResponseDTO>builder().code(e.getCode()).info(e.getInfo()).build();
        }
    }

    @Override
    @PutMapping("/messages/{messageId}/reject")
    public Response<MessageResponseDTO> rejectMessage(@PathVariable Long messageId) {
        try {
            return Response.<MessageResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(messageService.rejectMessage(messageId))
                    .build();
        } catch (AppException e) {
            return Response.<MessageResponseDTO>builder().code(e.getCode()).info(e.getInfo()).build();
        }
    }

    @Override
    @DeleteMapping("/messages/{messageId}")
    public Response<Void> deleteMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteAdminMessage(messageId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder().code(e.getCode()).info(e.getInfo()).build();
        }
    }

    @Override
    @GetMapping("/message-settings")
    public Response<MessageSettingsResponseDTO> getSettings() {
        return Response.<MessageSettingsResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(messageService.getSettings())
                .build();
    }

    @Override
    @PutMapping("/message-settings")
    public Response<MessageSettingsResponseDTO> updateSettings(@RequestBody MessageSettingsRequestDTO request) {
        try {
            return Response.<MessageSettingsResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(messageService.updateSettings(request))
                    .build();
        } catch (AppException e) {
            return Response.<MessageSettingsResponseDTO>builder().code(e.getCode()).info(e.getInfo()).build();
        }
    }
}
