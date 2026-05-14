package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {

    Article queryById(Long id);

    List<Article> queryList(@Param("keyword") String keyword,
                            @Param("status") String status, @Param("offset") int offset,
                            @Param("limit") int limit);

    long queryCount(@Param("keyword") String keyword,
                    @Param("status") String status);

    List<Article> queryByTheme(String theme);

    void insert(Article article);

    void update(Article article);

    void deleteById(Long id);

    void incrementViewCount(Long id);
}
