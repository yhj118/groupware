package io.github.yhj118.groupware.electronicApproval;

import io.github.yhj118.groupware.schedule.ScheduleRequest;
import io.github.yhj118.groupware.schedule.ScheduleResponse;
import io.github.yhj118.groupware.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EAService {

    private final EAMapper eaMapper;
    private final EAServiceAttachment eaServiceAttachment;
    private final ScheduleService scheduleService;

    private static final Set<String> APPROVAL_TYPES = Set.of(
            "PROPOSAL", "APPROVAL_REQUEST", "VACATION", "BUSINESS_TRIP", "FACT_REPORT", "WORK_REPORT");
    private static final Set<String> ATTACHMENT_TYPES = Set.of(
            "PROPOSAL", "APPROVAL_REQUEST", "FACT_REPORT", "WORK_REPORT");
    private static final Set<String> VACATION_TYPES = Set.of("ANNUAL", "HALF_AM", "HALF_PM", "SICK", "ETC");
    private static final Set<String> TRANSPORTATION_TYPES = Set.of("CAR", "TRAIN", "AIRPLANE", "ETC");

    // 6종 양식 작성 페이지 오픈
    @Transactional
    public EAResponse openFormProposal(int employeeId) {
        return eaMapper.openFormProposal(employeeId);
    }

    // 통합 기안서 작성
    @Transactional
    public void writeProposal(EARequest params, int employeeId, List<MultipartFile> files) {

        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("PROPOSAL");

        // 전자결재, 통합 기안서, 첨부파일 insert
        eaMapper.insertApproval(params);
        eaMapper.writeProposal(params);
        eaServiceAttachment.saveAttachments(params.getApprovalId(), files);

    }

    // 기존 기안서 수정 메서드도 공통 처리 흐름을 사용
    @Transactional
    public void updateProposal(EARequest params,
                               int employeeId,
                               List<MultipartFile> files,
                               List<Integer> deleteAttachmentIds) {
        updateApprovalInternal(params, employeeId, files, deleteAttachmentIds, "PROPOSAL");
    }

    // 6종 양식 수정: DB의 양식 종류로 분기
    @Transactional
    public void updateApproval(EARequest params,
                               int employeeId,
                               List<MultipartFile> files,
                               List<Integer> deleteAttachmentIds) {
        updateApprovalInternal(params, employeeId, files, deleteAttachmentIds, null);
    }

    private void updateApprovalInternal(EARequest params,
                                        int employeeId,
                                        List<MultipartFile> files,
                                        List<Integer> deleteAttachmentIds,
                                        String expectedType) {
        int approvalId = params.getApprovalId();
        EAResponse approval = requireEditableApproval(approvalId, employeeId);
        String eaType = approval.getEaType();
        if (expectedType != null && !expectedType.equals(eaType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "기안서가 아닙니다.");
        }
        int formId = requireFormId(approvalId, eaType);
        boolean usesAttachments = ATTACHMENT_TYPES.contains(eaType);
        if (!usesAttachments && ((deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty())
                || hasNewAttachments(files))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이 양식은 첨부파일을 사용하지 않습니다.");
        }

        params.setEmployeeId(employeeId);
        params.setEaType(eaType);

        switch (eaType) {
            case "PROPOSAL" -> {
                requireTitle(params.getProposalTitle());
                requireText(params.getProposalPurpose(), "기안 목적");
                requireText(params.getProposalContent(), "기안 내용");
                eaMapper.updateProposal(params, approvalId);
            }
            case "APPROVAL_REQUEST" -> {
                requireTitle(params.getApprovalRequestTitle());
                requireText(params.getApprovalRequestContent(), "품의 사유");
                // hidden 총액과 품의서 ID 대신 서버에서 계산/조회한 값 사용
                params.setApprovalRequestId(formId);
                params.setApprovalRequestTotalMoney(calculateItemTotal(params.getItems()));
                eaMapper.updateApprovalRequest(params, approvalId);
                eaMapper.deleteApprovalRequestItems(formId);
                eaMapper.insertApprovalRequestItems(formId, params.getItems());
            }
            case "VACATION" -> {
                if (params.getVacationType() == null || !VACATION_TYPES.contains(params.getVacationType())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "휴가 종류를 확인해주세요.");
                }
                requirePeriod(params.getVacationStart(), params.getVacationEnd());
                params.setVacationContent(emptyIfNull(params.getVacationContent()));
                eaMapper.updateVacation(params, approvalId);
            }
            case "BUSINESS_TRIP" -> {
                requireTitle(params.getBusinessTripTitle());
                requireText(params.getBusinessTripLocation(), "출장 목적지");
                requireText(params.getBusinessTripPurpose(), "출장 목적");
                requireText(params.getBusinessTripContent(), "출장 일정");
                requirePeriod(params.getBusinessTripStart(), params.getBusinessTripEnd());
                if (params.getBusinessTripTransportation() == null
                        || !TRANSPORTATION_TYPES.contains(params.getBusinessTripTransportation())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "교통수단을 확인해주세요.");
                }
                requireMoney(params.getBusinessTripAmount());
                params.setBusinessTripFriend(emptyIfNull(params.getBusinessTripFriend()));
                eaMapper.updateBusinessTrip(params, approvalId);
            }
            case "FACT_REPORT" -> {
                requireTitle(params.getFactReportTitle());
                if (params.getFactReportOccurrence() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "발생 일시를 입력해주세요.");
                }
                requireText(params.getFactReportLocation(), "발생 장소");
                requireText(params.getFactReportInspector(), "발생 경위");
                requireText(params.getFactReportCause(), "원인 및 책임");
                requireText(params.getFactReportCountermeasures(), "재발 방지 대책");
                eaMapper.updateFactReport(params, approvalId);
            }
            case "WORK_REPORT" -> {
                requireTitle(params.getWorkReportTitle());
                requirePeriod(params.getWorkReportStart(), params.getWorkReportEnd());
                if (params.getWorkReportDate() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보고일을 입력해주세요.");
                }
                requireText(params.getWorkReportOutline(), "업무 개요");
                requireText(params.getWorkReportContent(), "진행 내용");
                requireText(params.getWorkReportResult(), "결과 및 성과");
                params.setWorkReportEtc(emptyIfNull(params.getWorkReportEtc()));
                eaMapper.updateWorkReport(params, approvalId);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 양식입니다.");
        }

        // 내용이 동일해서 UPDATE 결과가 0이어도 첨부파일만 변경할 수 있음
        if (usesAttachments) {
            eaServiceAttachment.updateAttachments(approvalId, deleteAttachmentIds, files);
        }
        eaMapper.updateApproval(params, approvalId);
    }

    // 문서 삭제: 외래키의 자식 데이터부터 삭제하고 마지막에 공통 문서 삭제
    @Transactional
    public void deleteApproval(int approvalId, int employeeId) {
        EAResponse approval = requireEditableApproval(approvalId, employeeId);
        String eaType = approval.getEaType();
        int formId = requireFormId(approvalId, eaType);

        eaServiceAttachment.deleteAllAttachments(approvalId);
        if ("APPROVAL_REQUEST".equals(eaType)) {
            eaMapper.deleteApprovalRequestItems(formId);
        }
        if (eaMapper.deleteApprovalForm(approvalId, eaType) != 1
                || eaMapper.deleteApproval(approvalId, employeeId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "문서가 변경되었습니다. 목록을 새로고침해주세요.");
        }
    }

    private EAResponse requireEditableApproval(int approvalId, int employeeId) {
        if (approvalId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문서 번호가 올바르지 않습니다.");
        }
        EAResponse approval = eaMapper.findApprovalForUpdate(approvalId);
        if (approval == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.");
        }
        if (approval.getWriterEmployeeId() != employeeId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정하거나 삭제할 수 있습니다.");
        }
        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "결재 대기 문서만 수정하거나 삭제할 수 있습니다.");
        }
        if (approval.getEaType() == null || !APPROVAL_TYPES.contains(approval.getEaType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 양식입니다.");
        }
        return approval;
    }

    private int requireFormId(int approvalId, String eaType) {
        Integer formId = eaMapper.findApprovalFormId(approvalId, eaType);
        if (formId == null || formId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "양식 데이터를 찾을 수 없습니다.");
        }
        return formId;
    }

    private int calculateItemTotal(List<EARequestItem> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "구매 물품을 한 개 이상 입력해주세요.");
        }
        long total = 0;
        for (EARequestItem item : items) {
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "구매 물품 목록을 확인해주세요.");
            }
            requireText(item.getItemName(), "구매 물품명");
            requireMoney(item.getItemAmount());
            total += item.getItemAmount();
            if (total > 100_000_000) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "총 품의 금액은 1억 원 이하로 입력해주세요.");
            }
        }
        return (int) total;
    }

    private void requireMoney(int amount) {
        if (amount < 0 || amount > 100_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "금액은 0원부터 1억 원까지 입력해주세요.");
        }
    }

    private void requireTitle(String title) {
        requireText(title, "제목");
        if (title.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목은 200자 이하로 입력해주세요.");
        }
    }

    private void requireText(String text, String label) {
        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "을(를) 입력해주세요.");
        }
    }

    private void requirePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일과 종료일을 확인해주세요.");
        }
    }

    private boolean hasNewAttachments(List<MultipartFile> files) {
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) return true;
            }
        }
        return false;
    }

    private String emptyIfNull(String text) {
        return text == null ? "" : text;
    }

    // 통합 품의서 작성
    @Transactional
    public void writeApprovalRequest(EARequest params, int employeeId, List<MultipartFile> files) {

        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("APPROVAL_REQUEST");

        // 구매 물품 목록 확인
        List<EARequestItem> items = params.getItems();

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("구매 물품을 한 개 이상 입력해야 합니다.");
        }

        // 서버에서 총금액 계산
        long totalMoney = 0;

        for (EARequestItem item : items) {
            if (item.getItemName() == null || item.getItemName().isBlank()) {
                throw new IllegalArgumentException("구매 물품명을 입력해야 합니다.");
            }

            if (item.getItemAmount() < 0) {
                throw new IllegalArgumentException("물품 금액은 0원 이상이어야 합니다.");
            }

            totalMoney += item.getItemAmount();
        }

        if (totalMoney > 100_000_000) {
            throw new IllegalArgumentException("총 품의 금액은 1억 원을 넘을 수 없습니다.");
        }

        params.setApprovalRequestTotalMoney((int) totalMoney);

        // 전자결재, 품의서 내용 저장
        eaMapper.insertApproval(params);
        eaMapper.writeRequest(params);
        // 구매 물품 여러 개 저장
        eaMapper.insertApprovalRequestItems(params.getApprovalRequestId(), items);
        // 첨부파일 여러 개 저장
        eaServiceAttachment.saveAttachments(params.getApprovalId(), files);
    }

    // 휴가 계획서 작성
    @Transactional
    public void writeVacation(EARequest params, int employeeId) {
        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("VACATION");

        eaMapper.insertApproval(params);
        eaMapper.writeVacation(params);

    }

    // 출장 계획서 작성
    @Transactional
    public void writeBusinessTrip(EARequest params, int employeeId) {
        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("BUSINESS_TRIP");

        eaMapper.insertApproval(params);
        eaMapper.writeBusinessTrip(params);

    }

    // 경위/시말서 작성
    @Transactional
    public void writeFactReport(EARequest params, int employeeId, List<MultipartFile> files) {
        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("FACT_REPORT");

        // 전자결재, 통합 기안서, 첨부파일 insert
        eaMapper.insertApproval(params);
        eaMapper.writeFactReport(params);
        eaServiceAttachment.saveAttachments(params.getApprovalId(), files);
    }

    // 업무 보고서 작성
    @Transactional
    public void writeWorkReport(EARequest params, int employeeId, List<MultipartFile> files) {
        // 작성자와 같은 팀의 결재자 ID 조회 및 공통 전자결재 저장에 필요한 값 설정
        int findLevel5 = eaMapper.findLevel5(employeeId);
        params.setEmployeeId(employeeId);
        params.setApproverEmployeeId(findLevel5);
        params.setEaType("WORK_REPORT");

        // 전자결재, 통합 기안서, 첨부파일 insert
        eaMapper.insertApproval(params);
        eaMapper.writeWorkReport(params);
        eaServiceAttachment.saveAttachments(params.getApprovalId(), files);
    }

    // 결재 상황 카운트
    @Transactional
    public EAResponse countApprovalStatus(int employeeId) {
        return eaMapper.countApprovalStatus(employeeId);
    }

    // 메인 전체 리스트
    @Transactional
    public Map<String, List<EAResponse>> countMainList(int employeeId) {
        List<EAResponse> dataList = eaMapper.countMainList(employeeId);

        List<EAResponse> pendingList = new ArrayList<>();
        List<EAResponse> approveList = new ArrayList<>();
        List<EAResponse> rejectList = new ArrayList<>();

        for (EAResponse data : dataList) {
            if("PENDING".equals(data.getApprovalStatus())) {
                if (pendingList.size() < 10) {
                    pendingList.add(data);
                }
            }
            if("APPROVE".equals(data.getApprovalStatus())) {
                if (approveList.size() < 10) {
                    approveList.add(data);
                }
            }
            if("REJECT".equals(data.getApprovalStatus())) {
                if (rejectList.size() < 10) {
                    rejectList.add(data);
                }
            }
        }
        Map<String, List<EAResponse>> result = new HashMap<>();

        result.put("pendingList", pendingList);
        result.put("approveList", approveList);
        result.put("rejectList", rejectList);
        result.put("dataAllList", dataList); // 팝업용 전체 데이터

        return result;
    }

    // 전자결재 상세보기
    @Transactional
    public EAResponse viewApprovalDetail(int employeeId, int approvalId, String keyType) {
        return eaMapper.viewApprovalDetail(employeeId, approvalId, keyType);
    }

    // 클릭한 파일 1개 조회 + 다운로드 권한 확인
    @Transactional
    public EAResponseAttachment findDownloadAttachment(int attachmentId, int employeeId) {
        return eaMapper.findDownloadAttachment(attachmentId, employeeId);
    }

    // 첨부파일 목록 조회
    @Transactional(readOnly = true)
    public List<EAResponseAttachment> viewAttachmentList(int approvalId) {
        return eaMapper.viewAttachmentList(approvalId);
    }

    // 품의물품 조회
    @Transactional
    public List<EARequestItem> viewRequestItem(int approvalRequestId) {
        return eaMapper.viewRequestItem(approvalRequestId);
    }

    // 결재자의 승인, 반려 처리
    @Transactional
    public int updateApprovalStatus(int approvalId, int employeeId, String approvalStatus, String approvalComment) {

        int count = eaMapper.updateApprovalStatus(approvalId, employeeId, approvalStatus, approvalComment);

        // 처리가 안 됐으면 메서드 종료
        if(count == 0) {
            return 0;
        }

        // 일정에 추가
        if("APPROVE".equals(approvalStatus)) {

            // 승인처리한 글의 사원 employeeId, 결재된 문서 데이터 조회
            EAResponse selectData = eaMapper.selectScheduleData(approvalId);

            // 휴가·출장이 아니면 일정 저장 없이 종료
            if (selectData == null) {
                return count;
            }

            ScheduleRequest params = new ScheduleRequest();
                params.setApprovalId(selectData.getApprovalId());
                params.setScheduleType("APPROVAL");
                int writerEmployeeId = selectData.getWriterEmployeeId();

            if ("VACATION".equals(selectData.getEaType())) {
                params.setScheduleTitle("휴가");
                params.setScheduleStart(selectData.getVacationStart());
                params.setScheduleEnd(selectData.getVacationEnd());
            } else {
                params.setScheduleTitle("출장");
                params.setScheduleStart(selectData.getBusinessTripStart());
                params.setScheduleEnd(selectData.getBusinessTripEnd());
            }

            scheduleService.insertSchedule(params, writerEmployeeId); // (ScheduleRequest params, int employeeId)
        }

        return count;
    }

}
