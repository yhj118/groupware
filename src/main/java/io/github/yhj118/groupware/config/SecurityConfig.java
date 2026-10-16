package io.github.yhj118.groupware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfFilter;
// 스프링 시큐리티 기본 설정 후 빈으로 등록
@Configuration
public class SecurityConfig {

    // application.properties의 허용 IP 대역을 읽어 allowedCidrs에 저장
    @Value("${groupware.network.allowed-cidrs}")
    private String allowedCidrs;

    // spring security Filter(*http 요청 검사)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 허용 IP 대역을 전달해 필터를 만들고, CSRF 검사 전에 접속 IP를 검사하도록 등록
        http.addFilterBefore(
                new InternalNetworkFilter(allowedCidrs),
                CsrfFilter.class
        );

        http
                // 로그인 유무에 따른 URL 제한 설정
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers( // .requestMatchers(): ()의 URL을 공개해서 로그인하지 않아도 접속 가능
                            "/openLogin", // 로그인 페이지
                            "/openSignUp", // 사원등록 페이지
                            "/signUp", // 사원 등록 버튼
                            "/loginIdCheck", // 중복 확인
                            "/goLogin", // 로그인 실행
                            "/tabler/**" // tabler의 모든 경로
                    ).permitAll().anyRequest().authenticated() // 그 외 모든 요청은 인증된 사용자(로그인)만 접속 가능
                )

                // 스프링 시큐리티의 로그인 처리 규칙
                //
                .formLogin(form -> form // form 방식으로 로그인 처리.
                        .loginPage("/openLogin") // 로그인이 필요할 경우 /openLogin으로 보냄.
                        .loginProcessingUrl("/goLogin") // 로그인 요청을 스프링 시큐리티가 직접 처리(그래서 컨트롤러 없음)
                        .usernameParameter("loginKey") // 입력한 아이디(html name과 일치)
                        .passwordParameter("password") // 입력한 패스워드
                        .failureUrl("/openLogin?error=true") // 로그인 실패 시 해당 URL로 이동
                        .defaultSuccessUrl("/openMain", true) // true 시 메인 페이지 이동
                        .permitAll() // 비로그인 사용자도 접근 가능하도록.
                )

                // 로그아웃 설정
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/openLogin")
                    .permitAll()
                )

                // 로그인 유지
                .rememberMe(remember -> remember
                        .tokenValiditySeconds(60*60*24*365)
                );

        return http.build();
    }

    // 사용자가 입력한 비밀번호와 DB에 저장된 비밀번호 비교
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}