package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.entity.Category;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("mobile");
    }

    @Test
    void create_WithValidName_ShouldSaveCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category result = categoryService.create("mobile");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("mobile");

        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(savedCategory.getName()).isEqualTo("mobile");
    }

    @Test
    void create_WithDifferentName_ShouldSaveWithThatName() {
        Category techCategory = new Category();
        techCategory.setId(2L);
        techCategory.setName("tech");

        when(categoryRepository.save(any(Category.class))).thenReturn(techCategory);

        Category result = categoryService.create("tech");

        assertThat(result.getName()).isEqualTo("tech");

        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getName()).isEqualTo("tech");
    }

    @Test
    void getAll_ShouldReturnAllCategories() {
        Category tech = new Category();
        tech.setId(2L);
        tech.setName("tech");

        Category gold = new Category();
        gold.setId(3L);
        gold.setName("gold");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, tech, gold));

        List<Category> result = categoryService.getAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Category::getName)
                .containsExactlyInAnyOrder("mobile", "tech", "gold");

        verify(categoryRepository).findAll();
    }

    @Test
    void getAll_WhenNoCategories_ShouldReturnEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<Category> result = categoryService.getAll();

        assertThat(result).isEmpty();
        verify(categoryRepository).findAll();
    }

    @Test
    void create_ShouldCallRepositorySave() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        categoryService.create("mobile");

        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
