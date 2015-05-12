package arfdatatech;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.util.Pair;   

public class ARFDataTech {

    public ARFDataTech() {
        do_main();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
        Use this main function to set up tests for various parts. I'm going to make a new class
        to write the 'real' experiments we're going to do.
        */
        
//        String test = "test";
//        NumberGenerator uni = new UniformGenerator(0, 20);
//
//        Logger l = new Logger(test);
//        l.logmeta("testfilter", "gaussian", "lognormal", true, false);
//        for (int i = 0; i < 10; i++) {
//            l.logdata(i, uni.getNext() % 2 == 0, uni.getNext() % 2 == 0);
//        }
        ArrayList<Integer> peaks = new ArrayList();
        peaks.add((int)Math.pow(2,1));
        peaks.add((int)Math.pow(2,1));
        int range = (int) Math.pow(2, 24);        
        int randomcount = 10;
        
        
        NumberGenerator rng1 = new ZipfGenerator(range, 1, peaks);
        NumberGenerator rng2 = new UniformGenerator(range);
        
       // int[][] results = new int[range][2];
        
        for (int i = 0; i < randomcount; i++) {
            //results[rng1.getNext()][0]++;
            //results[rng2.getNext()][1]++;
            System.out.println("Zipf: " + rng1.getNext() + "\tUniform: " + rng2.getNext());
        }
//        for (int i = 0; i < results.length; i++) {
//            System.out.println("Value " + i + "\t Zipf: " + results[i][0] + "\t Uniform: " + results[i][1]);
//        }
        
        //new ARFDataTech();

    }

    private void do_main() {
        List<Filter> filters = new ArrayList();
        ARFFilter arf = new ARFFilter("ARFtest", 50);
        ARFFilter arf2 = new ARFFilter("ARFtest2", 50);
        filters.add(arf);
        filters.add(arf2);
        List<QueryStrategy> queries = new ArrayList();
        QueryStrategy qs = new QueryStrategy(20, new UniformGenerator(50), 30);
        queries.add(qs);
        qs = new QueryStrategy(10, new UniformGenerator(50), 0);
        queries.add(qs);
        runExperiment("test", filters, new DataBaseIndexer("DBtest", 50), false, queries);
    }

    public short runExperiment(String name, List<Filter> filters, DataBaseIndexer db, boolean dbflag, List<QueryStrategy> queries) {

        Random r = new Random();

        List<Pair<Filter, Logger>> FLpairs = new ArrayList();

        for (Filter f : filters) {
            Logger l = new Logger(name + "-" + f.name);
            FLpairs.add(new Pair(f, l));
            l.logmeta(f.name, db.name, "todo", dbflag, queries.size() > 1);
        }

        int counter = 0;

        for (QueryStrategy qs : queries) {
            int todo = qs.numQuery;
            while (todo > 0) {

                int key = qs.strategy.getNext();
                int range = (int) r.nextGaussian() * qs.mu / 3 + qs.mu;

                for (Pair<Filter, Logger> fl : FLpairs) {
                    Filter f = fl.getKey();
                    Logger l = fl.getValue();

                    boolean resultFilter = f.query(key, key + range);
                    if (resultFilter) {
                        boolean resultDB = db.getKey(key, key + range);
                        l.logdata(counter, true, resultDB);
                        
                        //false positive!!
                        if (!resultDB) {
                            f.adjustFilter(key, key + range);
                        }

                    } else {
                        l.logdata(counter, false, false);
                    }
                }

                //done with query for all filters
                if (dbflag) {
                    db.adjustDB();
                }
                counter++;
                todo--;
            }
            //start new query strategy
        }

        return 0;
    }

    public class QueryStrategy {

        private int numQuery;
        private NumberGenerator strategy;
        private int mu;

        public QueryStrategy(int numQuery, NumberGenerator strategy, int mu) {
            this.numQuery = numQuery;
            this.strategy = strategy;
            this.mu = mu;
        }
    }

}
