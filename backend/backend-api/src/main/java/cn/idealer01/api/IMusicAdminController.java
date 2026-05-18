package cn.idealer01.api;

import cn.idealer01.api.dto.MusicAdminResponseDTO;
import cn.idealer01.api.dto.MusicRequestDTO;
import cn.idealer01.api.response.Response;

import java.util.List;
import java.util.Map;

public interface IMusicAdminController {
    Response<List<MusicAdminResponseDTO>> getMusic();

    Response<MusicAdminResponseDTO> getMusicById(Long musicId);

    Response<MusicAdminResponseDTO> createMusic(MusicRequestDTO request);

    Response<MusicAdminResponseDTO> updateMusic(Long musicId, MusicRequestDTO request);

    Response<Void> deleteMusic(Long musicId);

    Response<Map<String, String>> uploadAudio(byte[] fileData, String fileName);

    Response<Map<String, String>> uploadCover(byte[] fileData, String fileName);
}
