package io.github.yhj118.groupware.authentication;

import io.github.yhj118.groupware.employee.EmpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;

    // 로그인
    EmpResponse findEmployeeLogin(String loginKey) {
       return authMapper.findEmployeeLogin(loginKey);
    }



}
