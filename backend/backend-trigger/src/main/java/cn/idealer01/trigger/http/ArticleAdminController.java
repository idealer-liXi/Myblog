package cn.idealer01.trigger.http;

import cn.idealer01.api.IArticleAdminController;
import cn.idealer01.api.dto.ArticleDetailResponseDTO;
import cn.idealer01.api.dto.ArticleListResponseDTO;
import cn.idealer01.api.dto.ArticleRequestDTO;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.article.service.IArticleService;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/admin/articles")
public class ArticleAdminController implements IArticleAdminController {

    @Resource
    private IArticleService articleService;

    @Resource
    private ILoginRepository loginRepository;

    @Override
    @GetMapping("")
    public Response<ArticleListResponseDTO> getArticles(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        try {
            ArticleListResponseDTO data = articleService.getArticles(keyword, status, page, pageSize);
            return Response.<ArticleListResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<ArticleListResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            return Response.<ArticleListResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/{articleId}")
    public Response<ArticleDetailResponseDTO> getArticleById(@PathVariable Long articleId) {
        try {
            ArticleDetailResponseDTO data = articleService.getArticleById(articleId);
            if (data == null) {
                return Response.<ArticleDetailResponseDTO>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("文章不存在")
                        .build();
            }

            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取文章详情失败 articleId:{}", articleId, e);
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PostMapping("")
    public Response<ArticleDetailResponseDTO> createArticle(@RequestBody ArticleRequestDTO request) {
        try {
            ArticleDetailResponseDTO data = articleService.createArticle(request, getCurrentUserId());
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("创建文章失败", e);
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PutMapping("/{articleId}")
    public Response<ArticleDetailResponseDTO> updateArticle(@PathVariable Long articleId, @RequestBody ArticleRequestDTO request) {
        try {
            if (articleService.getArticleById(articleId) == null) {
                return Response.<ArticleDetailResponseDTO>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("文章不存在")
                        .build();
            }

            ArticleDetailResponseDTO data = articleService.updateArticle(articleId, request);
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("更新文章失败 articleId:{}", articleId, e);
            return Response.<ArticleDetailResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{articleId}")
    public Response<Void> deleteArticle(@PathVariable Long articleId) {
        try {
            articleService.deleteArticle(articleId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除文章失败 articleId:{}", articleId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CurrentUserResponseDTO currentUser = loginRepository.queryCurrentUser(username);
        return currentUser.getId();
    }
}
