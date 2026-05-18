package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.service.UserAdminService;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    private UserAdminService userAdminService;

    @Mock
    private ILoginRepository loginRepository;

    @BeforeEach
    public void setUp() {
        userAdminService = new UserAdminService();
        ReflectionTestUtils.setField(userAdminService, "loginRepository", loginRepository);
    }

    @Test
    public void getUsers_shouldExcludeDeletedUsers() {
        when(loginRepository.queryAllCurrentUsers()).thenReturn(Arrays.asList(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").loginType("password").build(),
                CurrentUserResponseDTO.builder().id(2L).displayName("张三").status("disabled").loginType("wechat").build(),
                CurrentUserResponseDTO.builder().id(3L).displayName("已删除用户").status("deleted").loginType("password").build()
        ));

        Map<String, Object> result = userAdminService.getUsers(null, null, null, 1, 10);

        assertEquals(2L, result.get("total"));
        assertEquals(2, ((java.util.List<?>) result.get("users")).size());
    }

    @Test
    public void getUsers_shouldFilterByStatus() {
        when(loginRepository.queryAllCurrentUsers()).thenReturn(Arrays.asList(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").loginType("password").build(),
                CurrentUserResponseDTO.builder().id(2L).displayName("张三").status("disabled").loginType("wechat").build()
        ));

        Map<String, Object> result = userAdminService.getUsers(null, null, "active", 1, 10);

        assertEquals(1L, result.get("total"));
    }

    @Test
    public void getUsers_shouldFilterByLoginType() {
        when(loginRepository.queryAllCurrentUsers()).thenReturn(Arrays.asList(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").loginType("password").build(),
                CurrentUserResponseDTO.builder().id(2L).displayName("张三").status("active").loginType("wechat").build()
        ));

        Map<String, Object> result = userAdminService.getUsers(null, "wechat", null, 1, 10);

        assertEquals(1L, result.get("total"));
    }

    @Test
    public void getUsers_shouldFilterByKeyword() {
        when(loginRepository.queryAllCurrentUsers()).thenReturn(Arrays.asList(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").loginType("password").username("idealer").build(),
                CurrentUserResponseDTO.builder().id(2L).displayName("张三").status("active").loginType("wechat").build()
        ));

        Map<String, Object> result = userAdminService.getUsers("管理", null, null, 1, 10);

        assertEquals(1L, result.get("total"));
    }

    @Test
    public void getUsers_shouldPaginate() {
        when(loginRepository.queryAllCurrentUsers()).thenReturn(Arrays.asList(
                CurrentUserResponseDTO.builder().id(1L).displayName("A").status("active").loginType("password").build(),
                CurrentUserResponseDTO.builder().id(2L).displayName("B").status("active").loginType("password").build(),
                CurrentUserResponseDTO.builder().id(3L).displayName("C").status("active").loginType("password").build()
        ));

        Map<String, Object> result = userAdminService.getUsers(null, null, null, 2, 2);

        assertEquals(3L, result.get("total"));
        assertEquals(2, result.get("page"));
        assertEquals(2, result.get("pageSize"));
        assertEquals(1, ((java.util.List<?>) result.get("users")).size());
    }

    @Test
    public void getUserById_shouldReturnUser() {
        CurrentUserResponseDTO expected = CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").build();
        when(loginRepository.queryCurrentUserByUserId(1L)).thenReturn(expected);

        CurrentUserResponseDTO result = userAdminService.getUserById(1L);

        assertEquals("管理员", result.getDisplayName());
    }

    @Test
    public void getUserById_shouldThrowWhenUserNotFound() {
        when(loginRepository.queryCurrentUserByUserId(99L)).thenThrow(new AppException("0005", "用户不存在"));

        assertThrows(AppException.class, () -> userAdminService.getUserById(99L));
    }

    @Test
    public void updateUserStatus_shouldPersistAndReturnUpdatedUser() {
        CurrentUserResponseDTO before = CurrentUserResponseDTO.builder().id(7L).status("active").build();
        CurrentUserResponseDTO after = CurrentUserResponseDTO.builder().id(7L).status("disabled").build();
        when(loginRepository.queryCurrentUserByUserId(7L)).thenReturn(before).thenReturn(after);

        CurrentUserResponseDTO result = userAdminService.updateUserStatus(7L, "disabled");

        assertEquals("disabled", result.getStatus());
        verify(loginRepository).updateUserStatus(7L, 0);
    }

    @Test
    public void updateUserStatus_shouldMapActiveToOne() {
        CurrentUserResponseDTO user = CurrentUserResponseDTO.builder().id(1L).status("active").build();
        when(loginRepository.queryCurrentUserByUserId(1L)).thenReturn(user);

        userAdminService.updateUserStatus(1L, "active");

        verify(loginRepository).updateUserStatus(1L, 1);
    }

    @Test
    public void updateUserStatus_shouldRejectInvalidStatus() {
        CurrentUserResponseDTO user = CurrentUserResponseDTO.builder().id(1L).status("active").build();
        when(loginRepository.queryCurrentUserByUserId(1L)).thenReturn(user);

        assertThrows(AppException.class, () -> userAdminService.updateUserStatus(1L, "invalid"));
    }

    @Test
    public void deleteUser_shouldSoftDeleteBySettingDeletedStatus() {
        CurrentUserResponseDTO user = CurrentUserResponseDTO.builder().id(9L).status("active").build();
        when(loginRepository.queryCurrentUserByUserId(9L)).thenReturn(user);

        userAdminService.deleteUser(9L);

        verify(loginRepository).updateUserStatus(9L, 2);
    }
}