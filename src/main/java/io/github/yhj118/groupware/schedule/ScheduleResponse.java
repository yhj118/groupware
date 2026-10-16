package io.github.yhj118.groupware.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleResponse {

    private int scheduleId;                 // 일정 식별번호
    private int employeeId;                 // 사원 식별번호
    private Integer approvalId;             // 전자결재 식별번호
    private String scheduleType;            // 일정 유형(PERSONAL: 개인, COMMON: 공동, APPROVAL: 결재 연동)
    private String scheduleTitle;           // 일정 제목
    private String scheduleContent;         // 일정 내용
    private LocalDate scheduleStart;        // 일정 시작 날짜
    private LocalDate scheduleEnd;          // 일정 종료 날짜
    private LocalDateTime createdAt;        // 작성 시간
    private LocalDateTime updatedAt;        // 수정 시간
    private int countCommon;                // 공지 개수
}
