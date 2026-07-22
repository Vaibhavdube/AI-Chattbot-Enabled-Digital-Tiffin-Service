//package com.app.security.filters;
//
//import java.io.IOException;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import com.app.security.jwt_utils.JwtUtils;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//public class JWTRequestFilter extends OncePerRequestFilter {
//	@Autowired
//	private JwtUtils utils;
//	@Autowired
//	private UserDetailsService userDetailsService;
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		log.info("in once per request filter");
//		// get authorization header n check if not null n starting with Bearer
//		String header = request.getHeader("Authorization");
//		if (header != null && header.startsWith("Bearer ")) {
//			// Bearer token present --> extract n validate it
//			String token = header.substring(7);
//			if (utils.validateJwtToken(token)) {
//				// valid token --> extract user name from the token
//				String userName = utils.getUserNameFromJwtToken(token);
//				
//				if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//					// load user details from UserDetailsService
//					UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
//					// create Authentication object , wrapping user details lifted from DB
//					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//							userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
//					//set details in auth object
//		//			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
////					Save this authentication token in the sec ctx.
//					SecurityContextHolder.getContext().setAuthentication(authentication);
//				}
//				else
//					log.info("user name null or authentication already set , username {}",userName);
//
//			}
//		} else
//			log.error("Request header DOES NOT contain a Bearer Token");
//		//pass the request to the next filter in the chain
//		filterChain.doFilter(request, response);
//
//	}
//
//}
//
package com.app.security.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.security.jwt_utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils utils;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("in once per request filter");

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.error("Request header DOES NOT contain a Bearer Token");
            filterChain.doFilter(request, response);
            return; // Exit early to prevent further processing
        }

        // Extract token
        String token = header.substring(7);

        try {
            if (utils.validateJwtToken(token)) {
                String userName = utils.getUserNameFromJwtToken(token);

                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("User {} authenticated successfully", userName);
                } else {
                    log.info("Username null or authentication already set: {}", userName);
                }
            } else {
                log.error("Invalid JWT token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return; // Prevent further processing
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token processing failed");
            return; // Prevent further processing
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}

