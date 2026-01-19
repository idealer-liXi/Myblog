package cn.idealer01.config.utils;

import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import cn.idealer01.types.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1. 获取head中的Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 2. 检查 Header 格式 "Bearer <token>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Token 无效或过期，这里暂时忽略，后续 Security 会处理
                throw new AppException(ResponseCode.JWT_INVALID);
            }
        }

        // 3. 如果用户名存在，且当前上下文没有认证信息（说明还没登录）
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从数据库加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. 生成认证对象
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 5. 将认证信息存入 SecurityContext (核心！)
            SecurityContextHolder.getContext().setAuthentication(authToken);

        }

        // 6. 放行
        filterChain.doFilter(request, response);

    }
}
