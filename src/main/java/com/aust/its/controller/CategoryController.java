package com.aust.its.controller;

import com.aust.its.dto.CategoryDto;
import com.aust.its.dto.UpdateIssueCategoriesPayload;
import com.aust.its.dto.model.IssueDto;
import com.aust.its.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final Logger log = Logger.getLogger(CategoryController.class.getName());
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories() {

        List<CategoryDto> categories = categoryService.getAll();
        // Log the categories to check if they're being fetched correctly
        categories.forEach(category -> log.info("Category: " + category));
        return categories;
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public CategoryDto createCategory(@RequestBody CategoryDto categoryDto) {
        return categoryService.create(categoryDto);
    }

    @PutMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable long id, @RequestBody CategoryDto categoryDto) {
        return categoryService.update(id, categoryDto);
    }

    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable long id) {
        try {
            categoryService.delete(id);
        } catch (Exception e) {
            log.info("Delete category failed: " + e.getMessage());
        }
        return "Deleted";
    }


}
