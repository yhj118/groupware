package io.github.yhj118.groupware.main;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class MainResponse {

    private int noticeId;             // 게시글 번호 또는 일정 번호
    private String noticeType;        // BOARD: 게시판, SCHEDULE: 일정
    private String noticeTitle;       // 공지 제목
    private String noticeContent;     // 공지 내용
    private LocalDateTime createdAt;  // 작성일시

}
