package io.github.yhj118.groupware.org;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrgMapper {

    // 팀-부서-사원 전체 조회
    List<OrgResponse> findOrgList();

    // 사원 프로필 조회
    OrgResponse findByEmployeeId(int employeeId);

    // 부서 총 몇개, 팀 총 몇개, 전체 총 인원
    OrgResponse findOrgSummary();

}
