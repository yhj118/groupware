package io.github.yhj118.groupware.main;

import io.github.yhj118.groupware.schedule.ScheduleResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MainMapper {

    // 공지 개수
    int countByCommon();

    // 오늘 일정 개수
    int countBySchedule(@Param("employeeId") int employeeId);

    // 내 업무 개수
    int countByMyWork(@Param("employeeId") int employeeId);

    // 일정 공지사항
    List<MainResponse> scheduleNotice();

    // 게시판 공지사항
    List<MainResponse> boardNotice();

    // 오늘의 일정
    List<ScheduleResponse> todaySchedule(@Param("employeeId") int employeeId);

}
