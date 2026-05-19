package cn.idealer01.trigger.http;

import cn.idealer01.api.IMessageUserController;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.MessageRequestDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.message.service.IMessageService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user/messages")
public class MessageUserController implements IMessageUserController {

    @Resource
    private IMessageService messageService;

    @Resource
    private ILoginRepository loginRepository;

    @Override
    @PostMapping("")
    public Response<MessageResponseDTO> createMessage(@RequestBody MessageRequestDTO request) {
        try {
            return Response.<MessageResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(messageService.createMessage(getCurrentUserId(), getCurrentUsername(), request))
                    .build();
        } catch (AppException e) {
            return Response.<MessageResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/mine")
    public Response<List<MessageResponseDTO>> getMyMessages() {
        return Response.<List<MessageResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(messageService.getMyMessages(getCurrentUserId()))
                .build();
    }

    @Override
    @DeleteMapping("/{messageId}")
    public Response<Void> deleteOwnMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteOwnMessage(messageId, getCurrentUserId());
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CurrentUserResponseDTO currentUser = loginRepository.queryCurrentUser(username);
        return currentUser == null ? null : currentUser.getId();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
