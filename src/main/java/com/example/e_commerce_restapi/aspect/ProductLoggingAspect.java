package com.example.e_commerce_restapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ProductLoggingAspect {

    //Get all
    @Before("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.getAll(..))")
    public void logGetAll() {
        log.info("Fetching all products");
    }

    //Create
    @Before("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.create(..))")
    public void logBeforeCreate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object request = args[0];

        try {
            String name = (String) request.getClass()
                    .getMethod("getName")
                    .invoke(request);

            log.info("Creating new product | name={}", name);
        } catch (Exception ignored) {}
    }

    @AfterReturning(
            pointcut = "execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.create(..))",
            returning = "result")
    public void logAfterCreate(JoinPoint joinPoint, Object result) {

        Object[] args = joinPoint.getArgs();
        Long categoryId = (Long) args[1];

        try {
            Long productId = (Long) result.getClass()
                    .getMethod("getId")
                    .invoke(result);

            log.info("Product created successfully | productId={} | categoryId={}",
                    productId, categoryId);
        } catch (Exception ignored) {}
    }

    //Update
    @Before("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.update(..))")
    public void logBeforeUpdate(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        log.info("Updating product | productId={}", id);
    }

    @AfterReturning("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.update(..))")
    public void logAfterUpdate(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        log.info("Product updated successfully | productId={}", id);
    }

    //Delete
    @Before("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.delete(..))")
    public void logBeforeDelete(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        log.warn("Deleting product | productId={}", id);
    }

    @AfterReturning("execution(* com.example.e_commerce_restapi.service.ProductServiceImpl.delete(..))")
    public void logAfterDelete(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        log.info("Product deleted successfully | productId={}", id);
    }
}
