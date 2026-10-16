package io.github.yhj118.groupware.electronicApproval;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

// 전자결재 등록, 수정, 삭제 관련 클래스
@Setter
@Getter
@NoArgsConstructor
public class EARequest {
    // 사원 정보
    private int employeeId;                     // 사원 ID
    private String employeeName;                // 사원 이름

    // 부서 정보
    private int departmentId;                   // 부서 ID
    private String departmentType;              // 부서명
    private String departmentName;              // 팀 이름

    // 전자결재
    private int approvalId;                     // 전자결재 식별번호
    private int writerEmployeeId;               // 사원 식별번호(작성자)
    private String eaType;                      // 전자결재 타입: PROPOSAL(기안서), APPROVAL_REQUEST(품의서), VACATION(휴가), BUSINESS_TRIP(출장), FACT_REPORT(경위서/시말서), WORK_REPORT(업무 보고서)
    private int approverEmployeeId;             // 결재권자(부장만(lv5))
    private String approvalStatus;              // 결재상태: 결재상태(PENDING(승인대기), APPROVE(승인), REJECT(반려)
    private LocalDateTime approvalWriteAt;      // 작성일시
    private LocalDateTime approvalUpdateAt;     // 수정일시
    private LocalDateTime approvalProcessedAt;  // 처리일시
    private String approvalComment;             // 코멘트(피드백)

    // 통합 기안서
    private int proposalId;                     // 통합 기안서 식별번호
    private String proposalTitle;               // 통합 기한서 제목
    private String proposalPurpose;             // 통합 기한서 목적
    private String proposalContent;             // 통합 기한서 내용

    // 통합 품의서
    private int approvalRequestId;                    // 통합 품의서 식별번호
    private String approvalRequestTitle;              // 통합 품의서 제목
    private String approvalRequestContent;            // 통합 품의서 내용
    private LocalDate approvalRequestExecutionDate;   // 통합 품의서 집행 예정일
    private List<EARequestItem> items;                // 구매 물품 목록
    private int approvalRequestTotalMoney;            // 구매 물품 총비용

    // 출장 계획서
    private int businessTripId;                         // 출장 계획서 식별번호
    private LocalDate businessTripExecutionDate;        // 출장 신청일
    private String businessTripTitle;                   // 출장 제목
    private String businessTripLocation;                // 출장 목적지
    private String businessTripFriend;                  // 출장 동행자
    private LocalDate businessTripStart;                // 출장 시작 날짜
    private LocalDate businessTripEnd;                  // 출장 끝 날짜
    private String businessTripPurpose;                 // 출장 목적
    private String businessTripContent;                 // 출장 일정
    private int businessTripAmount;                     // 출장 경비
    private String businessTripTransportation;          // 출장 교통수단

    // 휴가 계획서
    private int vacationId;                     // 휴가 계획서 식별번호
    private String vacationType;                // 휴가 종류
    private String vacationContent;             // 업무 인수인게
    private LocalDate vacationStart;            // 휴가 시작 날짜
    private LocalDate vacationEnd;              // 휴가 끝 날짜
    private LocalDate vacationSubscription;     // 휴가 신청

    // 경위서/시말서
    private int factReportId;                              // 경위서/시말서 식별번호
    private LocalDate factReportWriteDate;                 // 작성일
    private String factReportTitle;                        // 제목
    private LocalDateTime factReportOccurrence;            // 발생일시
    private String factReportLocation;                     // 발생 장소
    private String factReportInspector;                    // 발생 경위
    private String factReportCause;                        // 원인 및 책임
    private String factReportCountermeasures;              // 방지 대책

    // 업무 보고서
    private int workReportId;                   // 업무 보고서 식별번호
    private LocalDate workReportDate;           // 보고일
    private String workReportTitle;             // 제목
    private LocalDate workReportStart;          // 업무 시작일
    private LocalDate workReportEnd;            // 업무 종료일
    private String workReportOutline;           // 개요
    private String workReportContent;           // 내용
    private String workReportResult;            // 결과 및 성과
    private String workReportEtc;               // 향후 계획 및 특이사항

}
