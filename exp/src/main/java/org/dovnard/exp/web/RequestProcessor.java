package org.dovnard.exp.web;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface RequestProcessor {
    public void process(BufferedReader in, PrintWriter out) throws IOException;
    public void process(JSONObject in, PrintWriter out) throws IOException;
    public void process(String in, PrintWriter out) throws IOException;
}
