package io.github.yhj118.groupware.schedule;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    // 일정 등록
    void insertSchedule(@Param("params") ScheduleRequest params,
                        @Param("employeeId") int employeeId);

    // 일정 전체 조회
    List<ScheduleResponse> selectAllSchedule(@Param("employeeId") int employeeId);

    // 일정 수정
    void updateSchedule(@Param("params") ScheduleRequest params,
                        @Param("employeeId") int employeeId);

    // 일정 삭제
    void deleteSchedule(@Param("scheduleId") int scheduleId, @Param("employeeId") int employeeId, @Param("scheduleType") String scheduleType);

}
