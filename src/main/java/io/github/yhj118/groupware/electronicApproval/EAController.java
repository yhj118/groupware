package io.github.yhj118.groupware.electronicApproval;

import io.github.yhj118.groupware.employee.EmpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class EAController {

    private final EAService eAService;
    private final EAServiceExcel eaServiceExcel;

    // 전자결재 메인페이지
    @GetMapping("/openEAMain")
    String openEAMain(@ModelAttribute("loginEmployee") EmpResponse empResponse, Model model) {
        int employeeId = empResponse.getEmployeeId();
        EAResponse countStatus = eAService.countApprovalStatus(employeeId);
        Map<String, List<EAResponse>> mainList = eAService.countMainList(employeeId);

        model.addAttribute("countStatus", countStatus);
        model.addAttribute("pendingList", mainList.get("pendingList"));
        model.addAttribute("approveList", mainList.get("approveList"));
        model.addAttribute("rejectList", mainList.get("rejectList"));
        model.addAttribute("dataAllList", mainList.get("dataAllList"));

        return "electronicApproval/eAMain";
    }

    // 양식 선택 페이지
    @GetMapping("/openFormSelect")
    String openFormSelect() {
        return "electronicApproval/eAFormSelect";
    }

    // 기안서 작성 페이지
    @GetMapping("/openFormProposal")
    String openFormProposal(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formProposal";
    }

    // 품의서 작성 페이지
    @GetMapping("/openFormRequest")
    String openFormRequest(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formApprovalRequest";
    }

    // 휴가 신청서 작성 페이지
    @GetMapping("/openFormVacation")
    String openFormVacation(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formVacation";
    }

    // 출장 계획서 작성 페이지
    @GetMapping("/openFormBusiness")
    String openFormBusiness(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formBusinessTrip";
    }

    // 경위/시말서 작성 페이지
    @GetMapping("/openFormFactReport")
    String openFormFactReport(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formFactReport";
    }

    // 업무 보고서 작성 페이지
    @GetMapping("/openFormWorkReport")
    String openFormWorkReport(@RequestParam("employeeId") int employeeId, Model model) {
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);
        return "electronicApproval/formWorkReport";
    }

    // 기안서 저장
    @PostMapping("/writeProposal")
    String writeProposal(@ModelAttribute EARequest params,
                         @ModelAttribute("loginEmployee") EmpResponse empResponse,
                         @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeProposal(params, employeeId, attachments);
        return "redirect:/openEAMain";
    }

    // 품의서 저장
    @PostMapping("/writeApprovalRequest")
    String writeApprovalRequest(@ModelAttribute EARequest params,
                                @ModelAttribute("loginEmployee") EmpResponse empResponse,
                                @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeApprovalRequest(params, employeeId, attachments);
        return "redirect:/openEAMain";
    }

    // 휴가 신청서 저장
    @PostMapping("/wirteVacation")
    String writeVacation(@ModelAttribute EARequest params,
                         @ModelAttribute("loginEmployee") EmpResponse empResponse) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeVacation(params, employeeId);
        return "redirect:/openEAMain";
    }

    // 출장 계획서 저장
    @PostMapping("/writeBusinessTrip")
    String writeBusinessTrip(@ModelAttribute EARequest params,
                             @ModelAttribute("loginEmployee") EmpResponse empResponse) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeBusinessTrip(params, employeeId);
        return "redirect:/openEAMain";
    }

    // 경위/시말서 저장
    @PostMapping("/writeFactReport")
    String writeFactReport(@ModelAttribute EARequest params,
                           @ModelAttribute("loginEmployee") EmpResponse empResponse,
                           @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeFactReport(params, employeeId, attachments);
        return "redirect:/openEAMain";
    }

    // 업무 보고서 저장
    @PostMapping("/writeWorkReport")
    String writeWorkReport(@ModelAttribute EARequest params,
                           @ModelAttribute("loginEmployee") EmpResponse empResponse,
                           @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {
        int employeeId = empResponse.getEmployeeId();
        eAService.writeWorkReport(params, employeeId, attachments);
        return "redirect:/openEAMain";
    }

    // 전자결재 상세보기
    @GetMapping("/viewApprovalDetail")
    String viewApprovalDetail(@RequestParam("approvalId") int approvalId,
                              @RequestParam("keyType") String keyType,
                              Model model,
                              @ModelAttribute("loginEmployee") EmpResponse empResponse,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestType) {

        int employeeId = empResponse.getEmployeeId();
        EAResponse infoByEmployeeId = eAService.openFormProposal(employeeId);

        // 상세 조회 쿼리
        EAResponse viewApprovalData = eAService.viewApprovalDetail(employeeId, approvalId, keyType);

        if (viewApprovalData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.");
        }

        model.addAttribute("viewApprovalData", viewApprovalData);
        model.addAttribute("infoByEmployeeId", infoByEmployeeId);

        // 품의서 구매 물품 목록 조회
        if ("APPROVAL_REQUEST".equals(viewApprovalData.getEaType())) {
            int approvalRequestId = viewApprovalData.getApprovalRequestId();
            List<EARequestItem> viewRequestItemList = eAService.viewRequestItem(approvalRequestId);
            model.addAttribute("viewRequestItemList", viewRequestItemList);
        }

        // 첨부파일 목록 조회
        if ("PROPOSAL".equals(viewApprovalData.getEaType()) || "APPROVAL_REQUEST".equals(viewApprovalData.getEaType())
                || "FACT_REPORT".equals(viewApprovalData.getEaType()) || "WORK_REPORT".equals(viewApprovalData.getEaType())) {
            List<EAResponseAttachment> viewAttachmentList = eAService.viewAttachmentList(approvalId);
            model.addAttribute("viewAttachmentList", viewAttachmentList);
        }

        // 양식에 맞는 상세보기 화면 선택
        String viewName = switch (viewApprovalData.getEaType()) {
            case "PROPOSAL" -> "electronicApproval/detailProposal";
            case "APPROVAL_REQUEST" -> "electronicApproval/detailApprovalRequest";
            case "VACATION" -> "electronicApproval/detailVacation";
            case "BUSINESS_TRIP" -> "electronicApproval/detailBusinessTrip";
            case "FACT_REPORT" -> "electronicApproval/detailFactReport";
            case "WORK_REPORT" -> "electronicApproval/detailWorkReport";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 양식입니다.");
        };

        return "XMLHttpRequest".equals(requestType) ? viewName + " :: detail" : viewName;
    }

    // 첨부파일 다운로드
    @GetMapping("/downloadAttachment")
    public ResponseEntity<Resource> downloadAttachment(
            @RequestParam("attachmentId") int attachmentId,
            @ModelAttribute("loginEmployee") EmpResponse empResponse) throws IOException {

        int employeeId = empResponse.getEmployeeId();

        // 클릭한 파일 한 개 조회 및 다운로드 권한 확인
        EAResponseAttachment attachment = eAService.findDownloadAttachment(attachmentId, employeeId);

        if (attachment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없거나 조회 권한이 없습니다.");
        }

        // DB의 file_path에는 파일명까지 포함되어 있음
        Path filePath = Path.of(attachment.getFilePath()).toAbsolutePath().normalize();

        if (!Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 파일을 찾을 수 없습니다.");
        }

        Resource resource = new FileSystemResource(filePath);
        String downloadName = attachment.getOriginalFileName().replace("\r", "").replace("\n", "");

        String disposition = ContentDisposition.attachment()
                .filename(downloadName, StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(filePath))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    // 6종 양식 수정 페이지 오픈
    @GetMapping("/openUpdateApproval")
    String openSixApproval(@RequestParam int approvalId,
                           @RequestParam String keyType,
                           Model model,
                           @ModelAttribute("loginEmployee") EmpResponse empResponse,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestType) {

        int employeeId = empResponse.getEmployeeId();

        // 수정할 문서 조회
        EAResponse viewApprovalData = eAService.viewApprovalDetail(employeeId, approvalId, keyType);

        if (viewApprovalData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.");
        }

        if (viewApprovalData.getWriterEmployeeId() != employeeId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        if (!"PENDING".equals(viewApprovalData.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "결재 대기 문서만 수정할 수 있습니다.");
        }

        model.addAttribute("viewApprovalData", viewApprovalData);
        model.addAttribute("infoByEmployeeId", eAService.openFormProposal(employeeId));

        String eaType = viewApprovalData.getEaType();

        // 품의서 구매 물품 조회
        if ("APPROVAL_REQUEST".equals(eaType)) {
            model.addAttribute("viewRequestItemList", eAService.viewRequestItem(viewApprovalData.getApprovalRequestId()));
        }

        // 기존 첨부파일 조회
        if ("PROPOSAL".equals(eaType) || "APPROVAL_REQUEST".equals(eaType)
                || "FACT_REPORT".equals(eaType) || "WORK_REPORT".equals(eaType)) {
            model.addAttribute("viewAttachmentList", eAService.viewAttachmentList(approvalId));
        }

        // 양식별 수정 HTML 선택
        String viewName = switch (eaType) {
            case "PROPOSAL" -> "electronicApproval/editProposal";
            case "APPROVAL_REQUEST" -> "electronicApproval/editApprovalRequest";
            case "VACATION" -> "electronicApproval/editVacation";
            case "BUSINESS_TRIP" -> "electronicApproval/editBusinessTrip";
            case "FACT_REPORT" -> "electronicApproval/editFactReport";
            case "WORK_REPORT" -> "electronicApproval/editWorkReport";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 양식입니다.");
        };

        return "XMLHttpRequest".equals(requestType) ? viewName + " :: edit" : viewName;
    }

    // 6종 양식 수정: 실제 양식 종류는 서비스가 DB에서 확인
    @PostMapping("/updateApproval")
    String updateApproval(@ModelAttribute EARequest params,
                          @ModelAttribute("loginEmployee") EmpResponse empResponse,
                          @RequestParam(value = "deleteAttachmentIds", required = false) List<Integer> deleteAttachmentIds,
                          @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {

        eAService.updateApproval(params, empResponse.getEmployeeId(), attachments, deleteAttachmentIds);
        return "redirect:/openEAMain";
    }

    // 본인이 작성한 결재 대기 문서 삭제
    @PostMapping("/deleteApproval")
    String deleteApproval(@RequestParam("approvalId") int approvalId,
                          @ModelAttribute("loginEmployee") EmpResponse empResponse) {
        eAService.deleteApproval(approvalId, empResponse.getEmployeeId());
        return "redirect:/openEAMain";
    }

    // 결재자의 승인, 반려 처리
    @PostMapping("/updateStatus")
    String updateApprovalStatus(@RequestParam("approvalId") int approvalId,
                                @ModelAttribute("loginEmployee") EmpResponse empResponse,
                                @ModelAttribute("EARequest") EARequest EARequest) {

        eAService.updateApprovalStatus(approvalId, empResponse.getEmployeeId(), EARequest.getApprovalStatus(), EARequest.getApprovalComment());

        return "redirect:/openEAMain";
    }

    // 엑셀 다운로드
    @GetMapping("/downloadProposalExcel")
    public ResponseEntity<byte[]> downloadProposalExcel(
            @RequestParam("approvalId") int approvalId,
            @RequestParam("keyType") String keyType,
            @ModelAttribute("loginEmployee") EmpResponse empResponse) throws IOException {

        if (empResponse == null || empResponse.getEmployeeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        int employeeId = empResponse.getEmployeeId();

        // 문서 상세 조회
        EAResponse document = eAService.viewApprovalDetail(employeeId, approvalId, keyType);

        if (document == null || !keyType.equals(document.getEaType())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.");
        }

        // 작성자 또는 해당 문서의 결재자만 다운로드 가능
        if (document.getWriterEmployeeId() != employeeId
                && document.getApproverEmployeeId() != employeeId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다운로드 권한이 없습니다.");
        }

        // 승인된 문서만 다운로드 가능
        if (!"APPROVE".equals(document.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "승인된 문서만 다운로드할 수 있습니다.");
        }

        // 문서 작성자의 부서와 팀 조회
        EAResponse writerInfo = eAService.openFormProposal(document.getWriterEmployeeId());

        // 품의서 구매 물품 조회
        List<EARequestItem> items = List.of();

        if ("APPROVAL_REQUEST".equals(document.getEaType())) {
            items = eAService.viewRequestItem(document.getApprovalRequestId());
        }

        // 첨부파일이 있는 양식의 파일 목록 조회
        List<EAResponseAttachment> attachments = List.of();

        if ("PROPOSAL".equals(document.getEaType())
                || "APPROVAL_REQUEST".equals(document.getEaType())
                || "FACT_REPORT".equals(document.getEaType())
                || "WORK_REPORT".equals(document.getEaType())) {
            attachments = eAService.viewAttachmentList(document.getApprovalId());
        }

        // 문서, 작성자 정보, 구매 물품, 첨부파일을 함께 전달
        byte[] excelData = eaServiceExcel.createProposalExcel(
                document,
                writerInfo,
                items,
                attachments
        );

        String fileName = document.getEATypeName() + "_" + approvalId + ".xlsx";

        String disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentLength(excelData.length)
                .body(excelData);
    }

}