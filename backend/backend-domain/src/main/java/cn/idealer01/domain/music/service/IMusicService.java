package cn.idealer01.domain.music.service;

import cn.idealer01.api.dto.MusicAdminResponseDTO;
import cn.idealer01.api.dto.MusicPublicResponseDTO;
import cn.idealer01.api.dto.MusicRequestDTO;

import java.util.List;
import java.util.Map;

public interface IMusicService {
    List<MusicAdminResponseDTO> getMusicList();

    MusicAdminResponseDTO getMusicById(Long musicId);

    List<MusicPublicResponseDTO> getPublicMusicList();

    MusicAdminResponseDTO createMusic(MusicRequestDTO request);

    MusicAdminResponseDTO updateMusic(Long musicId, MusicRequestDTO request);

    void deleteMusic(Long musicId);

    Map<String, String> uploadAudio(byte[] fileData, String fileName);

    Map<String, String> uploadCover(byte[] fileData, String fileName);
}
