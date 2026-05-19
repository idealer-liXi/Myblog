package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.dto.MessageSettingsResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.message.service.IMessageService;
import cn.idealer01.trigger.http.MessageAdminController;
import cn.idealer01.trigger.http.MessagePublicController;
import cn.idealer01.trigger.http.MessageUserController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IMessageControllerTest {

    private MockMvc publicMockMvc;
    private MockMvc userMockMvc;
    private MockMvc adminMockMvc;
    private IMessageService messageService;
    private ILoginRepository loginRepository;

    @Before
    public void setUp() {
        MessagePublicController publicController = new MessagePublicController();
        MessageUserController userController = new MessageUserController();
        MessageAdminController adminController = new MessageAdminController();
        messageService = mock(IMessageService.class);
        loginRepository = mock(ILoginRepository.class);

        ReflectionTestUtils.setField(publicController, "messageService", messageService);
        ReflectionTestUtils.setField(userController, "messageService", messageService);
        ReflectionTestUtils.setField(userController, "loginRepository", loginRepository);
        ReflectionTestUtils.setField(adminController, "messageService", messageService);

        publicMockMvc = MockMvcBuilders.standaloneSetup(publicController).build();
        userMockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("tester", null, Collections.emptyList()));
        when(loginRepository.queryCurrentUser("tester")).thenReturn(CurrentUserResponseDTO.builder().id(7L).username("tester").build());
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getPublicMessages_shouldReturnPlainPagePayload() throws Exception {
        when(messageService.getPublicMessages(1, 10)).thenReturn(MessagePageResponseDTO.builder()
                .items(Collections.singletonList(MessageResponseDTO.builder().id(1L).username("tester").content("公开留言").status("PUBLISHED").build()))
                .page(1)
                .pageSize(10)
                .total(1L)
                .build());

        publicMockMvc.perform(get("/api/public/messages").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].content").value("公开留言"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    public void createMessage_shouldWrapCreatedPayload() throws Exception {
        when(messageService.createMessage(eq(7L), eq("tester"), any())).thenReturn(
                MessageResponseDTO.builder().id(2L).content("新留言").status("PENDING").build()
        );

        userMockMvc.perform(post("/api/user/messages")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"新留言\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void getAdminSettings_shouldWrapReviewMode() throws Exception {
        when(messageService.getSettings()).thenReturn(MessageSettingsResponseDTO.builder().reviewMode("AUTO_PUBLISH").build());

        adminMockMvc.perform(get("/api/admin/message-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewMode").value("AUTO_PUBLISH"));
    }
}
