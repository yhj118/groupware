package io.github.yhj118.groupware.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 사용자가 요청한 IP를 서버가 설정한 대역에 포함되는지 필터
public class InternalNetworkFilter extends OncePerRequestFilter {

    private final List<IpAddressMatcher> allowedNetworks = new ArrayList<>();

    // properties에서 받은 허용 대역을 저장
    public InternalNetworkFilter(String allowedCidrs) {
        for (String cidr : allowedCidrs.split(",")) {
            allowedNetworks.add(new IpAddressMatcher(cidr.trim()));
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 접속한 기기의 IP
        String clientIp = request.getRemoteAddr();

        for (IpAddressMatcher network : allowedNetworks) {
            if (network.matches(clientIp)) {
                // 허용 대역이면 기존 로그인·권한 검사로 진행
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 허용 대역이 아니면 여기서 종료
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("허용된 내부망에서만 접속할 수 있습니다.");
    }
}