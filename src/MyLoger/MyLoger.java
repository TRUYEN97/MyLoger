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
public class MyLoger {

    private File file;
    private TimeBase timeBase;
    private StringBuilder log;
    private boolean saveMemory;
    private List<Queue<String>> queueLogs;

    public MyLoger() {
        this(TimeBase.UTC);
    }

    public MyLoger(TimeZone timeZone) {
        this.timeBase = new TimeBase(timeZone);
        this.log = new StringBuilder();
        this.saveMemory = false;
        this.queueLogs = new ArrayList<>();
    }

    public void setSaveMemory(boolean saveMemory) {
        this.saveMemory = saveMemory;
        if (saveMemory) {
            this.log.delete(0, this.log.length());
        }
    }

    public void setFile(File file) {
        if (file == null) {
            return;
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        this.file = file;
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

    public synchronized void addLog(String str, Object... params) throws Exception {
        String strLog = String.format(str, params);
        addLog(strLog);
    }

    public synchronized void addLog(String key, Object str) throws IOException {
        if (str == null) {
            add(String.format("%s:  [%s] null\r\n",
                    this.timeBase.getDateTime(TimeBase.DATE_TIME_MS), key));
            return;
        }
        for (String line : str.toString().split("\n")) {
            add(String.format("%s:   [%s] %s\r\n",
                    this.timeBase.getDateTime(TimeBase.DATE_TIME_MS), key, line.trim()));
        }
    }

    public synchronized void addLog(String key, String str, Object... params) throws Exception {
        String strLog = String.format(str, params);
        addLog(key, strLog);
    }

    public synchronized void add(String log) throws IOException {
        if (log == null) {
            return;
        }
        if (!saveMemory) {
            this.log.append(log);
        }
        addToQueue(log);
        if (file != null) {
            try ( FileWriter writer = new FileWriter(file, true)) {
                writer.write(log);
                writer.flush();
            }
        }
    }

    public synchronized void setLog(String log) throws IOException {
        if (log == null) {
            return;
        }
        if (!saveMemory) {
            this.log.delete(0, this.log.length());
            this.log.append(log);
        }
        if (file != null) {
            try ( FileWriter writer = new FileWriter(file)) {
                writer.write(log);
                writer.flush();
            }
        }
    }

    public void clear() {
        try {
            setLog("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getLog() throws IOException {
        if (saveMemory) {
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
            }
        } else {
            return log.toString();
        }
    }

    public Queue<String> getQueueLog() {
        try {
            return addQueueToList(new ArrayDeque<>());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void addToQueue(String log) {
        for (Queue<String> queueLog : queueLogs) {
            queueLog.add(log);
        }
    }

    private Queue<String> addQueueToList(Queue<String> queue) throws IOException {
        queue.add(getLog());
        this.queueLogs.add(queue);
        return queue;
    }
}
