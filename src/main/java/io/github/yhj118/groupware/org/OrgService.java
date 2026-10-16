package io.github.yhj118.groupware.org;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgMapper orgMapper;

    // 팀-부서-사원 전체 조회
    List<OrgResponse> findOrgList() {
        return orgMapper.findOrgList();
    }

    // 사원 프로필 조회
    OrgResponse findByEmployeeId(int employeeId) {
        return orgMapper.findByEmployeeId(employeeId);
    }

    // 부서 총 몇개, 팀 총 몇개, 전체 총 인원
    OrgResponse findOrgSummary() {
        return orgMapper.findOrgSummary();
    }


}
