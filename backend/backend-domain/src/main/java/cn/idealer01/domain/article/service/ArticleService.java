package cn.idealer01.domain.article.service;

import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;
import cn.idealer01.api.dto.ArticleRequestDTO;
import cn.idealer01.domain.article.adapter.repository.IArticleRepository;
import cn.idealer01.domain.article.model.entity.ArticleEntity;
import cn.idealer01.types.enums.ArticleStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class ArticleService implements IArticleService {

    @Resource
    private IArticleRepository articleRepository;

    private ArticleDetailResponseDTO toDetailResponseDTO(ArticleEntity article) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return ArticleDetailResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .theme(article.getTheme())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .authorId(article.getAuthorId())
                .viewCount(article.getViewCount() == null ? 0 : article.getViewCount())
                .createdAt(formatDate(article.getCreateTime(), formatter))
                .updatedAt(formatDate(article.getUpdateTime(), formatter))
                .build();
    }

    private ArticleListResponseDTO.ArticleItemDTO toArticleItemDTO(ArticleEntity article) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return ArticleListResponseDTO.ArticleItemDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .theme(article.getTheme())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .viewCount(article.getViewCount() == null ? 0 : article.getViewCount())
                .createdAt(formatDate(article.getCreateTime(), formatter))
                .build();
    }

    private String formatDate(Date date, SimpleDateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    @Override
    public ArticleListResponseDTO getArticles(String keyword, String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleEntity> articles = articleRepository.findAll(keyword, status, offset, pageSize);
        long total = articleRepository.count(keyword, status);

        return ArticleListResponseDTO.builder()
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .articles(articles.stream().map(this::toArticleItemDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public ArticleDetailResponseDTO getArticleById(Long articleId) {
        ArticleEntity article = articleRepository.findById(articleId);
        if (article == null) {
            return null;
        }
        return toDetailResponseDTO(article);
    }

    @Override
    public ArticleListResponseDTO getArticlesByTheme(String theme) {
        List<ArticleEntity> articles = articleRepository.findByTheme(theme);
        List<ArticleListResponseDTO.ArticleItemDTO> items = articles.stream()
                .map(this::toArticleItemDTO)
                .collect(Collectors.toList());

        return ArticleListResponseDTO.builder()
                .total(items.size())
                .page(1)
                .pageSize(items.size())
                .articles(items)
                .build();
    }

    @Override
    public ArticleDetailResponseDTO getArticleDetail(Long articleId) {
        articleRepository.incrementViewCount(articleId);
        return getArticleById(articleId);
    }

    @Override
    public ArticleDetailResponseDTO createArticle(ArticleRequestDTO request, Long authorId) {
        Date now = new Date();
        ArticleEntity article = ArticleEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary() == null ? "" : request.getSummary())
                .theme(request.getTheme() == null ? "" : request.getTheme())
                .coverImage(request.getCoverImage() == null ? "" : request.getCoverImage())
                .status(request.getStatus() == null ? ArticleStatus.DRAFT.getCode() : request.getStatus())
                .authorId(authorId)
                .viewCount(0)
                .createTime(now)
                .updateTime(now)
                .build();

        Long articleId = articleRepository.save(article);
        article.setId(articleId);
        return toDetailResponseDTO(article);
    }

    @Override
    public ArticleDetailResponseDTO updateArticle(Long articleId, ArticleRequestDTO request) {
        ArticleEntity article = articleRepository.findById(articleId);
        if (article == null) {
            return null;
        }

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getTheme() != null) {
            article.setTheme(request.getTheme());
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }

        article.setUpdateTime(new Date());
        articleRepository.update(article);
        return toDetailResponseDTO(article);
    }

    @Override
    public void deleteArticle(Long articleId) {
        articleRepository.deleteById(articleId);
    }
}
