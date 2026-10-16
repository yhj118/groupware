package io.github.yhj118.groupware.authentication;


import io.github.yhj118.groupware.employee.EmpController;
import io.github.yhj118.groupware.employee.EmpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

// 로그인 및 로그아웃 관련
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인 페이지 호출
    @GetMapping({"/", "/openLogin"}) // 접속 url (http://localhost:8081/login)
    public String openLogin() {

        return "authentication/login"; // 출력 경로 (authentication 폴더의 login.html을 찾아 출력)
    }

}
