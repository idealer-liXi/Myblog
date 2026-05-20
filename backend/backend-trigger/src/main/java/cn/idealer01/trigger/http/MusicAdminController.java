package cn.idealer01.trigger.http;

import cn.idealer01.api.IMusicAdminController;
import cn.idealer01.api.dto.MusicAdminResponseDTO;
import cn.idealer01.api.dto.MusicRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.music.service.IMusicService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/music")
public class MusicAdminController implements IMusicAdminController {

    private static final long MAX_AUDIO_FILE_SIZE = 50 * 1024 * 1024;
    private static final long MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024;

    @Resource
    private IMusicService musicService;

    @Override
    @GetMapping("")
    public Response<List<MusicAdminResponseDTO>> getMusic() {
        return Response.<List<MusicAdminResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(musicService.getMusicList())
                .build();
    }

    @Override
    @GetMapping("/{musicId}")
    public Response<MusicAdminResponseDTO> getMusicById(@PathVariable Long musicId) {
        MusicAdminResponseDTO data = musicService.getMusicById(musicId);
        if (data == null) {
            return Response.<MusicAdminResponseDTO>builder()
                    .code(ResponseCode.USER_NOT_EXIST.getCode())
                    .info("音乐不存在")
                    .build();
        }

        return Response.<MusicAdminResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    @Override
    @PostMapping("")
    public Response<MusicAdminResponseDTO> createMusic(@RequestBody MusicRequestDTO request) {
        try {
            return Response.<MusicAdminResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(musicService.createMusic(request))
                    .build();
        } catch (AppException e) {
            return Response.<MusicAdminResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @Override
    @PutMapping("/{musicId}")
    public Response<MusicAdminResponseDTO> updateMusic(@PathVariable Long musicId, @RequestBody MusicRequestDTO request) {
        try {
            MusicAdminResponseDTO data = musicService.updateMusic(musicId, request);
            if (data == null) {
                return Response.<MusicAdminResponseDTO>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("音乐不存在")
                        .build();
            }

            return Response.<MusicAdminResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<MusicAdminResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{musicId}")
    public Response<Void> deleteMusic(@PathVariable Long musicId) {
        try {
            musicService.deleteMusic(musicId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @Override
    public Response<Map<String, String>> uploadAudio(byte[] fileData, String fileName) {
        try {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(musicService.uploadAudio(fileData, fileName))
                    .build();
        } catch (AppException e) {
            return Response.<Map<String, String>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @Override
    public Response<Map<String, String>> uploadCover(byte[] fileData, String fileName) {
        try {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(musicService.uploadCover(fileData, fileName))
                    .build();
        } catch (AppException e) {
            return Response.<Map<String, String>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @PostMapping("/upload-audio")
    public Response<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件不能为空")
                    .build();
        }
        if (file.getSize() > MAX_AUDIO_FILE_SIZE) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("音频文件大小不能超过50MB")
                    .build();
        }
        if (!isAllowedAudio(file.getContentType(), file.getOriginalFilename())) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("仅支持常见音频文件")
                    .build();
        }

        try {
            return uploadAudio(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("读取上传音频失败", e);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("上传失败")
                    .build();
        }
    }

    @PostMapping("/upload-cover")
    public Response<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件不能为空")
                    .build();
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("仅支持图片文件")
                    .build();
        }
        if (file.getSize() > MAX_IMAGE_FILE_SIZE) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件大小不能超过5MB")
                    .build();
        }

        try {
            return uploadCover(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("读取上传封面失败", e);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("上传失败")
                    .build();
        }
    }

    private boolean isAllowedAudio(String contentType, String fileName) {
        if (contentType != null && contentType.startsWith("audio/")) {
            return true;
        }
        if (fileName == null) {
            return false;
        }

        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".mp3")
                || lowerFileName.endsWith(".wav")
                || lowerFileName.endsWith(".ogg")
                || lowerFileName.endsWith(".m4a")
                || lowerFileName.endsWith(".aac");
    }
}
