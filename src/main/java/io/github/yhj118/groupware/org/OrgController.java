package io.github.yhj118.groupware.org;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    // 조직현황 메인 페이지 호출
    @GetMapping("/openOrgMain")
    public String openOrgMain(Model model, String departmentName) {

        List<OrgResponse> findOrgList = orgService.findOrgList();
        OrgResponse findOrgSummary = orgService.findOrgSummary();

        for (OrgResponse org : findOrgList) {
            if (org.getLevel().equals("lv1")) {
                org.setLevel("사원");
            } else if(org.getLevel().equals("lv2")) {
                org.setLevel("대리");
            } else if(org.getLevel().equals("lv3")) {
                org.setLevel("과장");
            } else if(org.getLevel().equals("lv4")) {
                org.setLevel("차장");
            } else if(org.getLevel().equals("lv5")) {
                org.setLevel("부장");
            }
        }

        model.addAttribute("findOrgList", findOrgList); // 팀-부서-사원 전체 조회
        model.addAttribute("findOrgSummary", findOrgSummary); // 부서 총 몇개, 팀 총 몇개, 전체 총 인원

        return "org/orgMain";
    }

    // 조직현황 사원 프로필 조회
    @GetMapping("/org/employee/{employeeId}")
    @ResponseBody
    public OrgResponse findEmployeeProfile(@PathVariable int employeeId, Model model) {

        OrgResponse findByEmpData = orgService.findByEmployeeId(employeeId);

        switch(findByEmpData.getLevel()) {
            case "lv1" -> findByEmpData.setLevel("사원");
            case "lv2" -> findByEmpData.setLevel("대리");
            case "lv3" -> findByEmpData.setLevel("과장");
            case "lv4" -> findByEmpData.setLevel("차장");
            case "lv5" -> findByEmpData.setLevel("부장");
        }

        switch (findByEmpData.getEmploymentStatus()) {
            case "EMPLOYED" -> findByEmpData.setEmploymentStatus("재직중");
            case "ON_LEAVE" -> findByEmpData.setEmploymentStatus("휴가중");
            case "RESIGNED" -> findByEmpData.setEmploymentStatus("퇴사");
        }

        switch (findByEmpData.getApprovalStatus()) {
            case "PENDING" -> findByEmpData.setApprovalStatus("사원등록 승인 전");
            case "APPROVED" -> findByEmpData.setApprovalStatus("사원등록 승인");
            case "REJECTED" -> findByEmpData.setApprovalStatus("사원등록 거절");
        }

        switch (findByEmpData.getRole()) {
            case "EMPLOYEE" -> findByEmpData.setRole("비관리자");
            case "ADMIN" -> findByEmpData.setRole("관리자");
        }

        return findByEmpData;

    }

}
