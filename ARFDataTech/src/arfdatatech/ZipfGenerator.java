package arfdatatech;

import java.util.List;
import java.util.Random;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipfGenerator implements NumberGenerator {

    private ZipfDistribution zd;
    private int gendomain;
    private int domain;

    private List<Integer> peaks;
    private Random rnd;

//    private double exp;
//    private double bottom;

    ZipfGenerator(int domain, double exp, List<Integer> peaks) {
        this.gendomain = (int) Math.ceil(Math.sqrt(domain/2));
        this.domain = domain;
        this.peaks = peaks;
        rnd = new Random();

//        this.exp = exp;
//        this.bottom = 0;
//        for (int i = 1; i < domain; i++) {
//            this.bottom += (1 / Math.pow(i, this.exp));
//        }

        //System.out.println("Creating distribution...");
        zd = new ZipfDistribution(this.gendomain, exp);
        //System.out.println("Done creating distribution...");

//        for (int i = 0; i < range_max; i++) {
//            System.out.println(i + ": " + zd.cumulativeProbability(i));
//        }
    }

    private int getValue() {
        double comparator = rnd.nextDouble();
        int low = 0;
        int high = gendomain;
        int result = gendomain / 2;
        while (low + 1 < high) {
            //System.out.println(zd.cumulativeProbability(result) + " < " + comparator + " && " +  comparator + " <= " + zd.cumulativeProbability(result+1));
            //System.out.println(low + " <= " + result + " <= " + high);
            //System.out.println("Calculating probabilty of result+1");
            double temp1 = zd.cumulativeProbability(result+1);
            //System.out.println("Calculating probabilty of result");
            double temp = zd.cumulativeProbability(result);
            if (temp < comparator && comparator <= temp1) {
                //System.out.println("FOUND");
                break;
            } else { if (comparator <= temp1) {
                high = result;
                result = (low + high)/2;
            } else {
                low = result;
                result = (low + high)/2;
            }
                
            }
        }
        return result;
    }
    
    @Override
    public int getNext() {
        int result1 = getValue();
        int result2 = rnd.nextInt(gendomain);
        
        int result = gendomain*result1+result2;
        //System.out.println("generated: " + result);

        result = (rnd.nextInt(2) > 0) ? -result : result;
        //System.out.println("flipped: " + result);

        int translation = peaks.get(rnd.nextInt(peaks.size()));
        result += translation;
        //System.out.println("translated: " + result);
        
        if (result < 0) {
            result += domain;
        }
        if (result >= domain) {
            result -= domain;
        }

        //System.out.println("end: " + result);
        return result;
    }
    
    
//    @Override
//    public int getNext() {
//        int rank;
//        double frequency = 0;
//        double dice;
//
//        rank = rnd.nextInt(range_max);
//        frequency = (1.0d / Math.pow(rank, this.exp)) / this.bottom;
//        dice = rnd.nextDouble();
//
//        while (dice >= frequency) {
//            rank = rnd.nextInt(range_max);
//            frequency = (1.0d / Math.pow(rank, this.exp)) / this.bottom;
//            dice = rnd.nextDouble();
//        }
//        
//        System.out.println("Generated " + rank);
//
//        int result = rank;
//        
//                result = (rnd.nextInt(2) > 0) ? -result : result;
//        //System.out.println("flipped: " + result);
//
//        int translation = peaks.get(rnd.nextInt(peaks.size()));
//        result += translation;
//        //System.out.println("translated: " + result);
//        
//        if (result < 0) {
//            result += domain;
//        }
//        if (result >= domain) {
//            result -= domain;
//        }
//
//        //System.out.println("end: " + result);
//        return result;
//    }

}
