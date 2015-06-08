/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 *
 * @author s080440
 */
public class Logger {

    private String name;

    private boolean started = false;

    double dataHits;
    double dataAccesses;

    double dataHits_recent;
    double dataAccesses_recent;
    int totalQueries;
    int interval = 300;

    public Logger(String name) {
        this.name = name;
    }

    public boolean logdata(int id, boolean filter, boolean data) {
        id++;
        File f = new File(name + "-data.csv");

        if (!started) {
            f.delete();
            started = true;
            dataHits = 0;
            dataAccesses = 0;
            dataAccesses_recent = 0;
            dataHits_recent = 0;
        }

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f, "UTF-8");
                writer.println("queryID;filterResult;dataResult;fprate_rec;fprate");
                writer.close();
            } catch (Exception e) {
                return false;
            }
            dataHits = 0;
            dataAccesses = 0;
            dataAccesses_recent = 0;
            dataHits_recent = 0;
        }

        if (totalQueries % interval == 0) {
            dataAccesses_recent = 0;
            dataHits_recent = 0;
        }

        totalQueries++;
        if (filter) {
            dataAccesses++;
            dataAccesses_recent++;
        }
        if (data) {
            dataHits++;
            dataHits_recent++;
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f, true)))) {
            writer.println(id + ";" + filter + ";" + data + ";" + (1 - dataHits_recent / dataAccesses_recent) + ";" + (1 - dataHits / dataAccesses));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean logmeta(String filter, String data, String workload, boolean datachange, boolean workloadchange) {
        try {
            PrintWriter writer = new PrintWriter(name + "-metadata.txt", "UTF-8");
            writer.println("Filter: " + filter);
            writer.println("Data: " + data + ", data change is " + datachange);
            writer.println("Workload: " + workload + ", workload change is " + workloadchange);
            writer.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
