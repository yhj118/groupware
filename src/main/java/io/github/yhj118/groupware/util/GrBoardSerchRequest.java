package io.github.yhj118.groupware.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 게시판 목록 조회 및 페이징 조건
@Setter
@Getter
@NoArgsConstructor
public class GrBoardSerchRequest {

    // 검색
    private String searchType; // 검색 조건
    private String keyword;    // 검색어

    private String category = "ALL";    // 선택한 카테고리
    private int page = 1;               // 현재 페이지(기본 1페이지)
    private int recordSize = 10;        // 한 페이지에 표시할 일반글 개수 (SQL의 LIMMIT)

    // 앞에서 건너뛸 일반글 개수 (SQL의 OFFSET)
    // *tip: 필드 offset이 없어도 getter 작성 가능
    public int getOffset() {
        return (page -1) * recordSize;
    }

}
