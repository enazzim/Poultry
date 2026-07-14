package com.poultry.platform.repository;

import com.poultry.platform.domain.ArticleType;
import com.poultry.platform.domain.PortalArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortalArticleRepository extends JpaRepository<PortalArticle, Long> {
    List<PortalArticle> findByPublishedTrueOrderByPublishedAtDesc();
    List<PortalArticle> findByPublishedTrueAndTypeOrderByPublishedAtDesc(ArticleType type);
    List<PortalArticle> findAllByOrderByCreatedAtDesc();
}
