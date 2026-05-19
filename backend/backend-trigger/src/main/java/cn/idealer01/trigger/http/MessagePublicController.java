package cn.idealer01.trigger.http;

import cn.idealer01.api.IMessagePublicController;
import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.domain.message.service.IMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/public/messages")
public class MessagePublicController implements IMessagePublicController {

    @Resource
    private IMessageService messageService;

    @Override
    @GetMapping("")
    public MessagePageResponseDTO getMessages(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Boolean latestOnly,
                                              @RequestParam(required = false) Integer limit) {
        int resolvedPageSize = Boolean.TRUE.equals(latestOnly) && limit != null ? limit : pageSize;
        return messageService.getPublicMessages(page, resolvedPageSize);
    }
}
