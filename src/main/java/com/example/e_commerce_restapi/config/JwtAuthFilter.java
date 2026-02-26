package com.example.e_commerce_restapi.config;

import com.example.e_commerce_restapi.security.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader= request.getHeader("Authorization");

        String token = null;
        String username= null;

        log.debug("JWT Filter triggered for request: {}", request.getRequestURI());

        //extract token and username from previously generated token during login
        if (authHeader!=null && authHeader.startsWith("Bearer ")) {
            token= authHeader.substring(7);
            username= jwtService.extractUsername(token);
            log.debug("Username extracted from token: {}", username);
        }
        //Check if authentication already exists
        if (username!=null && SecurityContextHolder.getContext().getAuthentication()== null){
            //the user was called by its name as the validation of token required username not email
            UserDetails userDetails= userDetailService.loadUserByUsername(username);

            if (jwtService.validateToken(token,userDetails)){
                //Create Authentication object
                //This tells Spring:
                //User is authenticated
                //What roles they have
                UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                //Attach request details and add ip address
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request) );
        //Set authentication in SecurityContext
        //After this:
        //@PreAuthorize
        //@Secured
        //hasRole()
        //ALL work.
        SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("JWT authentication successful | username={} | roles={}",
                        username,
                        userDetails.getAuthorities());
        System.out.println(userDetails.getAuthorities());
    }
}
//Moves request forward.
        filterChain.doFilter(request,response);
    }
}
