package org.dovnard.exp.web;

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
    public WebServerAlone() throws IOException {
        this(8080);
    }
    public WebServerAlone(int port) throws IOException {
        logger.info("Listening for connection on port " + port + "....");
        server = new ServerSocket(port);
    }

    public void run() {
        while (true) {
            try {
                logger.info("Start request");
                try (Socket socket = server.accept()) {
                    logger.info("in accept");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Date today = new Date();

                    out.print("HTTP/1.1 200 \r\n");
                    out.print("Content-Type: text/plain\r\n"); // The type of data
                    out.print("Connection: close\r\n"); // Will close stream
                    out.print("\r\n"); // End of headers

                    out.print("FROM WebServer method" + "\r\n");
                    logger.info("before process");
                    processRequest(in, out);
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
    private void  processRequest(BufferedReader in, PrintWriter out) throws IOException {
        if (procreq != null) {
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
}
