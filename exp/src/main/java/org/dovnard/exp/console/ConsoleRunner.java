package org.dovnard.exp.console;

import java.util.Scanner;

public class ConsoleRunner {
    private CommandExec command;
    private boolean exit = false;
    private Scanner scanner = new Scanner(System.in);

    public void setCommand(CommandExec cmd) {
        command = cmd;
    }
    public void run() {
        if (command != null) {
            command.run();
        }
    }
    public Scanner getScanner() {
        return scanner;
    }
    public boolean isExit() {
        return exit;
    }
    public String askKey(String message) {
        System.out.println(message);
        return scanner.nextLine();
    }
    public void askToQuit() {
        System.out.println("Do you want to quit (Y/N)?: ");
        String answer = scanner.nextLine();
        exit = answer.equalsIgnoreCase("Y");
    }
}
