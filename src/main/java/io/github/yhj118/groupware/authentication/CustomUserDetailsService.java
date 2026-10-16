package io.github.yhj118.groupware.authentication;

import io.github.yhj118.groupware.employee.EmpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 로그인할 때 이 클래스에서 service를 통해 DB 조회 (컨트롤러 대체)
// 시큐리티가 얘를 호출함으로써, 사용자가 입력한 사번 or ID가 뭔지 모르기 때문에 이 클래스를 통해 로그인한 정보를 이해할 수 있다.
// 즉 DB와 스프링시큐리티를 연결하는 중간 번역기.
/*  DB 조회의 결과는 아래와 같이 리턴받는데,
EmpResponse {
    employeeNum = "20260001"
    loginId = "g5"
    password = "$2a$10$..."
    employeeName = "유훈재"
    role = "EMPLOYEE"
    departmentName = "개발팀"
 누가 아이디고 패스워드인지 모르기 때문에 return을 통해서 설정한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthService authService;

    // 로그인할 때 스프링 시큐리티가 메서드 호출
    @Override
    public UserDetails loadUserByUsername(String loginKey) {

        EmpResponse employee = authService.findEmployeeLogin(loginKey);

        // 사용자가 없으면
        if(employee == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        return User.builder()
                .username(loginKey) // XXX사용자가 입력한 사번 또는 ID를 loginKey로 통일하여 설정(어차피 서비스 로직에서 loginKey로 받아 DB에서 OR로 사번과 ID를 전부 조회한다.)
                .password(employee.getPassword())
                .roles("USER")
                .build();

    }

}
