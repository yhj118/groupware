package io.github.yhj118.groupware.Team;

import io.github.yhj118.groupware.employee.EmpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamMapper teamMapper;

    // 부서-팀 조회
    public List<TeamResponse> empFindTeam(){
        return teamMapper.empFindTeam();
    }
}
