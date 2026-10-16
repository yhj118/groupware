// 사원 등록, 수정, 탈퇴, 조회
package io.github.yhj118.groupware.employee;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmpMapper {

    // 사원 등록
    // parameter : 서비스로부터 하나의 객체(EmpRequest의 변수 정보들이 담겨 있음)를 전달받음
    int empSignUp(EmpRequest params);


    // 사원 수정
    int empModify(@Param("params") EmpRequest params, @Param("loginKey") String loginKey); // @Param() : 2개 이상의 파라미터를 보낼 때 @Param으로 이름을 지정하고 Map 형태로 보낸다.
                                                                                           //            MyBatis가 내부적으로 2개를 Map 형태로 묶는다.

    // 사원 삭제


    // 사원 정보 조회
    // parameter: 아이디 혹은 사번으로 로그인한 loginKey
    EmpResponse empFindByLoginKey(String loginKey);


    // 사원 전체 조회


    // ID 중복 체크
    // parameter: 서비스로부터 String 타입의 loginId 하나를 전달받음
    int loginIdCheck(String loginId);


    // 사번 조회
    // parameter: 서비스로부터 String 타입의 employeeNum 하나 전달받음
    // Mybatis는 SQL 실행 후 호출한 곳으로 자동 return
    int employeeNumCheck(String employeeNum);


}
