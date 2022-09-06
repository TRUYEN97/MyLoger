/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyLoger;

import Time.TimeBase;
import java.io.File;
import java.io.FileNotFoundException;
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
               throw new FileSystemException("Can't delete "+file.getPath());
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

    public void addLog(Object txt) {
        for (String line : txt.toString().split("\n")) {
            add(String.format("%s:   %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS), line.trim()));
        }
    }

    public void add(String log) {
        if (!isOpen) {
            System.err.println("Loger has close!");
            System.err.println("can't write: " + log);
            return;
        }
        try {
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

    public void close() throws IOException {
        this.queueLogs.clear();
        if (this.writer != null) {
            this.writer.close();
            isOpen = false;
        }
    }

    public void addLog(String key, Object str) {
        for (String line : str.toString().split("\n")) {
            add(String.format("%s:   [%s] %s\r\n",
                    this.timeBase.getDateTime(TimeBase.UTC,
                            TimeBase.DATE_TIME_MS), key, line.trim()));
        }
    }
}
