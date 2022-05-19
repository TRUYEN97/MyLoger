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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public class MyLoger {

    private FileWriter writer;
    private File file;
    private final TimeBase timeBase;
    private final List<Queue<String>> queueLogs;
    private final Queue<String> queuelog;
    private boolean isOpen;

    public MyLoger() {
        this.timeBase = new TimeBase();
        this.queueLogs = new ArrayList<>();
        this.queuelog = new ArrayDeque<>();
    }

    public boolean begin(File file, boolean append) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            this.writer = new FileWriter(file, append);
            this.file = file;
            return isOpen = true;
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

    public Queue<String> getQueueLog() {
        if (this.queueLogs.isEmpty()) {
            return addQueueToList(this.queuelog);
        }
        return addQueueToList(new ArrayDeque<>(this.queuelog));
    }

    public void addLog(Object txt) {
        if (txt == null) {
            add("null");
            return;
        }
        for (String line : txt.toString().split("\n")) {
            add(line);
        }
    }

    private void add(String data) {
        if (!isOpen) {
            System.err.println("Loger has close!");
            return;
        }
        try {
            String log = String.format("%s : %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.SIMPLE_DATE_TIME), data.trim());
            addToQueue(log);
            this.writer.write(log);
            this.writer.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            isOpen = false;
        }
    }

    private void addToQueue(String log) {
        if (queueLogs.isEmpty()) {
            this.queuelog.add(log);
        }
        for (Queue<String> queueLog : queueLogs) {
            queueLog.add(log);
        }
    }

    private Queue<String> addQueueToList(Queue<String> queue) {
        this.queueLogs.add(queue);
        return this.queuelog;
    }

    public String getLog() {
        StringBuilder builder = new StringBuilder();
        try ( Scanner scanner = new Scanner(this.file)) {
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine()).append("\r\n");
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return null;
        }
    }

    public void close() {
        this.queueLogs.clear();
        if (this.writer != null) {
            try {
                this.writer.close();
                isOpen = false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
