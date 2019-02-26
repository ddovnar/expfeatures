package org.dovnard.exp.web;

import org.dovnard.exp.console.CommandExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class WebServer extends ServerSocket {
    private static Logger logger = LoggerFactory.getLogger(WebServer.class);
    public WebServer() throws IOException {
        this(8080);
    }
    public WebServer(int port) throws IOException {
        super(port);
        logger.info("Listening for connection on port " + port + "....");
    }

    public void run(CommandExec cmd) {
        if (cmd != null) {
            cmd.run();
        }
    }
}
