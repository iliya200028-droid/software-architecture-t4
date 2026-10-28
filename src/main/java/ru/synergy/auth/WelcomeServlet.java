package ru.synergy.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/welcome")
public class WelcomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Html.requireLogin(request, response)) return;
        String username = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    username = cookie.getValue();
                    break;
                }
            }
        }
        // Cookie используется для отображения; право доступа определяется только сессией.
        String greeting = username == null
                ? "Добро пожаловать! Cookie с именем отсутствует."
                : "Добро пожаловать, " + Html.escape(username) + "!";
        Html.page(response, "Приветствие", "<p>" + greeting + "</p>"
                + "<form method='get' action='" + Html.escape(request.getContextPath()) + "/userinfo'>"
                + "<button type='submit'>Информация о пользователе</button></form>");
    }
}
