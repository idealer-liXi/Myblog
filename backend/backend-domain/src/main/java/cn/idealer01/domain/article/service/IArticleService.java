package cn.idealer01.domain.article.service;

import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;
import cn.idealer01.api.dto.ArticleRequestDTO;

public interface IArticleService {

    ArticleListResponseDTO getArticles(String keyword, String status, int page, int pageSize);

    ArticleDetailResponseDTO getArticleById(Long articleId);

    ArticleListResponseDTO getArticlesByTheme(String theme);

    ArticleDetailResponseDTO getArticleDetail(Long articleId);

    ArticleDetailResponseDTO createArticle(ArticleRequestDTO request, Long authorId);

    ArticleDetailResponseDTO updateArticle(Long articleId, ArticleRequestDTO request);

    void deleteArticle(Long articleId);
}
