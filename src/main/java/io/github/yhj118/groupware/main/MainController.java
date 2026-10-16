package io.github.yhj118.groupware.main;

import io.github.yhj118.groupware.electronicApproval.EAResponse;
import io.github.yhj118.groupware.electronicApproval.EAService;
import io.github.yhj118.groupware.employee.EmpResponse;
import io.github.yhj118.groupware.schedule.ScheduleResponse;
import io.github.yhj118.groupware.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final EAService eAService;
    private final ScheduleService scheduleService;
    private final MainService mainService;

    @GetMapping({"/", "/openMain"})
    public String openMain(Model model,
                           @ModelAttribute("loginEmployee") EmpResponse empResponse) {
        int employeeId = empResponse.getEmployeeId();

        // 결재 대기 카운트
        EAResponse countStatus = eAService.countApprovalStatus(employeeId);
        // 새로운 공지 카운트
        int countCommon = mainService.countByCommon();
        // 오늘 일정
        int countSchedule = mainService.countBySchedule(employeeId);
        // 내 업무
        int countByMyWork = mainService.countByMyWork(employeeId);
        // 공지사항
        List<MainResponse> allNoticeList = mainService.allNotice();
        // 오늘의 일정
        List<ScheduleResponse> todaySchedule = mainService.todaySchedule(employeeId);
        // 최근 결재 현황
        List<EAResponse> recentApprovalList = eAService.countMainList(employeeId).get("dataAllList").stream()
                        .limit(5).toList();

        model.addAttribute("countStatus", countStatus);
        model.addAttribute("countCommon", countCommon);
        model.addAttribute("countSchedule", countSchedule);
        model.addAttribute("countByMyWork", countByMyWork);
        model.addAttribute("allNoticeList", allNoticeList);
        model.addAttribute("todaySchedule", todaySchedule);
        model.addAttribute("recentApprovalList", recentApprovalList);

        return "index";
    }
}