package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.dto.ArticleDtos;
import com.poultry.platform.repository.AppUserRepository;
import com.poultry.platform.repository.PortalArticleRepository;
import com.poultry.platform.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class ArticleService {

    private final PortalArticleRepository articleRepository;
    private final AppUserRepository appUserRepository;

    public ArticleService(PortalArticleRepository articleRepository, AppUserRepository appUserRepository) {
        this.articleRepository = articleRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleDtos.ArticleResponse> published(ArticleType type) {
        List<PortalArticle> list = type == null
                ? articleRepository.findByPublishedTrueOrderByPublishedAtDesc()
                : articleRepository.findByPublishedTrueAndTypeOrderByPublishedAtDesc(type);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ArticleDtos.ArticleResponse getPublished(Long id) {
        PortalArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기사가 없습니다."));
        if (!article.isPublished()) {
            throw new IllegalArgumentException("공개되지 않은 기사입니다.");
        }
        return toResponse(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleDtos.ArticleResponse> adminList() {
        return articleRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ArticleDtos.ArticleResponse create(UserPrincipal principal, ArticleDtos.ArticleRequest request) {
        PortalArticle article = new PortalArticle();
        apply(article, request);
        article.setCreatedBy(appUserRepository.findById(principal.getId()).orElse(null));
        articleRepository.save(article);
        return toResponse(article);
    }

    @Transactional
    public ArticleDtos.ArticleResponse update(Long id, ArticleDtos.ArticleRequest request) {
        PortalArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기사가 없습니다."));
        apply(article, request);
        return toResponse(article);
    }

    private void apply(PortalArticle article, ArticleDtos.ArticleRequest request) {
        article.setType(request.type());
        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setBody(request.body());
        boolean wasPublished = article.isPublished();
        article.setPublished(request.published());
        if (request.published() && (!wasPublished || article.getPublishedAt() == null)) {
            article.setPublishedAt(Instant.now());
        }
        if (!request.published()) {
            article.setPublishedAt(null);
        }
    }

    private ArticleDtos.ArticleResponse toResponse(PortalArticle article) {
        return new ArticleDtos.ArticleResponse(
                article.getId(),
                article.getType(),
                article.getTitle(),
                article.getSummary(),
                article.getBody(),
                article.isPublished(),
                article.getPublishedAt(),
                article.getCreatedAt()
        );
    }
}
