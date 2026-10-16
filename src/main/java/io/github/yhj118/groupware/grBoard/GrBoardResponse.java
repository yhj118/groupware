package io.github.yhj118.groupware.grBoard;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 글조회, 글 목록 조회, 조회수
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrBoardResponse {

    private int grBoardId;              // 게시판 식별번호
    private int employeeId;             // 사원 식별번호
    private String title;               // 제목
    private String content;             // 내용
    private LocalDateTime createdDate;  // 작성일자
    private LocalDateTime modifyDate;   // 수정일자
    private String category;            // 분류(전체공개, 공지, 자유글)
    private String viewCnt;             // 조회수
    private boolean deleteYn;           // 삭제 여부
    private boolean secretYn;           // 비밀글 여부

    private String departmentName;      // 부서명
    private String employeeName;        // 사원명

    private int rowNum;                 // 순번


}
