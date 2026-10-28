package ru.synergy.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/userinfo")
public class UserInfoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Html.requireLogin(request, response)) return;
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("user");
        String loginTime = (String) session.getAttribute("loginTime");
        Html.page(response, "Информация о пользователе", "<p>Имя из сессии: <strong>"
                + Html.escape(username) + "</strong></p><p>Время входа (UTC): " + Html.escape(loginTime)
                + "</p><p>Время бездействия до завершения сессии: " + session.getMaxInactiveInterval() / 60
                + " минут.</p><p><a href='" + Html.escape(request.getContextPath()) + "/welcome'>К приветствию</a></p>");
    }
}
