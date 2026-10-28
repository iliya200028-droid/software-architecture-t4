package ru.synergy.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(value = "/login", loadOnStartup = 1)
public class LoginServlet extends HttpServlet {
    private Map<String, String> users;

    @Override
    public void init() {
        // Учебное хранилище: создаётся один раз при загрузке сервлета.
        users = new ConcurrentHashMap<>();
        users.put("ilya", "12345");
        users.put("student", "java123");
        getServletContext().setAttribute("users", users);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        showForm(request, response, "");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, String error) throws IOException {
        String base = Html.escape(request.getContextPath());
        Html.page(response, "Вход в приложение", "<p class='error'>" + Html.escape(error) + "</p>"
                + "<form method='post' action='" + base + "/login'>"
                + "<label>Имя пользователя<input name='username' autocomplete='username' required maxlength='32'></label>"
                + "<label>Пароль<input type='password' name='password' autocomplete='current-password' required maxlength='128'></label>"
                + "<button type='submit'>Войти</button></form><p><a href='" + base + "/register'>Регистрация</a></p>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        username = username == null ? "" : username.trim();
        if (username.isEmpty() || password == null || !password.equals(users.get(username))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            showForm(request, response, "Неверное имя пользователя или пароль.");
            return;
        }
        // Заменяем прежнюю сессию, чтобы не использовать старый идентификатор после входа.
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("user", username);
        session.setAttribute("loginTime", Instant.now().toString());
        session.setMaxInactiveInterval(30 * 60);

        Cookie userCookie = new Cookie("username", username);
        userCookie.setMaxAge(60 * 60);
        userCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        userCookie.setHttpOnly(true);
        userCookie.setSecure(request.isSecure());
        userCookie.setAttribute("SameSite", "Lax");
        response.addCookie(userCookie);
        response.sendRedirect(request.getContextPath() + "/welcome");
    }
}
