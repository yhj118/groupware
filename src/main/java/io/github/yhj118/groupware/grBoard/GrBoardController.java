package io.github.yhj118.groupware.grBoard;

import io.github.yhj118.groupware.employee.EmpResponse;
import io.github.yhj118.groupware.employee.EmpService;
import io.github.yhj118.groupware.util.GrBoardPagination;
import io.github.yhj118.groupware.util.GrBoardSerchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GrBoardController {

    private final GrBoardService grBoardService;
    private final EmpService empService;


    // 게시판 메인페이지 호출, params: 페이징 처리를 위한 객체 정보
    @GetMapping("/openGrBoardMain")
    public String openGrBoardMain(GrBoardSerchRequest params, Model model) { // params에 page값이 자동 바인딩된다.
        List<GrBoardResponse> grBoardList = grBoardService.grBoardList(params);

        // 페이징 버튼을 위한 전체 글 개수 조회
        int totalCount = grBoardService.grBoardCount(params);
        // 페이징 버튼을 위한 페이징 수 계산
        GrBoardPagination pagination = new GrBoardPagination(params, totalCount);

        model.addAttribute("grBoardList", grBoardList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("params", params);

        // 삭제 후 알람 처리
        model.getAttribute("deleteMessage");
        return "grBoard/grBoardMain";
    }


    // 게시판 글쓰기 페이지 호출
    @GetMapping("/openGrBoardWrite")
    public String openGrBoardWrite() {
        return "grBoard/grBoardWrite";
    }


    // 게시판 글보기 페이지 호출
    @GetMapping("/openGrBoardView") // @RequestParam: URL 쿼리스트링으로 오는 값이라는 걸 명확하게 명시하기 위해(생략 가능)
    public String openGrBoardView(@RequestParam int grBoardId,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) String searchType,
                                  @RequestParam(required = false) String keyword,
                                  Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        // 게시글 조회
        GrBoardResponse grBoardViewData = grBoardService.grBoardView(grBoardId);

        // 비밀글 + 작성자가 아닌 경우
        // 현재 로그인한 사원 식별번호와 작성된 글의 사원 식별번호를 비교
        String loginKey = authentication.getName();
        EmpResponse loginEmployee = empService.empFindByLoginKey(loginKey);

        int loginEmployeeId = loginEmployee.getEmployeeId();
        int boardEmployeeId = grBoardViewData.getEmployeeId();
        boolean secretYn = grBoardViewData.isSecretYn();

        if (secretYn && boardEmployeeId != loginEmployeeId) { // 비밀글이되 작성된 글의 사원 식별번호와 로그인한 사원 식별번호가 일치하지 않으면.
            redirectAttributes.addFlashAttribute("failedMessage", "작성자만 조회 가능합니다.");

            // 알람 띄우고 현재 페이지에 머무르도록 URL 상태를 유지하기 위함.
            redirectAttributes.addAttribute("category", category);
            redirectAttributes.addAttribute("page", page);
            redirectAttributes.addAttribute("searchType", searchType);
            redirectAttributes.addAttribute("keyword", keyword);

            return "redirect:/openGrBoardMain";
        }

        // 조회수 증가
        grBoardService.grBoardPlusCnt(grBoardId);

        model.addAttribute("grBoardViewData", grBoardViewData);
        model.addAttribute("loginEmployeeId",loginEmployeeId);
        return "grBoard/grBoardView";
    }


    // 게시글 수정 페이지 호출
    @GetMapping("/openGrBoardModify")
    public String openGrBoardModify(int grBoardId, Model model) {
        GrBoardResponse grBoardViewData = grBoardService.grBoardView(grBoardId);
        model.addAttribute("grBoardViewData", grBoardViewData);

        return "grBoard/grBoardModify";
    }


    // 게시판 글쓰기
    @PostMapping("/goGrBoardWrite")
    public String goGrBoardWrite(GrBoardRequest params, Authentication authentication) {

        // 로그인한 사번 또는 ID
        String loginKey = authentication.getName();
        EmpResponse empSearch = empService.empFindByLoginKey(loginKey);
        int employeeId = empSearch.getEmployeeId();

        grBoardService.grBoardInsert(params, employeeId);
        return "redirect:/openGrBoardMain";
    }


    // 게시글 수정
    @PostMapping("/goGrBoardModify")
    public String goGrBoardModify(int grBoardId, GrBoardRequest params, RedirectAttributes redirectAttributes) {
        params.setGrBoardId(grBoardId);
        grBoardService.grBoardUpdate(params);

        // 수정 후 알람 처리
        redirectAttributes.addFlashAttribute("updateMessage","글이 수정되었습니다.");

        return "redirect:/openGrBoardView?grBoardId="+grBoardId;
    }


    // 게시글 삭제
    @PostMapping("/goGrBoardDelete")
    public String goGrBoardDelete(int grBoardId, RedirectAttributes redirectAttributes) {
        grBoardService.grBoardDelete(grBoardId);

        // 삭제 후 알람 처리,
        redirectAttributes.addFlashAttribute("deleteMessage","글이 삭제되었습니다.");

        return "redirect:/openGrBoardMain";
    }
}
