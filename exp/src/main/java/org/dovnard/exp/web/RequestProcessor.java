package org.dovnard.exp.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface RequestProcessor {
    public void process(BufferedReader in, PrintWriter out) throws IOException;
}
