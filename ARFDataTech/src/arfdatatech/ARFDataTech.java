package arfdatatech;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.util.Pair;

public class ARFDataTech {

    public ARFDataTech() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         Use this main function to set up tests for various parts. I'm going to make a new class
         to write the 'real' experiments we're going to do.
         */
        ARFDataTech adt = new ARFDataTech();
        
        adt.rangeQueries();
        adt.dataChange();
        adt.workloadChange();
    }

    /* 
     Experiments section 5.3.3: Range queries
     */
    private void rangeQueries() {

        //global variables 8a 8b 8c
        int dkeys = 100000;
        int domain = (int) Math.pow(2, 24);
        int[] range = {0, domain};
        int numQueries = 300;

        //zipf 8b
        double exponent = 3;
        int numPeaks = 5;

        //mu value
        int mu_min = 1;
        int mu_max = 5;
        int bitsperkey = 8;

        for (int bpk = 1; bpk <= 10; bpk++) {
            System.out.println("Now processing bpk " + bpk);
            System.out.println("8a");
            rangeQuery_8a(dkeys, domain, range, bpk, numQueries);
            System.out.println("8b");
            rangeQuery_8b(dkeys, domain, range, exponent, numPeaks, bpk, numQueries);
        }

        for (int mu = mu_min; mu <= mu_max; mu++) {
            System.out.println("Now processing mu " + mu);
            System.out.println("8c");
            rangeQuery_8c(dkeys, domain, range, bitsperkey, mu, numQueries);
        }

    }

    private void rangeQuery_8a(int dkeys, int domain, int[] range, int bpkey, int numQueries) {
        DataBaseIndexer db = new DataBaseIndexer("Uniform database", dkeys, domain, new UniformGenerator(domain));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        QueryStrategy qs = new QueryStrategy(numQueries, new UniformGenerator(domain), 30);
        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs);

        runExperiment("Exp_8a-" + bpkey, filters, db, false, queries);
    }

    private void rangeQuery_8b(int dkeys, int domain, int[] range, double exp, int numPeaks, int bpkey, int numQueries) {
        DataBaseIndexer db = new DataBaseIndexer("Uniform database", dkeys, domain, new UniformGenerator(domain));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        Random r = new Random();
        List<Integer> peaks = new ArrayList();
        for (int i = 0; i < numPeaks; i++) {
            peaks.add(r.nextInt(domain));
        }

        QueryStrategy qs = new QueryStrategy(numQueries, new ZipfGenerator(domain, exp, peaks), 30);
        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs);

        runExperiment("Exp_8b-" + bpkey, filters, db, false, queries);
    }

    private void rangeQuery_8c(int dkeys, int domain, int[] range, int bpkey, int mu, int numQueries) {
        DataBaseIndexer db = new DataBaseIndexer("Uniform database", dkeys, domain, new UniformGenerator(domain));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        QueryStrategy qs = new QueryStrategy(numQueries, new UniformGenerator(domain), mu);
        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs);

        runExperiment("Exp_8c-" + bpkey, filters, db, false, queries);
    }
    /* 
     Experiments section 5.6: Adapt to data changes
     */

    private void dataChange() {

        //global variables 11a 11b
        int dkeys = 100000;
        int domain = (int) Math.pow(2, 24);
        int[] range = {0, domain};
        int numQueries = 300;
        int bpk = 8;

        //zipf 8b
        double exponent = 3;
        int numPeaks = 5;

        //mu value
        int mu_min = 1;
        int mu_max = 5;
        int bitsperkey = 8;

        System.out.println("Now processing bpk " + bpk);
        System.out.println("11a");
        rangeQuery_11a(dkeys, domain, range, bpk, numQueries);
        System.out.println("11b");
        rangeQuery_11b(dkeys, domain, range, exponent, numPeaks, bpk, numQueries);

    }

    private void rangeQuery_11a(int dkeys, int domain, int[] range, int bpkey, int numQueries) {
        DataBaseIndexer db = new DataBaseIndexer("Uniform database", dkeys, domain, new UniformGenerator(domain));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        QueryStrategy qs = new QueryStrategy(numQueries, new UniformGenerator(domain), 30);
        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs);

        runExperiment("Exp_11a", filters, db, true, queries);
    }

    private void rangeQuery_11b(int dkeys, int domain, int[] range, double exp, int numPeaks, int bpkey, int numQueries) {
        Random r = new Random();
        List<Integer> peaks = new ArrayList();
        for (int i = 0; i < numPeaks; i++) {
            peaks.add(r.nextInt(domain));
        }

        DataBaseIndexer db = new DataBaseIndexer("Zipf database", dkeys, domain, new ZipfGenerator(domain, exp, peaks));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        QueryStrategy qs = new QueryStrategy(numQueries, new UniformGenerator(domain), 30);
        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs);

        runExperiment("Exp_11b", filters, db, true, queries);
    }

    /* 
     Experiments section 5.7: Adapt to workload changes
     */
    private void workloadChange() {

        //global variables 11a 11b
        int dkeys = 100000;
        int domain = (int) Math.pow(2, 24);
        int[] range = {0, domain};
        int numQueries = 300;
        int bpk = 8;

        //zipf 8b
        double exponent = 3;
        int numPeaks = 5;

        //mu value
        int mu_min = 1;
        int mu_max = 5;
        int bitsperkey = 8;

        System.out.println("Now processing bpk " + bpk);
        System.out.println("11c");
        rangeQuery_11c(dkeys, domain, range, exponent, numPeaks, bpk, numQueries);

    }

    private void rangeQuery_11c(int dkeys, int domain, int[] range, double exp, int numPeaks, int bpkey, int numQueries) {
        DataBaseIndexer db = new DataBaseIndexer("Uniform database", dkeys, domain, new UniformGenerator(domain));

        BloomFilter bloom = new BloomFilter("Bloomfilter " + bpkey + " bpe", domain, bpkey, db);
        ARFFilter arfno = new ARFFilter("No-adapt ARF", domain * bpkey, range);
        ARFFilter arf0b = new ARFFilter("Adapt-0bit ARF", domain * bpkey, range);
        ARFFilter arf1b = new ARFFilter("Adapt-1bit ARF", domain * bpkey, range);
        //TODO ARF variants
        List<Filter> filters = new ArrayList<>();
        filters.add(bloom);
        filters.add(arfno);
        filters.add(arf0b);
        filters.add(arf1b);

        Random r = new Random();
        List<Integer> peaks1 = new ArrayList();
        for (int i = 0; i < numPeaks; i++) {
            peaks1.add(r.nextInt(domain));
        }
        List<Integer> peaks2 = new ArrayList();
        for (int i = 0; i < numPeaks; i++) {
            peaks2.add(r.nextInt(domain));
        }

        QueryStrategy qs1 = new QueryStrategy(numQueries, new ZipfGenerator(domain, exp, peaks1), 30);
        QueryStrategy qs2 = new QueryStrategy(numQueries, new ZipfGenerator(domain, exp, peaks2), 30);

        List<QueryStrategy> queries = new ArrayList<>();
        queries.add(qs1);
        queries.add(qs2);

        runExperiment("Exp_11c", filters, db, false, queries);
    }

    /*
     Code to run experiments
     */
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

                        boolean resultDB = !(db.getKey(key, key + range)).isEmpty();
                        l.logdata(counter, true, resultDB);

                        //false positive!!
                        if (!resultDB) {
                            f.adjustFilter(key, key + range);
                        }

                    } else {
                        boolean resultDB = !(db.getKey(key, key + range)).isEmpty();
                        l.logdata(counter, false, resultDB);
                    }
                }

                counter = 0;
                //done with query for all filters
                if (dbflag && ++counter % 3 == 0) {
                    int change = db.adjustDB();
                    //TODO: update filter with db update!!
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
