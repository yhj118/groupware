package io.github.yhj118.groupware.electronicApproval;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EAMapper {

    // 6종 양식 작성 페이지 오픈 << 작성자 정보를 조회 >>
    EAResponse openFormProposal(int employeeId);

    // 작성자의 결재자ID 조회
    int findLevel5(int employeeId);

    // 전자결재 insert
    int insertApproval(EARequest params);
    // 전자결재 update
    int updateApproval(@Param("params") EARequest params, @Param("approvalId") int approvalId);

    // 첨부파일 insert
    int insertAttachments(@Param("attachments") List<EARequestAttachment> params);
    // 삭제할 첨부파일 정보 조회: 수정 중인 문서의 파일만 조회
    List<EAResponseAttachment> findAttachmentsToDelete(
            @Param("deleteAttachmentIds") List<Integer> deleteAttachmentIds,
            @Param("approvalId") int approvalId);

    // 선택한 첨부파일 DB행 삭제
    int deleteAttachments(
            @Param("deleteAttachmentIds") List<Integer> deleteAttachmentIds,
            @Param("approvalId") int approvalId);

    // 수정할 기안서를 잠그고 실제 작성자와 결재 상태 확인
    EAResponse findProposalForUpdate(@Param("approvalId") int approvalId);

    // 6종 공통 문서 잠금 및 실제 작성자·결재 상태 조회
    EAResponse findApprovalForUpdate(@Param("approvalId") int approvalId);

    // DB에서 확인한 양식 종류로 해당 양식의 실제 식별번호 조회
    Integer findApprovalFormId(@Param("approvalId") int approvalId, @Param("eaType") String eaType);

    // 품의 물품 목록 교체/문서 삭제에 사용
    int deleteApprovalRequestItems(@Param("approvalRequestId") int approvalRequestId);

    // 문서 삭제: 첨부파일 → 물품 → 양식 → 공통 문서 순으로 호출
    int deleteAllAttachments(@Param("approvalId") int approvalId);
    int deleteApprovalForm(@Param("approvalId") int approvalId, @Param("eaType") String eaType);
    int deleteApproval(@Param("approvalId") int approvalId, @Param("employeeId") int employeeId);

    // 기안서 작성
    int writeProposal(EARequest params);
    // 기안서 수정
    int updateProposal(@Param("params") EARequest params, @Param("approvalId") int approvalId);
    // 품의서 작성
    int writeRequest(EARequest params);
    // 품의서 수정
    int updateApprovalRequest(@Param("params") EARequest params, @Param("approvalId") int approvalId);
    // 휴가 계획서 작성
    int writeVacation(EARequest params);
    // 휴가 신청서 수정
    int updateVacation(@Param("params") EARequest params, @Param("approvalId") int approvalId);
    // 출장 계획서 작성
    int writeBusinessTrip(EARequest params);
    // 출장 신청서 수정
    int updateBusinessTrip(@Param("params") EARequest params, @Param("approvalId") int approvalId);
    // 경위/시말서 작성
    int writeFactReport(EARequest params);
    // 경위/사실 보고서 수정
    int updateFactReport(@Param("params") EARequest params, @Param("approvalId") int approvalId);
    // 업무 보고서 작성
    int writeWorkReport(EARequest params);
    // 업무 보고서 수서
    int updateWorkReport(@Param("params") EARequest params, @Param("approvalId") int approvalId);


    // 품의서 구매 물품 저장
    int insertApprovalRequestItems(
            @Param("approvalRequestId") int approvalRequestId,
            @Param("items") List<EARequestItem> items
    );

    // 결재 상황 카운트
    EAResponse countApprovalStatus(int employeeId);

    // 메인 전체 리스트
    List<EAResponse> countMainList(int employeeId);

    // 전자결재 상세보기
    EAResponse viewApprovalDetail(@Param("employeeId") int employeeId, @Param("approvalId") int approvalId, @Param("keyType") String keyType);

    // 첨부파일 목록 조회
    List<EAResponseAttachment> viewAttachmentList(@Param("approvalId") int approvalId);

    // 클릭한 파일 1개 조회 + 다운로드 권한 확인
    EAResponseAttachment findDownloadAttachment(@Param("attachmentId") int attachmentId, @Param("employeeId") int employeeId);

    // 품의물품 조회
    List<EARequestItem> viewRequestItem(int approvalRequestId);

    // 결재자의 승인, 반려 처리
    int updateApprovalStatus(
            @Param("approvalId") int approvalId,
            @Param("employeeId") int employeeId,
            @Param("approvalStatus") String approvalStatus,
            @Param("approvalComment") String approvalComment
    );

    // 일정에 넣을 사원 식별번호와 승인받은 글만 조회
    EAResponse selectScheduleData(@Param("approvalId") int approvalId);

}
