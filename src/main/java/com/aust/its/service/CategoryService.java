package com.aust.its.service;

import com.aust.its.dto.CategoryDto;
import com.aust.its.entity.Category;
import com.aust.its.mapper.CategoryMapper;
import com.aust.its.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream().map(CategoryMapper::entityToDto).toList();
    }

    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
        return CategoryMapper.entityToDto(category);
    }

    public CategoryDto update(long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
        category.setCategoryName(categoryDto.categoryName());
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.entityToDto(savedCategory);
    }

    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        Category category = categoryRepository.save(CategoryMapper.dtoToEntity(categoryDto));
        return CategoryMapper.entityToDto(category);
    }

    public void delete(Long id) {
        categoryRepository.findById(id).ifPresent(categoryRepository::delete);
    }

    public List<Category> getCategoriesByCategoryIdList(List<Long> categoryIdList) {
        return categoryRepository.findAllById(categoryIdList);
    }
    // CategoryService.java
    public Category getOrCreateByName(String name) {
        return categoryRepository.findByCategoryName(name)
                .orElseGet(() -> categoryRepository.save(CategoryMapper.dtoToEntity(new CategoryDto(0, name))));
    }
}
