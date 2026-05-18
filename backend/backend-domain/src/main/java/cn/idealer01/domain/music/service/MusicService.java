package cn.idealer01.domain.music.service;

import cn.idealer01.api.dto.MusicAdminResponseDTO;
import cn.idealer01.api.dto.MusicPublicResponseDTO;
import cn.idealer01.api.dto.MusicRequestDTO;
import cn.idealer01.domain.image.adapter.repository.IImageStorageRepository;
import cn.idealer01.domain.music.adapter.repository.IMusicRepository;
import cn.idealer01.domain.music.model.entity.MusicEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MusicService implements IMusicService {

    private static final String AUDIO_DIRECTORY = "music/audio";
    private static final String COVER_DIRECTORY = "music/cover";

    @Resource
    private IMusicRepository musicRepository;

    @Resource
    private IImageStorageRepository imageStorageRepository;

    @Override
    public List<MusicAdminResponseDTO> getMusicList() {
        return musicRepository.findAll().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MusicAdminResponseDTO getMusicById(Long musicId) {
        MusicEntity entity = musicRepository.findById(musicId);
        return entity == null ? null : toAdminResponse(entity);
    }

    @Override
    public List<MusicPublicResponseDTO> getPublicMusicList() {
        List<MusicEntity> list = musicRepository.findEnabledList();
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MusicAdminResponseDTO createMusic(MusicRequestDTO request) {
        validateRequest(request);
        Date now = new Date();
        MusicEntity entity = MusicEntity.builder()
                .name(request.getName().trim())
                .artist(request.getArtist().trim())
                .audioUrl(request.getAudioUrl().trim())
                .coverImage(defaultString(request.getCoverImage()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .enabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled())
                .createTime(now)
                .updateTime(now)
                .build();
        Long id = musicRepository.save(entity);
        entity.setId(id);
        return toAdminResponse(entity);
    }

    @Override
    public MusicAdminResponseDTO updateMusic(Long musicId, MusicRequestDTO request) {
        MusicEntity entity = musicRepository.findById(musicId);
        if (entity == null) {
            return null;
        }

        if (request.getName() != null) {
            assertNotBlank(request.getName(), "歌曲名不能为空");
            entity.setName(request.getName().trim());
        }
        if (request.getArtist() != null) {
            assertNotBlank(request.getArtist(), "歌手不能为空");
            entity.setArtist(request.getArtist().trim());
        }
        if (request.getAudioUrl() != null) {
            assertNotBlank(request.getAudioUrl(), "音频地址不能为空");
            entity.setAudioUrl(request.getAudioUrl().trim());
        }
        if (request.getCoverImage() != null) {
            entity.setCoverImage(request.getCoverImage().trim());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }

        entity.setUpdateTime(new Date());
        musicRepository.update(entity);
        return toAdminResponse(entity);
    }

    @Override
    public void deleteMusic(Long musicId) {
        MusicEntity entity = musicRepository.findById(musicId);
        if (entity == null) {
            throw new AppException(ResponseCode.USER_NOT_EXIST.getCode(), "音乐不存在");
        }
        musicRepository.deleteById(musicId);
    }

    @Override
    public Map<String, String> uploadAudio(byte[] fileData, String fileName) {
        return uploadToDirectory(fileData, fileName, AUDIO_DIRECTORY);
    }

    @Override
    public Map<String, String> uploadCover(byte[] fileData, String fileName) {
        return uploadToDirectory(fileData, fileName, COVER_DIRECTORY);
    }

    private void validateRequest(MusicRequestDTO request) {
        if (request == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "音乐参数不能为空");
        }

        assertNotBlank(request.getName(), "歌曲名不能为空");
        assertNotBlank(request.getArtist(), "歌手不能为空");
        assertNotBlank(request.getAudioUrl(), "音频地址不能为空");
    }

    private void assertNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), message);
        }
    }

    private Map<String, String> uploadToDirectory(byte[] fileData, String fileName, String directory) {
        String extension = "";
        int dotIndex = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
        }

        String objectKey = directory + "/" + UUID.randomUUID() + extension;
        String url = imageStorageRepository.upload(fileData, objectKey);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return result;
    }

    private MusicAdminResponseDTO toAdminResponse(MusicEntity entity) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return MusicAdminResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .artist(entity.getArtist())
                .audioUrl(entity.getAudioUrl())
                .coverImage(defaultString(entity.getCoverImage()))
                .sortOrder(entity.getSortOrder())
                .enabled(entity.getEnabled())
                .createdAt(formatDate(entity.getCreateTime(), formatter))
                .updatedAt(formatDate(entity.getUpdateTime(), formatter))
                .build();
    }

    private MusicPublicResponseDTO toPublicResponse(MusicEntity entity) {
        return MusicPublicResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .artist(entity.getArtist())
                .audioUrl(entity.getAudioUrl())
                .coverImage(defaultString(entity.getCoverImage()))
                .sortOrder(entity.getSortOrder())
                .build();
    }

    private String formatDate(Date date, SimpleDateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
