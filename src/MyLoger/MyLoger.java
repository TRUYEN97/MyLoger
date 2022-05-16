/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyLoger;

import TimeBase.TimeBase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public class MyLoger {

    private FileWriter writer;
    private Scanner scanner;
    private File file;
    private final TimeBase timeBase;

    public MyLoger() {
        this.timeBase = new TimeBase();
    }

    public boolean begin(File file, boolean append) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            this.writer = new FileWriter(file, append);
            this.file = file;
            return true;
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
    }

    public boolean begin(File file, boolean append, boolean override) {
        if (override && file.exists()) {
            if (!file.delete()) {
                return false;
            }
        }
        return begin(file, append);
    }

    public void addLog(Object txt) {
        if (txt == null) {
            add("null");
            return;
        }
        String data = txt.toString();
        for (String line : data.split("\n")) {
            add(line);
        }
    }

    private void add(String data) {
        try {
            this.writer.write(String.format("%s : %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.SIMPLE_DATE_TIME), data.trim()));
            this.writer.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public Scanner getLogScanner() {
        try {
            return new Scanner(this.file);
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            return null;
        }
    }

    public String getLog() {
        StringBuilder builder = new StringBuilder();
        try {
            this.scanner = new Scanner(this.file);
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return null;
        }
    }
}
