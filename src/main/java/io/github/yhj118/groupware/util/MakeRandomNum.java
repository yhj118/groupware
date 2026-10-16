package io.github.yhj118.groupware.util;

import io.github.yhj118.groupware.employee.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

// 사번 생성 클래스 (올해년도 + 난수 4자리 + 최종값은 DB와 중복되지 않게)
@Component
@RequiredArgsConstructor
public class MakeRandomNum {

    private final EmpService empService;

    public String empMkNumManager() {

        // 년도 생성
        int getYear = LocalDate.now().getYear();
        String year = Integer.toString(getYear);

        while (true) { // wihle: 반복문, 조건문으로 true를 선언함으로써(일단 맞다는 가정하에) return을 만날 때까지 무한 반복(첨부터 다시 시작)
                       // return: 메서드 종료, break: while문만 종료

            // 4자리 난수 생성
            int random = (int) (Math.random() * 10000); // 0~9999
            String randomResult = String.format("%04d", random); // 0001, 0002 ... 9999

            // 8자리 난수 생성
            String yearRandom = year + randomResult;

            // 중복 검사(DB 조회)
            int resultEnd = empService.employeeNumCheck(yearRandom);

            // 중복되지 않으면
            if (resultEnd == 0) {
                return yearRandom;
            }

        }

    }

}
