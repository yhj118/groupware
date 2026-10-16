package io.github.yhj118.groupware.global;

import io.github.yhj118.groupware.employee.EmpResponse;
import io.github.yhj118.groupware.employee.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    private final EmpService empService;

    @ModelAttribute
    public void globalLoginKey(Authentication authentication, Model model) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        String loginKey = authentication.getName();

        EmpResponse loginEmployee =
                empService.empFindByLoginKey(loginKey);

        if (loginEmployee == null) {
            return;
        }

        model.addAttribute("loginEmployee", loginEmployee);
    }
}
