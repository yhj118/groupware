package io.github.yhj118.groupware.grBoard;

import io.github.yhj118.groupware.util.GrBoardSerchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrBoardService {

    private final GrBoardMapper grBoardMapper;

    // 글쓰기
    public void grBoardInsert(GrBoardRequest params, int employeeId) {
        grBoardMapper.grBoardInsert(params, employeeId);
    }

    // 글수정
    public void grBoardUpdate(GrBoardRequest params) {
        grBoardMapper.grBoardUpdate(params);
    }

    // 글삭제
    public void grBoardDelete(int grBoardId) {
        grBoardMapper.grBoardDelete(grBoardId);
    }

    // 글목록, params: 페이징 처리를 위한 객체 정보
    public List<GrBoardResponse> grBoardList(GrBoardSerchRequest params){
        return grBoardMapper.grBoardList(params);
    }

    // 글조회, params: html로부터 전달받은 글 식별번호
    public GrBoardResponse grBoardView(int grBoardId) {
        return grBoardMapper.grBoardView(grBoardId);
    };

    // 글목록 전체 개수
    // params: category에 따라 전체 개수가 달라지므로.
    public int grBoardCount(GrBoardSerchRequest params) {
        return grBoardMapper.grBoardCount(params);
    }

    // 조회수 증가
    public void grBoardPlusCnt(int grBoardId){
        grBoardMapper.grBoardPlusCnt(grBoardId);
    }

}
