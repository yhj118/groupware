package io.github.yhj118.groupware.grBoard;

import io.github.yhj118.groupware.util.GrBoardSerchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GrBoardMapper {

    // 글쓰기
    int grBoardInsert(@Param("params") GrBoardRequest params, @Param("employeeId") int employeeId);

    // 글수정
    int grBoardUpdate(GrBoardRequest params);

    // 글삭제
    int grBoardDelete(int grBoardId);

    // 글목록
    // params: 페이징 처리를 위한 객체 정보
    List<GrBoardResponse> grBoardList(GrBoardSerchRequest params);

    // 글조회
    // params: html로부터 전달받은 글 식별번호
    GrBoardResponse grBoardView(int grBoardId);

    // 조회수 증가
    int grBoardPlusCnt(int grBoardId);

    // 글목록 전체 개수
    // params: category에 따라 전체 개수가 달라지므로.
    int grBoardCount(GrBoardSerchRequest params);



}
