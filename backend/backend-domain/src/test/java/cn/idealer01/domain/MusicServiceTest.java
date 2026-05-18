package cn.idealer01.domain;

import cn.idealer01.api.dto.MusicRequestDTO;
import cn.idealer01.domain.image.adapter.repository.IImageStorageRepository;
import cn.idealer01.domain.music.adapter.repository.IMusicRepository;
import cn.idealer01.domain.music.model.entity.MusicEntity;
import cn.idealer01.domain.music.service.MusicService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MusicServiceTest {

    @Mock
    private IMusicRepository musicRepository;

    @Mock
    private IImageStorageRepository imageStorageRepository;

    @Test
    public void createMusic_shouldPersistValidatedFields() {
        MusicService musicService = new MusicService();
        ReflectionTestUtils.setField(musicService, "musicRepository", musicRepository);

        MusicRequestDTO request = new MusicRequestDTO();
        request.setName("夜曲");
        request.setArtist("周杰伦");
        request.setAudioUrl("https://cdn.example.com/music/audio/yequ.mp3");
        request.setCoverImage("https://cdn.example.com/music/cover/yequ.jpg");
        request.setSortOrder(9);
        request.setEnabled(Boolean.TRUE);

        when(musicRepository.save(any(MusicEntity.class))).thenReturn(3L);

        org.junit.jupiter.api.Assertions.assertEquals(3L, musicService.createMusic(request).getId());
        verify(musicRepository).save(any(MusicEntity.class));
    }

    @Test
    public void createMusic_shouldRejectBlankName() {
        MusicService musicService = new MusicService();

        MusicRequestDTO request = new MusicRequestDTO();
        request.setName("   ");
        request.setArtist("周杰伦");
        request.setAudioUrl("https://cdn.example.com/music/audio/yequ.mp3");

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class,
                () -> musicService.createMusic(request));

        org.junit.jupiter.api.Assertions.assertEquals(ResponseCode.ILLEGAL_PARAMETER.getCode(), exception.getCode());
        org.junit.jupiter.api.Assertions.assertEquals("歌曲名不能为空", exception.getInfo());
    }

    @Test
    public void getPublicMusicList_shouldMapRepositoryRows() {
        MusicService musicService = new MusicService();
        ReflectionTestUtils.setField(musicService, "musicRepository", musicRepository);

        when(musicRepository.findEnabledList()).thenReturn(Arrays.asList(
                MusicEntity.builder().id(1L).name("稻香").artist("周杰伦").audioUrl("a.mp3").coverImage("a.jpg").sortOrder(1).enabled(Boolean.TRUE).build(),
                MusicEntity.builder().id(2L).name("晴天").artist("周杰伦").audioUrl("b.mp3").coverImage("").sortOrder(2).enabled(Boolean.TRUE).build()
        ));

        org.junit.jupiter.api.Assertions.assertEquals(2, musicService.getPublicMusicList().size());
        org.junit.jupiter.api.Assertions.assertEquals("稻香", musicService.getPublicMusicList().get(0).getName());
    }

    @Test
    public void uploadAudio_shouldUploadIntoMusicAudioDirectory() {
        MusicService musicService = new MusicService();
        ReflectionTestUtils.setField(musicService, "imageStorageRepository", imageStorageRepository);

        byte[] fileData = "audio-data".getBytes();
        when(imageStorageRepository.upload(eq(fileData), anyString())).thenReturn("https://cdn.example.com/music/audio/fixed.mp3");

        Map<String, String> result = musicService.uploadAudio(fileData, "song.mp3");

        org.junit.jupiter.api.Assertions.assertEquals("https://cdn.example.com/music/audio/fixed.mp3", result.get("url"));
        verify(imageStorageRepository).upload(eq(fileData), org.mockito.ArgumentMatchers.startsWith("music/audio/"));
    }

    @Test
    public void deleteMusic_shouldThrowWhenMusicDoesNotExist() {
        MusicService musicService = new MusicService();
        ReflectionTestUtils.setField(musicService, "musicRepository", musicRepository);

        when(musicRepository.findById(99L)).thenReturn(null);

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class,
                () -> musicService.deleteMusic(99L));

        org.junit.jupiter.api.Assertions.assertEquals(ResponseCode.USER_NOT_EXIST.getCode(), exception.getCode());
        org.junit.jupiter.api.Assertions.assertEquals("音乐不存在", exception.getInfo());
    }
}
