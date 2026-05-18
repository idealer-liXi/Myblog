package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.music.adapter.repository.IMusicRepository;
import cn.idealer01.domain.music.model.entity.MusicEntity;
import cn.idealer01.infrastructure.dao.IMusicDao;
import cn.idealer01.infrastructure.dao.po.MusicRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MusicRepository implements IMusicRepository {

    @Resource
    private IMusicDao musicDao;

    @Override
    public List<MusicEntity> findAll() {
        return musicDao.queryAll().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<MusicEntity> findEnabledList() {
        return musicDao.queryEnabledList().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MusicEntity findById(Long id) {
        return toEntity(musicDao.queryById(id));
    }

    @Override
    public Long save(MusicEntity music) {
        MusicRecord record = toRecord(music);
        musicDao.insert(record);
        return record.getId();
    }

    @Override
    public void update(MusicEntity music) {
        musicDao.update(toRecord(music));
    }

    @Override
    public void deleteById(Long id) {
        musicDao.deleteById(id);
    }

    private MusicEntity toEntity(MusicRecord record) {
        if (record == null) {
            return null;
        }

        return MusicEntity.builder()
                .id(record.getId())
                .name(record.getName())
                .artist(record.getArtist())
                .audioUrl(record.getAudioUrl())
                .coverImage(record.getCoverImage())
                .sortOrder(record.getSortOrder())
                .enabled(record.getEnabled())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }

    private MusicRecord toRecord(MusicEntity entity) {
        if (entity == null) {
            return null;
        }

        return MusicRecord.builder()
                .id(entity.getId())
                .name(entity.getName())
                .artist(entity.getArtist())
                .audioUrl(entity.getAudioUrl())
                .coverImage(entity.getCoverImage())
                .sortOrder(entity.getSortOrder())
                .enabled(entity.getEnabled())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
