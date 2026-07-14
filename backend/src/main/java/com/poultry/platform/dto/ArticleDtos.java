package com.poultry.platform.dto;

import com.poultry.platform.domain.ArticleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class ArticleDtos {

    public record ArticleResponse(
            Long id,
            ArticleType type,
            String title,
            String summary,
            String body,
            boolean published,
            Instant publishedAt,
            Instant createdAt
    ) {}

    public record ArticleRequest(
            @NotNull ArticleType type,
            @NotBlank String title,
            String summary,
            @NotBlank String body,
            boolean published
    ) {}
}
