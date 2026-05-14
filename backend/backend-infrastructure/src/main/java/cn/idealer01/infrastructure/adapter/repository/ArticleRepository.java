package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.article.adapter.repository.IArticleRepository;
import cn.idealer01.domain.article.model.entity.ArticleEntity;
import cn.idealer01.infrastructure.dao.IArticleDao;
import cn.idealer01.infrastructure.dao.po.Article;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleRepository implements IArticleRepository {

    @Resource
    private IArticleDao articleDao;

    @Override
    public ArticleEntity findById(Long id) {
        return toEntity(articleDao.queryById(id));
    }

    @Override
    public List<ArticleEntity> findAll(String keyword, String status, int offset, int limit) {
        return articleDao.queryList(keyword, status, offset, limit).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, String status) {
        return articleDao.queryCount(keyword, status);
    }

    @Override
    public List<ArticleEntity> findByTheme(String theme) {
        return articleDao.queryByTheme(theme).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Long save(ArticleEntity article) {
        Article po = toPo(article);
        articleDao.insert(po);
        return po.getId();
    }

    @Override
    public void update(ArticleEntity article) {
        articleDao.update(toPo(article));
    }

    @Override
    public void deleteById(Long id) {
        articleDao.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        articleDao.incrementViewCount(id);
    }

    private ArticleEntity toEntity(Article article) {
        if (null == article) {
            return null;
        }

        return ArticleEntity.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .theme(article.getTheme())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .authorId(article.getAuthorId())
                .viewCount(article.getViewCount())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private Article toPo(ArticleEntity article) {
        if (null == article) {
            return null;
        }

        return Article.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .theme(article.getTheme())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .authorId(article.getAuthorId())
                .viewCount(article.getViewCount())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }
}
