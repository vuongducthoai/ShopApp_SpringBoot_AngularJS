package com.project.shopapp.configurations;

import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor  // annotation cua Lombok, tu dong tao constructor
//cho các thuoc tinh final, giup giam bot ma lenh lap.
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                /*
                    * Bo loc jwtTokenFilter se chay truoc bo loc mac dinh cua Spring Security (UsernamePasswordAuthenticationFilter)
                    * Muc dich: Kiem tra JWT token truoc khi xu ly xac thuc nguoi dung
                 */
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> {
                    request
                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix)
                            ).permitAll()

                            //Categories
                            .requestMatchers("POST",
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers("PUT",
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers("GET",
                                    String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.USER, Role.ADMIN)
                            .requestMatchers("DELETE",
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)


                            //Products
                            .requestMatchers("POST",
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers("PUT",
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers("GET",
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers("DELETE",
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)

                            //Orders
                            .requestMatchers(POST,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.USER)
                            .requestMatchers(GET,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.ADMIN, Role.USER)
                            .requestMatchers(PUT,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                            //Order_detail
                            .requestMatchers(POST,
                                    String.format("%s/order_details/**", apiPrefix)).hasAnyRole(Role.USER)
                            .requestMatchers(GET,
                                    String.format("%s/order_details/**", apiPrefix)).hasAnyRole(Role.ADMIN, Role.USER)
                            .requestMatchers(PUT,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
                            .anyRequest().authenticated();
                });
        return http.build();
    }
}
