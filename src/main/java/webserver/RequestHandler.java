package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            // stream을 통해서 요청을 들어오고 나가는 것
            // buffer에 있는 데이터를 읽기 위한 메서드 -> bufferedReader.readLine()
            String[] line = bufferedReader.readLine().split(" ");

            // 들어오는 요청의 URL을 매칭시켜준다고 생각해보기
            String url = "";
            for (String s : line) {
                if (s.startsWith("/"))
                    url = s;
            }

            // step1. if문으로 처리 -> 메서드로 처리할 것
            Map<String, String> parseResult;
            String requestPath = "";
            String params = "";
            if (url.startsWith("/user/create")) {
                requestPath = url.substring(0, url.indexOf("?"));
                params = url.substring(url.indexOf("?") + 1);
            }

            parseResult = HttpRequestUtils.parseQueryString(params);

            User user = new User(parseResult.get("userId"), parseResult.get("password"), parseResult.get("name"), parseResult.get("email"));

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
