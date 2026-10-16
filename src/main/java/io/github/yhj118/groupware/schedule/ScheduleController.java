package io.github.yhj118.groupware.schedule;

import io.github.yhj118.groupware.employee.EmpResponse;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 일정 메인 + 일정 조회
    @GetMapping("/openSchedule")
    String openSchedule(@ModelAttribute("loginEmployee") EmpResponse empResponse,
                        Model model) {

        int employeeId = empResponse.getEmployeeId();
        List<ScheduleResponse> scheduleList = scheduleService.selectAllSchedule(employeeId);

        model.addAttribute("scheduleList", scheduleList);

        return "schedule/scheduleMain";
    }

    // 일정 insert
    @PostMapping("/insertSchedule")
    @ResponseBody
    String insertSchedule(@ModelAttribute("loginEmployee") EmpResponse empResponse,
                          @RequestBody ScheduleRequest params) {

        int employeeId = empResponse.getEmployeeId();
        scheduleService.insertSchedule(params, employeeId);

        return "OK";
    }

    // 일정 update
    @PostMapping("/updateSchedule")
    @ResponseBody
    String updateSchedule(@ModelAttribute("loginEmployee") EmpResponse empResponse,
                          @RequestBody ScheduleRequest params) {

        int employeeId = empResponse.getEmployeeId();
        scheduleService.updateSchedule(params, employeeId);

        return "OK";
    }

    // 일정 delete
    @PostMapping("/deleteSchedule")
    @ResponseBody
    String deleteSchedule(@ModelAttribute("loginEmployee") EmpResponse empResponse,
                          @RequestBody ScheduleRequest params) {

        int employeeId = empResponse.getEmployeeId();
        int scheduleId = params.getScheduleId();
        String scheduleType = params.getScheduleType();

        scheduleService.deleteSchedule(scheduleId, employeeId, scheduleType);

        return "OK";
    }


}
