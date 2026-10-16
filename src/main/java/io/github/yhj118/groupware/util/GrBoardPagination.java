package io.github.yhj118.groupware.util;

import lombok.Getter;

// 페이징 수 계산 클래스(총 42개의 글이 있을 때 5개의 페이징이 나와야 하므로 계산하는 클래스)
@Getter
public class GrBoardPagination {

    private int totalPage;      // 전체 페이지 몇 개?
    private int totalCount;     // 글 전체 몇 개?

    private int page;           // 현재 페이지는?
    private int recordSize;     // 현재 페이지에 표시할 글 개수는?

    private int pageSize = 10;   // 한 화면에 보여줄 페이지 버튼 개수
    private int startPage;      // 시작 페이지 번호
    private int endPage;        // 마지막 페이지 번호

    private boolean existPrevPage;  // 이전 페이지 묶음 존재 여부
    private boolean existNextPage;  // 다음 페이지 묶음 존재 여부
    private int prevPage;           // 이전 버튼을 눌렀을 때 이동할 페이지
    private int nextPage;           // 다음 버튼을 눌렀을 때 이동할 페이지

    // 페이징 수 계산
    public GrBoardPagination(GrBoardSerchRequest params, int totalCount) {
        this.page = params.getPage(); // 요청받은 현재 페이지 (1, 2, 3...)
        this.recordSize = params.getRecordSize(); // 한 페이지에 표시할 일반글 개수 (SQL의 LIMMIT)
        this.totalCount = totalCount; // DB에서 조회한 전체 일반글 개수

        this.totalPage = (int) Math.ceil((double) totalCount / recordSize); // 총 글 개수 대비해서 몇 페이지 나오는지?

        // 페이징 수에 따른 페이징 버튼 계산
        this.startPage = ((page-1) / pageSize) * pageSize + 1;
        this.endPage = Math.min(startPage + pageSize - 1, totalPage); // 현재 버튼 묶음의 마지막 페이지 번호인 endPage를 구하는 계산

        this.existPrevPage = startPage > 1; // 1보다 큰 페이지의 경우에만 이전 묶음 버튼 생성
        this.existNextPage = endPage < totalPage; // 전체 페이지보다 끝 페이지가 작을 경우에만 다음 묶음 버튼 생성
        this.prevPage = startPage - 1; //
        this.nextPage = endPage + 1;
    }



}
