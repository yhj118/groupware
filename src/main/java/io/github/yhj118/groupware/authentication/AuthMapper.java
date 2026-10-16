package io.github.yhj118.groupware.authentication;

import io.github.yhj118.groupware.employee.EmpResponse;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AuthMapper {

    // 로그인
    EmpResponse findEmployeeLogin(String loginKey);

}
