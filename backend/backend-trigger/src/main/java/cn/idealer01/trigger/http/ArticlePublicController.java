package cn.idealer01.trigger.http;

import cn.idealer01.api.IArticlePublicController;
import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;
import cn.idealer01.domain.article.service.IArticleService;
import cn.idealer01.types.enums.ArticleStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/public/articles")
public class ArticlePublicController implements IArticlePublicController {

    @Resource
    private IArticleService articleService;

    @Override
    @GetMapping("")
    public ArticleListResponseDTO getArticlesByTheme(@RequestParam(required = false) String theme) {
        if (StringUtils.isBlank(theme)) {
            return articleService.getArticles(null, ArticleStatus.PUBLISHED.getCode(), 1, Integer.MAX_VALUE);
        }
        return articleService.getArticlesByTheme(theme);
    }

    @Override
    @GetMapping("/{articleId}")
    public ArticleDetailResponseDTO getArticleDetail(@PathVariable Long articleId) {
        return articleService.getArticleDetail(articleId);
    }
}
