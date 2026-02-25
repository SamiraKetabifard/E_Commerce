package com.example.e_commerce_restapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;
import com.example.e_commerce_restapi.entity.Category;
import java.util.List;

@Slf4j
@Aspect
@Component
public class CategoryLoggingAspect {

    //Create
    @Before("execution(* com.example.e_commerce_restapi.service.CategoryService.create(..))")
    public void logBeforeCreate(JoinPoint joinPoint) {
        String name = (String) joinPoint.getArgs()[0];
        log.info("Creating new category | name={}", name);
    }

    @AfterReturning(
            pointcut = "execution(* com.example.e_commerce_restapi.service.CategoryService.create(..))",
            returning = "result")
    public void logAfterCreate(Object result) {

        Category savedCategory = (Category) result;

        log.info("Category created successfully | categoryId={} | name={}",
                savedCategory.getId(),
                savedCategory.getName());
    }

    //Get all
    @Before("execution(* com.example.e_commerce_restapi.service.CategoryService.getAll(..))")
    public void logBeforeGetAll() {
        log.info("Fetching all categories");
    }

    @AfterReturning(
            pointcut = "execution(* com.example.e_commerce_restapi.service.CategoryService.getAll(..))",
            returning = "result")
    public void logAfterGetAll(Object result) {

        List<?> categories = (List<?>) result;

        log.info("Total categories fetched: {}", categories.size());
    }
}