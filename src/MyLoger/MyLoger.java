/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyLoger;

import Time.TimeBase;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public class MyLoger implements Cloneable {

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

    public void begin(File file, boolean append) throws IOException {
        try {
            if (file != null && file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            this.writer = new FileWriter(file, append);
            this.file = file;
            isOpen = true;
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public void begin(File file, boolean append, boolean override) throws IOException {
        if (file != null && override && file.exists()) {
            if (!file.delete()) {
                throw new FileSystemException("Can't delete " + file.getPath());
            }
        }
        begin(file, append);
    }

    public Queue<String> getQueueLog() {
        if (this.queueLogs.isEmpty()) {
            return addQueueToList(this.queuelog);
        }
        return addQueueToList(new ArrayDeque<>(this.queuelog));
    }

    public synchronized void addLog(Object txt) throws IOException {
        if (txt == null) {
            add(String.format("%s:  null\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS)));
            return;
        }
        for (String line : txt.toString().split("\n")) {
            add(String.format("%s:   %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS), line.trim()));
        }
    }

    public synchronized void addLog(String key, Object str) throws IOException {
        if (str == null) {
            add(String.format("%s:  [%s] null\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS), key));
            return;
        }
        for (String line : str.toString().split("\n")) {
            add(String.format("%s:   [%s] %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS), key, line.trim()));
        }
    }

    public synchronized void add(String log) throws IOException {
        if (!isOpen) {
            System.err.println("Loger has close!");
            System.err.println("can't write: " + log);
            return;
        }
        addToQueue(log);
        this.writer.write(log);
        this.writer.flush();
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line;
            while ((line = reader.readLine()) == null) {
                builder.append(line).append("\r\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() throws IOException {
        this.queueLogs.clear();
        if (this.writer != null) {
            this.writer.close();
            isOpen = false;
        }
    }
}
