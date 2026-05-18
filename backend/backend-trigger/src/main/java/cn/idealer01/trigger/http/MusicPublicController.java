package cn.idealer01.trigger.http;

import cn.idealer01.api.IMusicPublicController;
import cn.idealer01.api.dto.MusicPublicResponseDTO;
import cn.idealer01.domain.music.service.IMusicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/public/music")
public class MusicPublicController implements IMusicPublicController {

    @Resource
    private IMusicService musicService;

    @Override
    @GetMapping("")
    public List<MusicPublicResponseDTO> getMusic() {
        return musicService.getPublicMusicList();
    }
}
