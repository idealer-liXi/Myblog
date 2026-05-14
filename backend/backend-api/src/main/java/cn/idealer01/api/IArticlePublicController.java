package cn.idealer01.api;

import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;

public interface IArticlePublicController {
    ArticleListResponseDTO getArticlesByTheme(String theme);
    ArticleDetailResponseDTO getArticleDetail(Long articleId);
}
