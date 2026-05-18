package cn.idealer01.domain;

import cn.idealer01.api.dto.MusicAdminResponseDTO;
import cn.idealer01.api.dto.MusicPublicResponseDTO;
import cn.idealer01.domain.music.service.IMusicService;
import cn.idealer01.trigger.http.MusicAdminController;
import cn.idealer01.trigger.http.MusicPublicController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IMusicControllerTest {

    private MockMvc adminMockMvc;
    private MockMvc publicMockMvc;
    private IMusicService musicService;

    @Before
    public void setUp() {
        MusicAdminController adminController = new MusicAdminController();
        MusicPublicController publicController = new MusicPublicController();
        musicService = mock(IMusicService.class);
        ReflectionTestUtils.setField(adminController, "musicService", musicService);
        ReflectionTestUtils.setField(publicController, "musicService", musicService);
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        publicMockMvc = MockMvcBuilders.standaloneSetup(publicController).build();
    }

    @Test
    public void getAdminMusic_shouldWrapServicePayloadInUnifiedResponse() throws Exception {
        when(musicService.getMusicList()).thenReturn(Collections.singletonList(
                MusicAdminResponseDTO.builder().id(1L).name("夜曲").artist("周杰伦").enabled(Boolean.TRUE).build()
        ));

        adminMockMvc.perform(get("/api/admin/music"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data[0].name").value("夜曲"));
    }

    @Test
    public void getPublicMusic_shouldReturnPlainList() throws Exception {
        when(musicService.getPublicMusicList()).thenReturn(Collections.singletonList(
                MusicPublicResponseDTO.builder().id(1L).name("稻香").artist("周杰伦").audioUrl("a.mp3").build()
        ));

        publicMockMvc.perform(get("/api/public/music"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("稻香"))
                .andExpect(jsonPath("$[0].artist").value("周杰伦"));
    }

    @Test
    public void uploadAudio_shouldReturnUnifiedUrlPayload() throws Exception {
        when(musicService.uploadAudio(any(byte[].class), eq("song.mp3"))).thenReturn(new HashMap<String, String>() {{
            put("url", "https://cdn.example.com/music/audio/song.mp3");
        }});

        adminMockMvc.perform(multipart("/api/admin/music/upload-audio")
                        .file(new MockMultipartFile("file", "song.mp3", "audio/mpeg", "audio-data".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.url").value("https://cdn.example.com/music/audio/song.mp3"));
    }
}
