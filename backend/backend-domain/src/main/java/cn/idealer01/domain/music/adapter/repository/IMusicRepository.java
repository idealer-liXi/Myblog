package cn.idealer01.domain.music.adapter.repository;

import cn.idealer01.domain.music.model.entity.MusicEntity;

import java.util.List;

public interface IMusicRepository {
    List<MusicEntity> findAll();

    List<MusicEntity> findEnabledList();

    MusicEntity findById(Long id);

    Long save(MusicEntity music);

    void update(MusicEntity music);

    void deleteById(Long id);
}
