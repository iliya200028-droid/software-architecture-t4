package ru.synergy.auth;

import jakarta.servlet.http.*;
import java.io.IOException;

/** Общая HTML-оболочка и экранирование данных перед выводом. */
final class Html {
    private Html() { }
    static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    static void page(HttpServletResponse response, String title, String body) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("<!DOCTYPE html><html lang='ru'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'><title>"
                + escape(title) + "</title><style>body{font:18px Arial;background:#eef2f7;color:#172338;"
                + "margin:0;padding:40px 20px}main{max-width:560px;margin:30px auto;background:white;"
                + "padding:32px;border-radius:16px}label{display:block;margin:18px 0}input{display:block;"
                + "box-sizing:border-box;width:100%;padding:12px;margin-top:8px;font:inherit}"
                + "button{background:#2455b8;color:white;border:0;border-radius:7px;padding:12px 20px;"
                + "font:inherit;cursor:pointer}a{color:#2455b8}.error{color:#b42318}"
                + "p{line-height:1.5}</style></head><body><main><h1>" + escape(title)
                + "</h1>" + body + "</main></body></html>");
    }
    static boolean requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
