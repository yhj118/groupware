// 사원 등록, 수정, 탈퇴, 조회
package io.github.yhj118.groupware.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpService {

    private final EmpMapper empMapper;
    private final PasswordEncoder passwordEncoder;

    // 사원 등록
    // parameter: 컨트롤러로부터 하나의 객체(EmpRequest의 변수 정보들이 담겨 있음)를 전달받음
    public void empSignUp(EmpRequest params) {

        // 비밀번호 암호화
        String passwordEn = passwordEncoder.encode(params.getPassword());
        params.setPassword(passwordEn);

        empMapper.empSignUp(params);
    }


    // 사원 수정
    public void empModify(EmpRequest params, String loginKey) {

        // 비밀번호 암호화
        if (params.getPassword() != null && !params.getPassword().isBlank()) {
            String passwordEn = passwordEncoder.encode(params.getPassword());
            params.setPassword(passwordEn);
        }

        empMapper.empModify(params, loginKey);
    }


    // 사원 삭제


    // 사원 조회
    public EmpResponse empFindByLoginKey(String loginKey){
        EmpResponse employeeFindResult = empMapper.empFindByLoginKey(loginKey);
        return employeeFindResult;
    }

    // 사원 전체 조회


    // ID 중복 체크
    // parameter: 컨트롤러로부터 loginId를 전달받음
    public int loginIdCheck(String loginId) {
        return empMapper.loginIdCheck(loginId);
    }


    // 사번 조회
    // parameter: 컨트롤러부터 employeeNum을 전달받음
    public int employeeNumCheck(String employeeNum) {
        int resultEmpNum = empMapper.employeeNumCheck(employeeNum);
        return resultEmpNum;
    }

}
