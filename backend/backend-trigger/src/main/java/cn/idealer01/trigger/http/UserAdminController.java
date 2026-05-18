package cn.idealer01.trigger.http;

import cn.idealer01.api.IUserAdminController;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.UserStatusUpdateRequestDTO;
import cn.idealer01.domain.auth.service.IUserAdminService;
import cn.idealer01.api.response.Response;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController implements IUserAdminController {

    @Resource
    private IUserAdminService userAdminService;

    @Override
    @GetMapping("")
    public Response<Map<String, Object>> getUsers(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String loginType,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> data = userAdminService.getUsers(keyword, loginType, status, page, pageSize);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/{userId}")
    public Response<CurrentUserResponseDTO> getUserById(@PathVariable Long userId) {
        try {
            CurrentUserResponseDTO data = userAdminService.getUserById(userId);
            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<CurrentUserResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取用户详情失败 userId:{}", userId, e);
            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PutMapping("/{userId}/status")
    public Response<CurrentUserResponseDTO> updateUserStatus(@PathVariable Long userId,
                                                              @RequestBody UserStatusUpdateRequestDTO request) {
        try {
            CurrentUserResponseDTO data = userAdminService.updateUserStatus(userId, request.getStatus());
            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<CurrentUserResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("更新用户状态失败 userId:{}", userId, e);
            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{userId}")
    public Response<Void> deleteUser(@PathVariable Long userId) {
        try {
            userAdminService.deleteUser(userId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除用户失败 userId:{}", userId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}