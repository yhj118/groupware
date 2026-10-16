package io.github.yhj118.groupware.schedule;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    // 일정 등록
    public void insertSchedule(ScheduleRequest params, int employeeId) {
        scheduleMapper.insertSchedule(params, employeeId);
    }

    // 일정 전체 조회
    public List<ScheduleResponse> selectAllSchedule(int employeeId) {
        return scheduleMapper.selectAllSchedule(employeeId);
    }

    // 일정 수정
    public void updateSchedule(ScheduleRequest params, int employeeId) {
        scheduleMapper.updateSchedule(params, employeeId);
    }

    // 일정 삭제
    public void deleteSchedule(int scheduleId, int employeeId, String scheduleType) {
        scheduleMapper.deleteSchedule(scheduleId, employeeId, scheduleType);
    };

}
