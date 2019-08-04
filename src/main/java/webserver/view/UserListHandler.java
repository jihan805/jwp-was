package webserver.view;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.base.Charsets;
import db.DataBase;
import model.User;
import webserver.RequestMappingHandler;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserListHandler implements RequestMappingHandler {

    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws IOException {
        if("/user/list".equals(request.getRequestUriPath())) {
            if ("logined=true".equals(request.getHeader("Cookie"))) {
                byte[] body = loadTemplate();

                response.response200Header(body.length, "text/html;");
                response.responseBody(body);

            } else {
                response.response302Header("/user/login.html", false);
            }
        }
    }

    private byte[] loadTemplate() throws IOException {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".html");
        loader.setCharset(Charsets.UTF_8);
        Handlebars handlebars = new Handlebars(loader);

        Template template = handlebars.compile("user/list");
        Collection<User> users = DataBase.findAll();
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);

        String usersPage = template.apply(data);

        return usersPage.getBytes();
    }
}