package io.github.yhj118.groupware.Team;

import io.github.yhj118.groupware.employee.EmpResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeamMapper {

    // 부서-팀 조회
    List<TeamResponse> empFindTeam();

}
