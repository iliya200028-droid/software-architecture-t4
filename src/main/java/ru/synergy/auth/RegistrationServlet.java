package ru.synergy.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

/** Дополнительный сервлет для пункта К3 критериев оценивания. */
@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        form(request, response, "");
    }

    private void form(HttpServletRequest request, HttpServletResponse response, String error) throws IOException {
        String base = Html.escape(request.getContextPath());
        Html.page(response, "Регистрация", "<p class='error'>" + Html.escape(error) + "</p>"
                + "<form method='post' action='" + base + "/register'>"
                + "<label>Имя (3–32 символа: латиница, цифры, _)<input name='username' pattern='[A-Za-z0-9_]{3,32}' required maxlength='32' autocomplete='username'></label>"
                + "<label>Пароль (5–128 символов)<input type='password' name='password' minlength='5' maxlength='128' required autocomplete='new-password'></label>"
                + "<button type='submit'>Зарегистрироваться</button></form><p><a href='" + base + "/login'>Войти</a></p>");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        username = username == null ? "" : username.trim();
        if (!username.matches("[A-Za-z0-9_]{3,32}") || password == null || password.length() < 5 || password.length() > 128) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            form(request, response, "Проверьте формат имени и длину пароля.");
            return;
        }
        Map<String, String> users = (Map<String, String>) getServletContext().getAttribute("users");
        if (users.putIfAbsent(username, password) != null) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            form(request, response, "Это имя уже занято.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
