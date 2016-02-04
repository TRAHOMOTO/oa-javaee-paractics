package ua.org.oa.trahomoto.controllers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

public class Index extends HttpServlet {

    private static final String TEMPLATE = "/templates/page-index.jsp";
    private static ResourceBundle i18n;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        i18n = ResourceBundle.getBundle(config.getInitParameter("i18n"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        render(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /**
         * import javax.servlet.jst.jstl.core.Config;
         * Config.set( session, Config.FMT_LOCALE, new java.util.Locale("en_US") )
         */
        request.setAttribute("bundleBaseName", getInitParameter("i18n"));

        render(request, response);
    }

    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("token", Math.random()*1_000_000);
        getServletContext()
                .getRequestDispatcher(TEMPLATE)
                .forward(request, response);
    }
}
