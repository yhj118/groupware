package io.github.yhj118.groupware.sidebar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SidebarController {

    // 시스템 설정
    @GetMapping("/openSystemConfig")
    public  String openSystemConfig() {
        return "systemConfig/systemConfigMain";
    }
}
