package br.com.leticia.todolist.filter;


import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.leticia.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var path = request.getRequestURI();
        if (path.contains("tasks")) {

            var authorization = request.getHeader("Authorization").substring("Basic".length()).trim();

            var userInfo = new String(Base64.getDecoder().decode(authorization)).split(":");

            var user = this.userRepository.findByUsername(userInfo[0].toString());

            if (user == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
            } else {

                var passwordVerify = BCrypt.verifyer().verify(userInfo[1].toCharArray(), user.getPassword());

                if (passwordVerify.verified) {
                    request.setAttribute("userUuid", user.getUuid());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }
    }
}
