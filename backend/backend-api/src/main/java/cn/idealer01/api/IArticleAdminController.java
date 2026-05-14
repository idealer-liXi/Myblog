package cn.idealer01.api;

import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;
import cn.idealer01.api.dto.ArticleRequestDTO;
import cn.idealer01.api.response.Response;

public interface IArticleAdminController {
    Response<ArticleListResponseDTO> getArticles(String keyword, String status, int page, int pageSize);
    Response<ArticleDetailResponseDTO> getArticleById(Long articleId);
    Response<ArticleDetailResponseDTO> createArticle(ArticleRequestDTO request);
    Response<ArticleDetailResponseDTO> updateArticle(Long articleId, ArticleRequestDTO request);
    Response<Void> deleteArticle(Long articleId);
}
