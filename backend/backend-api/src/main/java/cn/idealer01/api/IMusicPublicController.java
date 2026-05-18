package cn.idealer01.api;

import cn.idealer01.api.dto.MusicPublicResponseDTO;

import java.util.List;

public interface IMusicPublicController {
    List<MusicPublicResponseDTO> getMusic();
}
