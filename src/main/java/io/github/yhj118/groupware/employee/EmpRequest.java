
package io.github.yhj118.groupware.employee;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

// 사원 등록, 수정, 삭제 클래스
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본생성자 생성
public class EmpRequest {

    private String loginId;             // id
    private int departmentId;           // 부서 식별번호
    private String password;            // 비밀번호
    private String employeeName;        // 이름
    private EmpGender gender;           // 성별
    private LocalDate birthDate;        // 생년월일
    private String postalCode;          // 우편번호
    private String roadAddress;         // 도로명주소
    private String detailAddress;       // 상세주소
    private MultipartFile profileImg;   // 사진 저장용 필드
    private String profileImgPath;      // DB에 저장할 사진 경로 및 불러오기 용도
    private String employeeNum;         // 사번

}
