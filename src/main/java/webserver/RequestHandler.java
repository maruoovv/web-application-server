package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            String[] headers = readHttpHeader(reader);
            String requestPath = HttpRequestUtils.parseRequestPath(headers);
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = null;

            if (requestPath.equals("/index.html")) {
                body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            } else {
                body = "Hello World".getBytes();
            }

            dos.write(body);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String[] readHttpHeader(BufferedReader reader) {
        List<String> headers = new ArrayList<>();

        try {
            String line = "";
            while (true) {
                line = reader.readLine();

                if (line == null || line.equals("")) break;

                headers.add(line);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return headers.toArray(new String[0]);
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
