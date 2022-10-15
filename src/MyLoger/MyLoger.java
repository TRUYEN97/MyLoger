/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyLoger;

import Time.TimeBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;

/**
 *
 * @author Administrator
 */
public class MyLoger implements Cloneable {

    private FileWriter writer;
    private File file;
    private final TimeBase timeBase;
    private final List<Queue<String>> queueLogs;

    public MyLoger() {
        this.timeBase = new TimeBase(TimeBase.UTC);
        this.queueLogs = new ArrayList<>();
    }
    
    public MyLoger(TimeZone timeZone) {
        this.timeBase = new TimeBase(timeZone);
        this.queueLogs = new ArrayList<>();
    }

    public void begin(File file, boolean append) throws IOException {
        try {
            if (file != null && file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            this.writer = new FileWriter(file, append);
            this.file = file;
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public void begin(File file, boolean append, boolean override) throws IOException {
        if (file != null && override && file.exists()) {
            try ( FileWriter fw = new FileWriter(file)) {
                fw.write("");
                fw.flush();
            }
        }
        begin(file, append);
    }

    public Queue<String> getQueueLog() {
       return addQueueToList(new ArrayDeque<>());
    }

    public synchronized void addLog(Object txt) throws IOException {
        if (txt == null) {
            add(String.format("%s:  null\r\n",
                    this.timeBase.getDateTime(TimeBase.DATE_TIME_MS)));
            return;
        }
        for (String line : txt.toString().split("\n")) {
            add(String.format("%s:   %s\r\n",
                    this.timeBase.getDateTime(TimeBase.DATE_TIME_MS), line.trim()));
        }
    }

    public synchronized void addLog(String key, Object str) throws IOException {
        if (str == null) {
            add(String.format("%s:  [%s] null\r\n",
                    this.timeBase.getDateTime(  TimeBase.DATE_TIME_MS), key));
            return;
        }
        for (String line : str.toString().split("\n")) {
            add(String.format("%s:   [%s] %s\r\n",
                    this.timeBase.getDateTime(TimeBase.DATE_TIME_MS), key, line.trim()));
        }
    }

    public synchronized void add(String log) throws IOException {
        if (log == null) {
            return;
        }
        if (writer == null) {
            System.err.println("writer == null !");
            System.err.println("can't write: " + log);
            return;
        }
        addToQueue(log);
        this.writer.write(log);
        this.writer.flush();
    }

    private void addToQueue(String log) {
        for (Queue<String> queueLog : queueLogs) {
            queueLog.add(log);
        }
    }

    private Queue<String> addQueueToList(Queue<String> queue) {
        queue.add(getLog());
        this.queueLogs.add(queue);
        return queue;
    }

    public String getLog() {
        if (this.file == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line;
            while ((line = reader.readLine()) != null) {
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
            this.writer = null;
        }
    }
}
