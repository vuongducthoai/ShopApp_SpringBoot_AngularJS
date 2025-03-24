package com.project.shopapp.filters;

import com.project.shopapp.component.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.*;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
          @NotNull HttpServletRequest request,
          @NotNull HttpServletResponse response,
          @NotNull  FilterChain filterChain)
            throws ServletException, IOException {

        if(isByPassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
          // filterChain.doFilter(request, response); // enable bypass
    }

    private boolean isByPassToken(@NotNull HttpServletRequest request) {
        //Định nghĩa các URL và phương thức HTTP mà bạn muốn bỏ qua kiểm tra JWT
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format(apiPrefix + "/products"), "GET"),
                Pair.of(String.format(apiPrefix +"/categories"), "GET"),
                Pair.of(String.format(apiPrefix +"/users/register"), "POST"),
                Pair.of(String.format(apiPrefix +"/users/login"), "POST")
        );

        //Kiem tra yeu cau co phai la mot trong nhung yeu cau bo qua khong
        for(Pair<String, String> bypassToken : bypassTokens) {
            //Neu yeu cau nam trong danh sah bo qua, khong kiem tra JWT va tiep tuc xu ly yeu cau
            if(request.getServletPath().contains(bypassToken.getFirst()) && request.getMethod().equals(bypassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }


    //Phương thức de lay token tu request (co the la tu header Authorization)
    private String extractTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        /*
            - authHeader != null: Kiem tra xem yeu cau co chua header Authorization khong
            - SecurityContextHolder.getContext().getAuthentication() == null: Kiem tra xem nguoi dung da duoc
              // xác thuc trong ung dung hay chua. Neu khong co toi tuong Authentication trong SecurityContext, co nghia la nguoi dung chua dc xac thuc
         */
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            final String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
            if(phoneNumber != null &&  SecurityContextHolder.getContext().getAuthentication() == null) {
             UserDetails existingUser = userDetailsService.loadUserByUsername(phoneNumber); // load tat ca thong tin cua nguoi dung tu co so du lieu
                if(jwtTokenUtil.validateToken(token, existingUser)) {

                }
            }
        }
    }
}
