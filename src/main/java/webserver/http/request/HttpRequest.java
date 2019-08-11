package webserver.http.request;

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import utils.IOUtils;
import webserver.SessionHolder;
import webserver.http.HttpCookie;
import webserver.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static webserver.http.HttpHeaders.CONTENT_LENGTH;
import static webserver.http.HttpHeaders.COOKIE;

public class HttpRequest {
    private static final String END_OF_LINE = "";
    private static final String HEADER_DELIMITER = ": ";
    private static final int HEADER_PAIR_COUNT = 2;

    private RequestLine requestLine;
    private Map<String, String> headers;
    private HttpCookie httpCookie;
    private RequestBody requestBody;

    private HttpRequest(RequestLine requestLine, Map<String, String> headers, RequestBody requestBody) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.requestBody = requestBody;
        setCookie();
    }

    public static HttpRequest parse(BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine();
        RequestLine requestLine = RequestLine.parse(line);

        Map<String, String> headers = new HashMap<>();

        while (!line.equals(END_OF_LINE)) {
            line = bufferedReader.readLine();
            String[] values = line.split(HEADER_DELIMITER);

            if(hasValues(values)) {
                headers.put(values[0], values[1]);
            }
        }

        String requestBody = StringUtils.EMPTY;
        if (headers.get(CONTENT_LENGTH) != null) {
            requestBody = IOUtils.readData(bufferedReader, Integer.parseInt(headers.get("Content-Length")));
        }

        return new HttpRequest(requestLine, headers, RequestBody.parse(requestBody));
    }

    private void setCookie() {
        String value = headers.get(COOKIE);
        this.httpCookie = HttpCookie.parse(value);
    }

    public HttpSession getSession() {
        String sessionId = httpCookie.getCookie("JSESSIONID");
        HttpSession httpSession = SessionHolder.getSession(sessionId);

        return httpSession;
    }

    public String getCookie(String key) {
        return httpCookie.getCookie(key);
    }

    private static boolean hasValues(String[] values) {
        return values.length == HEADER_PAIR_COUNT;
    }

    public String getRequestUriPath() {
        return requestLine.getRequestUriPath();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public boolean isPostRequest() {
        return requestLine.isPost();
    }

    public boolean isGetRequest() {
        return requestLine.isGet();
    }

    public String getQueryStringParameter(String key) {
        return requestLine.getParameter(key);
    }

    public String getRequestBodyParameter(String key) {
        return requestBody.getParameter(key);
    }

    @Override
    public String toString() {
        return "RequestHeader{" +
                "requestLine=" + requestLine +
                ", headers=" + headers +
                '}';
    }
}
