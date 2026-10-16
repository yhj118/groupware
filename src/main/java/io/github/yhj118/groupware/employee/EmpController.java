package io.github.yhj118.groupware.employee;

import io.github.yhj118.groupware.Team.TeamResponse;
import io.github.yhj118.groupware.Team.TeamService;
import io.github.yhj118.groupware.util.MakeRandomNum;
import io.github.yhj118.groupware.util.ImageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

// 사원 등록, 수정, 조정
@Controller
@RequiredArgsConstructor // 2.Lombok이 생성자를 생성(객체 생성한 service를 생성자를 통해 컨트롤러로 주입)
public class EmpController {

    private final EmpService empService; // 1.service 객체 생성
    private final ImageHandler imageHandler;
    private final MakeRandomNum makeRandomNum;
    private final TeamService teamService;


    // 사원 등록 페이지 호출
    @GetMapping("/openSignUp")
    public String openSigunUp(Model model) {

        // 부서-팀 조회
        List<TeamResponse> params = teamService.empFindTeam();
        model.addAttribute("findTeam", params);
        return "employee/signUp";
    }

    // 사원 정보 페이지 호출
    @GetMapping("/openMyPage")
    public String openMyPage(){
        return "employee/myPage";
    }


    // 사원 수정 페이지 호출
    @GetMapping("/openMyPageModify")
    public String openMyPageModify() {
        return "employee/myPageModify";
    }


    // 사원 등록
    @PostMapping("/signUp")
    public String signUp(EmpRequest params) throws IOException { // 서버로부터 받은 html데이터를 EmpRequest와 바인딩.

        // 사진 업로드 로직(사진 저장 + UUID 변환)
        String profileImgPath = imageHandler.imageHandlerManager(params);
        params.setProfileImgPath(profileImgPath);

        // 사번 생성
        String yearRandom = makeRandomNum.empMkNumManager();
        params.setEmployeeNum(yearRandom);

        empService.empSignUp(params);
        return "redirect:/openLogin"; // 메서드가 String 타입이므로 return도 String 타입으로.
    }


    // 사원 수정 (***** 아이디 중복체크, 비밀번호 재확인 추가해야 함)
    @PostMapping("/goEmpModify")
    public String empModify(EmpRequest params, Authentication authentication) throws Exception {

        if (params.getProfileImg() != null && !params.getProfileImg().isEmpty()) {
            // 사진 업로드 로직(사진 저장 + UUID 변환)
            String profileImgPath = imageHandler.imageHandlerManager(params);
            params.setProfileImgPath(profileImgPath);
        }

        // 로그인한 ID or 사번
        String loginKey = authentication.getName();

        empService.empModify(params, loginKey);
        return "redirect:/openMyPage";
    }


    // ID 중복 확인
    @GetMapping("/loginIdCheck")
    @ResponseBody // return값을 view 이름으로 해석하는 게 아닌 HTTP 응답 데이터를 보내기 위해.
    public int loginIdCheck(String loginId) {
        int resultCheckData = empService.loginIdCheck(loginId);
        return resultCheckData;
    }

}
