package io.github.yhj118.groupware.main;

import io.github.yhj118.groupware.schedule.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final MainMapper mainMapper;

    // 공지 개수
    public int countByCommon() {
        return mainMapper.countByCommon();
    }

    // 오늘 일정
    public int countBySchedule(int employeeId) {
        return mainMapper.countBySchedule(employeeId);
    }

    // 내 업무 개수
    public int countByMyWork(int employeeId) {
        return mainMapper.countByMyWork(employeeId);
    }

    // 공지사항 리스트
    public List<MainResponse> allNotice() {
        List<MainResponse> allNotice = new ArrayList<>();

        allNotice.addAll(mainMapper.boardNotice());
        allNotice.addAll(mainMapper.scheduleNotice());

        allNotice.sort(Comparator.comparing(MainResponse::getCreatedAt).reversed());

        return allNotice.stream().limit(4).toList();
    }

    // 오늘의 일정
    public List<ScheduleResponse> todaySchedule(int employeeId) {
        return mainMapper.todaySchedule(employeeId);
    }

}


