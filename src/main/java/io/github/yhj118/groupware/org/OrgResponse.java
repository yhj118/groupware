// 조직현황 조회 클래스
package io.github.yhj118.groupware.org;

import io.github.yhj118.groupware.employee.EmpGender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrgResponse {

    private Integer departmentId;       // 부서 식별번호
    private String departmentName;      // 팀 명
    private String departmentType;      // 부서명
    private String departmentStatus;    // 활성 상태
    private String dDescription;        // 부서 설명

    private int employeeId;             // 사원 식별번호
    private String employeeNum;         // 사번
    private String loginId;             // id
    private String employeeName;        // 이름
    private EmpGender gender;           // 성별
    private LocalDate birthDate;        // 생년월일
    private String postalCode;          // 우편번호
    private String roadAddress;         // 도로명주소
    private String detailAddress;       // 상세주소
    private String profileImgPath;      // DB에 저장할 사진 경로값 및 불러오기 용도
    private LocalDateTime createdAt;    // 가입일시
    private String approvalStatus;      // 가입 승인 상태
    private String employmentStatus;    // 재직 상태
    private LocalDateTime resignedAt;   // 퇴사일시
    private String role;                // 권한
    private String password;            // 비밀번호
    private String level;               // 직급

    private int totalDepartmentType;         // 전체 부서 개수
    private int totalDepartmentName;         // 전체 팀 개수
    private int totalEmployeeId;             // 전체 인원


}
