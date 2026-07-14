package com.poultry.platform.web;

import com.poultry.platform.domain.ArticleType;
import com.poultry.platform.dto.ArticleDtos;
import com.poultry.platform.security.SecurityUtils;
import com.poultry.platform.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/articles")
    public List<ArticleDtos.ArticleResponse> list(@RequestParam(required = false) ArticleType type) {
        return articleService.published(type);
    }

    @GetMapping("/articles/{id}")
    public ArticleDtos.ArticleResponse detail(@PathVariable Long id) {
        return articleService.getPublished(id);
    }

    @GetMapping("/admin/articles")
    public List<ArticleDtos.ArticleResponse> adminList() {
        return articleService.adminList();
    }

    @PostMapping("/admin/articles")
    public ArticleDtos.ArticleResponse create(@Valid @RequestBody ArticleDtos.ArticleRequest request) {
        return articleService.create(SecurityUtils.currentUser(), request);
    }

    @PutMapping("/admin/articles/{id}")
    public ArticleDtos.ArticleResponse update(@PathVariable Long id,
                                              @Valid @RequestBody ArticleDtos.ArticleRequest request) {
        return articleService.update(id, request);
    }
}
