package org.dovnard.exp.util;

import java.text.NumberFormat;

public class MemoryInfo {
    private static Runtime runtime = Runtime.getRuntime();
    private static NumberFormat format = NumberFormat.getInstance();
    private MemoryInfo() {
    }
    public static String getFreeMemory() {
        //runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        try {
            return new String(format.format(freeMemory / 1024).getBytes("UTF-8"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "---";
    }
    public static String getAllocatedMemory() {
        //runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory();
        try {
            return new String(format.format(allocatedMemory / 1024).getBytes("UTF-8"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "---";
    }
    public static String getMaxMemory() {
        //runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        try {
            return new String(format.format(maxMemory / 1024).getBytes("UTF-8"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "---";
    }
    public static String getTotalFreeMemory() {
        //runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        try {
            return new String(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024).getBytes("UTF-8"));
        }catch(Exception e) {
            e.printStackTrace();
        }
        return "---";
    }
    public static String getUsedMemory() {
        //runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        try {
            return new String(format.format(used / 1024).getBytes("UTF-8"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "---";
    }
}
