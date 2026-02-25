package com.example.e_commerce_restapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OrderLoggingAspect {

    //Order start
    @Before("execution(* com.example.e_commerce_restapi.service.OrderService.placeOrder(..))")
    public void logBeforePlaceOrder(JoinPoint joinPoint) {
        String email = (String) joinPoint.getArgs()[0];
        log.info("Order placement started | userEmail={}", email);
    }

    //Order success
    @AfterReturning(
            pointcut = "execution(* com.example.e_commerce_restapi.service.OrderService.placeOrder(..))",
            returning = "result")
    public void logAfterPlaceOrder(Object result) {

        try {
            Long orderId = (Long) result.getClass()
                    .getMethod("getOrderId")
                    .invoke(result);

            Long userId = (Long) result.getClass()
                    .getMethod("getUserId")
                    .invoke(result);

            Object totalAmount = result.getClass()
                    .getMethod("getTotalAmount")
                    .invoke(result);

            log.info("Order placed successfully | orderId={} | userId={} | totalAmount={}",
                    orderId, userId, totalAmount);

        } catch (Exception ignored) {}
    }

    //Order error
    @AfterThrowing(
            pointcut = "execution(* com.example.e_commerce_restapi.service.OrderService.placeOrder(..))",
            throwing = "ex")
    public void logOrderException(JoinPoint joinPoint, Throwable ex) {

        String email = (String) joinPoint.getArgs()[0];

        log.error("Order placement failed | userEmail={} | reason={}",
                email,
                ex.getMessage());
    }
}
