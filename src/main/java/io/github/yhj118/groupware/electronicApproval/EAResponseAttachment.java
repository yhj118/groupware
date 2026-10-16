package io.github.yhj118.groupware.electronicApproval;

import lombok.Getter;

import java.time.LocalDateTime;

// 첨부파일 조회 관련 클래스
@Getter
public class EAResponseAttachment {

    // 첨부파일
    private int attachmentId;                   // 첨부파일 식별번호
    private int approvalId;                     // 전자결재 식별번호
    private String originalFileName;            // 파일 원본 이름
    private String storedFileName;              // 서버 저장 이름
    private String filePath;                    // 저장 경로
    private String fileType;                    // 파일 타입
    private long fileSize;                      // 파일 크기
    private LocalDateTime saveAt;               // 저장일시

}
