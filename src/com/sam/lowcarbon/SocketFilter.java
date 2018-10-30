package com.sam.lowcarbon;

import javax.servlet.*;
import java.io.IOException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletException;

public class SocketFilter implements Filter {

    public void init(FilterConfig fconfig) throws ServletException {
        try {
            FriendSocket friendRequestSocket = new FriendSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

}
