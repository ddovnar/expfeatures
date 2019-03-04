package org.dovnard.exp.web;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class WebServerAlone implements AutoCloseable {
    private static Logger logger = LoggerFactory.getLogger(WebServer.class);
    private ServerSocket server = null;
    private RequestProcessor procreq = null;
    private String origin = "";
    public WebServerAlone() throws IOException {
        this(8080);
    }
    public WebServerAlone(int port) throws IOException {
        logger.info("Listening for connection on port " + port + "....");
        server = new ServerSocket(port);
    }

    private HttpContentType processHeader(BufferedReader in) throws IOException {
        HttpContentType contentType = HttpContentType.PLAIN_TEXT;

        String headerLine = null;
        while((headerLine = in.readLine()).length() != 0){
            logger.info("Header:" + headerLine);
            if (headerLine.indexOf("Content-Type") > -1) {
                logger.info("Content-Type:" + headerLine);
                if (headerLine.indexOf("application/json") > -1 || headerLine.indexOf("application/x-www-form-urlencoded") > -1) {
                    contentType = HttpContentType.JSON;
                }
            }
            if (headerLine.indexOf("Origin",0) > -1) {
                logger.info("Origin:" + headerLine);
                origin = headerLine.substring(headerLine.indexOf(":") + 1, headerLine.length()).trim();
            }
        }
        return contentType;
    }
    private String getRequestDataString(BufferedReader in) throws IOException {
        StringBuilder payload = new StringBuilder();
        while(in.ready()){
            payload.append((char) in.read());
        }
        return payload.toString();
    }
    private JSONObject getRequestDataJSON(BufferedReader in) throws IOException {
        JSONObject jsonObject = new JSONObject(getRequestDataString(in));
        return jsonObject;
    }
    public void run() {
        while (true) {
            try {
                logger.info("Start request");
                try (Socket socket = server.accept()) {
                    logger.info("in accept");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    //Date today = new Date();

                    HttpContentType contentType = processHeader(in);

                    out.print("HTTP/1.1 200 \r\n");
                    if (contentType == HttpContentType.JSON)
                        out.print("Content-Type: application/json; charset=utf-8\r\n");
                    else
                        out.print("Content-Type: text/plain\r\n"); // The type of data
                    //out.print("Connection: close\r\n"); // Will close stream
                    if (origin.isEmpty())
                        out.print("Access-Control-Allow-Origin: *\r\n");
                    else
                        out.print("Access-Control-Allow-Origin: " + origin + "\r\n");
                    out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
                    out.print("Access-Control-Max-Age: 3600\r\n");
                    out.print("Access-Control-Allow-Credentials: true\r\n");
                    out.print("Access-Control-Allow-Headers: authorization, content-type, xsrf-token\r\n");
                    out.print("Access-Control-Expose-Headers: xsrf-token\r\n");
                    out.print("\r\n"); // End of headers

                    //out.print("FROM WebServer method" + "\r\n");
                    logger.info("before process");
//                    if (contentType == HttpContentType.JSON)
//                        processRequest(getRequestDataJSON(in), out);
//                    else if (contentType == HttpContentType.PLAIN_TEXT)
//                        processRequest(getRequestDataString(in), out);
//                    else
                        processRequest(contentType, in, out);
//                        String line;
//                        while ((line = in.readLine()) != null) {
//                            if (line.length() == 0)
//                                break;
//                            out.print("BODY:" + line + "\r\n");
//                        }
                    logger.info("after process");
                    out.close();
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void setRequestProcessor(RequestProcessor req) {
        procreq = req;
    }
    private void  processRequest(HttpContentType contentType, BufferedReader in, PrintWriter out) throws IOException {
        if (procreq != null) {
            logger.info("Detected content:" + contentType.toString());
            if (contentType == HttpContentType.JSON)
                procreq.process(getRequestDataJSON(in), out);
            else if (contentType == HttpContentType.PLAIN_TEXT)
                procreq.process(getRequestDataString(in), out);
            else
                procreq.process(in, out);
        }
    }
    @Deprecated
    public void processRequestSimple() {
        try {
            try (Socket socket = server.accept()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Date today = new Date();

                out.print("HTTP/1.1 200 \r\n");
                out.print("Content-Type: text/plain\r\n"); // The type of data
                out.print("Connection: close\r\n"); // Will close stream
                out.print("\r\n"); // End of headers

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.length() == 0)
                        break;
                    out.print("BODY:" + line + "\r\n");
                }

                out.close();
                in.close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (server != null)
            server.close();
    }

    enum HttpContentType {
        PLAIN_TEXT,
        JSON
    }
}
