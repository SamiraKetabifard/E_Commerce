package com.example.e_commerce_restapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class JwtLoggingAspect {

    @Pointcut("execution(* com.example.e_commerce_restapi.config.JwtService.generateToken(..))")
    public void generateTokenPointcut() {
    }

    @Before("generateTokenPointcut()")
    public void logBeforeGenerateToken(JoinPoint joinPoint) {
        UserDetails userDetails = (UserDetails) joinPoint.getArgs()[0];
        log.info("Generating JWT token for user={}", userDetails.getUsername());
    }

    @AfterReturning("generateTokenPointcut()")
    public void logAfterGenerateToken(JoinPoint joinPoint) {
        UserDetails userDetails = (UserDetails) joinPoint.getArgs()[0];
        log.debug("JWT token generated successfully for user={}", userDetails.getUsername());
    }
}
