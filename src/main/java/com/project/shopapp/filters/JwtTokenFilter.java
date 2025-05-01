package com.project.shopapp.filters;

import com.project.shopapp.component.JwtTokenUtil;
import com.project.shopapp.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
          @NotNull FilterChain filterChain)
            throws IOException {
            try {
                if(isByPassToken(request)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                extractTokenFromRequest(request, response);  //Request require token
                filterChain.doFilter(request, response); // Request no require token
            } catch (Exception e){
                e.printStackTrace();
                //SC_UNAUTHORIZED(Status Code Unauthorized): la mot hang so tuong ung voi ma trang thai HTTP 401.
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(e.getMessage());
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                }

            }

    }

    private boolean isByPassToken(@NotNull HttpServletRequest request) {
        System.out.println("Request Path: " + request.getServletPath());
        System.out.println("Request Method: " + request.getMethod());
        //Định nghĩa các URL và phương thức HTTP mà bạn muốn bỏ qua kiểm tra JWT
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/products**", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/send-otp", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/verify-email", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/verify-phone", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/update-password", apiPrefix), "PUT"),

                Pair.of(String.format("%s/payments**", apiPrefix), "GET"),
                Pair.of(String.format("%s/payments**", apiPrefix), "POST"),

                //Danh nhap social
                Pair.of(String.format("%s/users/login-social", apiPrefix), "POST")
        );


        //Kiem tra yeu cau co phai la mot trong nhung yeu cau bo qua khong
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        for(Pair<String, String> token : bypassTokens) {
            System.out.println("Request Method: '" + request.getMethod() + "'");
            System.out.println("Request getServlet: '" + request.getServletPath() + "'");
            System.out.println("Bypass URL: " + token.getFirst() + " - Method: '" + token.getSecond() + "'");
            //Neu yeu cau nam trong danh sah bo qua, khong kiem tra JWT va tiep tuc xu ly yeu cau
            String path = token.getFirst();
            String method = token.getSecond();
            if (requestPath.matches(path.replace("**", ".*"))
                    && requestMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }

        return false;
    }


    //Phương thức de lay token tu request (co the la tu header Authorization)
    private void extractTokenFromRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
 /*
           + Phương thức getHeader(String name) của đối tượng HttpServletRequest
            sẽ trả về giá trị của header có tên là "Authorization" trong yêu cầu HTTP.
           + Nếu header không tồn tại trong yeu cầu, phuong thức se tra ve null.
           + Header Authorization thường có giá trị theo định dạng Bearer <token>,
              trong đó <token> là thông tin xác thực, có thể là token JWT hoặc một dạng mã xác thực khác.

         */
            final String authHeader = request.getHeader("Authorization");
        /*
            - authHeader != null: Kiem tra xem yeu cau co chua header Authorization khong
            - SecurityContextHolder.getContext().getAuthentication() == null: Kiem tra xem nguoi dung da duoc
              // xác thuc trong ung dung hay chua. Neu khong co toi tuong Authentication trong SecurityContext, co nghia la nguoi dung chua dc xac thuc
         */
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }
                return;
            }
                final String token = authHeader.substring(7);
                final String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
                if(phoneNumber != null &&  SecurityContextHolder.getContext().getAuthentication() == null) {
                    User existingUser = (User) userDetailsService.loadUserByUsername(phoneNumber); // load tat ca thong tin cua nguoi dung tu co so du lieu
                    if(jwtTokenUtil.validateToken(token, existingUser)) {
                        //* Doi tuong UsernamePasswordAuthenticationToken (la mot loai Authentication) de luu tru
                        // thong tin xac thuc cua nguoi dung trong SecurityContext.
                    /*
                        + existingUser la doi tuong chua thong tin nguoi dung de duoc tai tu co so du lieu.
                        + null duoc truyen vao cho mat khau vi khong can mat khau trong qua trinh xac thuc JWT (token thay the mat khau)
                        + existingUser.getAuthorities() la danh sach cac quyen(roles/authorities)
                         // cua nguoi dung, dieu nay se duoc su dung de phan quyen trong ung dung.
                    */
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                existingUser, null, existingUser.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
            }
        } catch (Exception ex){
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

    }
}
