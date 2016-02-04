package ua.org.oa.trahomoto.controllers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.jsp.jstl.core.Config;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 * <h1>Класс "Загружака"</h1>
 * <p>Класс с реализацией обработки аплоада файлов из html формы</p>
 *
 * todo - перенести часть функционала (token, preprocess) в фильтры
 *
 */
@MultipartConfig
public class Uploader extends HttpServlet {

    private static final String TEMPLATE = "/templates/page-index.jsp";
    private static Path uploadPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // <-- Рекомендуют именно такое использование

        String servletCtxPath = getServletContext().getRealPath(getServletContext().getContextPath());
        uploadPath = Paths.get(servletCtxPath, config.getInitParameter("imgPath"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        preprocessRequest(request, response);
        render(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        preprocessRequest(request, response);
        request.setAttribute("hasError", false);

        ResourceBundle i18n = (ResourceBundle) request.getAttribute("resourceBundle");

        if (!isValidFormToken(request)) {
            response.sendRedirect(getServletContext().getContextPath());
            return;
        }

        Collection<Part> parts = request.getParts();

        if (parts.isEmpty()) {
            request.setAttribute("hasError", true);
            request.setAttribute("errMessage", i18n.getString("form.error.noupload"));
            render(request, response);
            return;
        }

        Map<String, String> errMessages = new LinkedHashMap<>();
        Map<String, String> infoMessages = new LinkedHashMap<>();

        // Обработка полей формы
        parts.forEach((part) -> {

            // обрабатывает только указанный file input
            if (part.getName().equals(getInitParameter("imgFieldName"))) {

                if (isInvalidImagePart(part)) { // Файл не прошел проверку
                    errMessages.put(part.getSubmittedFileName(), i18n.getString("form.error.mime"));
                    request.setAttribute("hasError", true);
                } else {
                    try {
                        processPart(part); // Пересохранение картинки
                    } catch (IOException e) {

                        errMessages.put(part.getSubmittedFileName(), i18n.getString("error.process.image"));
                        request.setAttribute("hasError", true);

                        log(e.getLocalizedMessage(), e);
                    }
                    // Сообщение об успехе
                    infoMessages.put(part.getSubmittedFileName(), i18n.getString("form.img.process.success"));
                }
            }
        });

        // Передача сообщений во view
        request.setAttribute("errMessages", errMessages);
        request.setAttribute("infoMessages", infoMessages);

        render(request, response);
    }

    /**
     * Проверит токен формы
     *
     * @param request HttpServletRequest
     * @return true если token актуален, иначе - false
     */
    private boolean isValidFormToken(HttpServletRequest request) {
        return request.getParameter("form_token").equals(request.getSession().getAttribute("form_token"));
    }

    /**
     * Обертка для {@link #isValidImagePart(Part)}
     *
     * @param part Part с проверяемым файлом
     * @return true если переданный Part недопустимого MIME типа, иначе false
     */
    private boolean isInvalidImagePart(Part part) {
        return !isValidImagePart(part);
    }

    /**
     * Дополнит запрос данными и передаст JSP на отрисовку
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     * @throws ServletException
     * @throws IOException
     */
    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Список строковых путей к изображениям
        List<String> images = new LinkedList<>();

        // часть URL, по которому будут предоставляться изображения методом GET
        // todo - запилить сервлет для отдачи картинок
        String preUri
                = (request.getContextPath().isEmpty() ? "" : request.getContextPath() + "/")
                + getInitParameter("imgPath") + "/";

        // Перебор всех файлов в каталоге картинок
        // todo - проверить тип файла
        if (Files.exists(uploadPath)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(uploadPath)) {
                dirStream.forEach(path -> {
                    images.add(preUri + path.getFileName().toString());
                });
            }
        }

        // Передача данных во View
        request.setAttribute("images", images);
        request.setAttribute("imgFieldName", getInitParameter("imgFieldName"));

        // Токен для защиты формы от повторного сабмита по Refresh
        String token = System.currentTimeMillis() + request.getSession().getId();
        request.setAttribute("form_token", token);
        request.getSession().setAttribute("form_token", token);

        // Render
        getServletContext()
                .getRequestDispatcher(TEMPLATE)
                .forward(request, response);
    }

    /**
     * Добавит в запрос token для внутреннего использования.
     * Предотвращает прямое обращение по URL к JSP
     *
     * todo - переместить в фильтр
     *
     * @param request
     */
    private void addToken(HttpServletRequest request) {
        request.getSession(true);
        request.setAttribute("token", request.getSession().getId());
    }

    /**
     * Добавляет всякую нужную информацию в запрос
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     * @throws ServletException
     * @throws IOException
     */
    private void preprocessRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        addToken(request);

        Locale currSessionJstlLang = (Locale) Config.get(request.getSession(), Config.FMT_LOCALE);

        Locale i18n;
        // Переключение языка через параметр напр. http://localhost/index?lang=ua
        if (nonNull(request.getParameter("lang"))) {

            Locale reqParamLocale = new Locale(request.getParameter("lang"));
            Config.set(request.getSession(), Config.FMT_LOCALE, reqParamLocale);
            i18n = reqParamLocale;
        // Проверка языка в сесии
        } else if (isNull(currSessionJstlLang)) {
            // Язык не указан, берем локаль из запроса (браузера)
            Config.set(request.getSession(), Config.FMT_LOCALE, request.getLocale());
            i18n = (Locale) Config.get(request.getSession(), Config.FMT_LOCALE);

        } else {
            // Язык уже сохранен в сесии
            i18n = currSessionJstlLang;
        }

        // Запоминаем для последующего использования
        // todo - переместить в фильтр
        request.setAttribute("lang", i18n.getLanguage());
        request.setAttribute("bundleBaseName", getInitParameter("i18n"));
        request.setAttribute("resourceBundle", ResourceBundle.getBundle((String) request.getAttribute("bundleBaseName"), i18n));
    }

    /**
     *
     * @param part
     * @throws IOException
     */
    private void processPart(Part part) throws IOException {
        // Залоггируем параметры обрабатываемого файла
        log(String.format("%s %s %s", part.getSubmittedFileName(), part.getContentType(), part.getSize()));

        if (!Files.exists(uploadPath)) {
            Files.createDirectory(uploadPath);
        }

        // Перенос временного файла в постоянный
        String fileName = System.nanoTime() + resolveExtension(part);
        Files.copy(part.getInputStream(), uploadPath.resolve(fileName), REPLACE_EXISTING);
    }

    /**
     * Проверка MIME и размера файла
     *
     * @param part Part с проверяемым файлом
     * @return true если файл допустимого типа, иначе - false
     */
    private boolean isValidImagePart(Part part) {
        switch (part.getContentType()) {
            case "image/jpeg": // no brake
            case "image/png":
                // do nothing
                break;

            default:
                return false;
        }

        return part.getSize() > 0;
    }

    /**
     * Используя MIME Part вернет расширение для файла
     *
     * @param part Part с проверяемым файлом
     * @return расширение файла этого типа
     */
    private String resolveExtension(Part part) {
        switch (part.getContentType()) {
            case "image/jpeg":
                return ".jpg";

            case "image/png":
                return ".png";

            default:
                throw new IllegalArgumentException(part.getContentType() + ": unsupported image MIME");
        }
    }
}
