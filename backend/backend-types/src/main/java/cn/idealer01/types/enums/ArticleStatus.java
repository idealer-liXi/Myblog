package cn.idealer01.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED");

    private final String code;
}
