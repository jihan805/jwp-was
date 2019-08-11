package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpSession;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static webserver.http.HttpHeaders.SET_COOKIE;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private RequestDispatcher dispatcher;

    public RequestHandler(Socket connectionSocket, RequestDispatcher dispatcher) {
        this.connection = connectionSocket;
        this.dispatcher = dispatcher;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest httpRequest = HttpRequest.parse(bufferedReader);
            HttpSession session = httpRequest.getSession();

            HttpResponse httpResponse = new HttpResponse(dos);

            if( httpRequest.getCookie("JSESSIONID") == null) {
                httpResponse.addHeader(SET_COOKIE, "JSESSIONID=" + session.getId() + "; Path=/");
            }

            dispatcher.processRequest(httpRequest, httpResponse);
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }
}
