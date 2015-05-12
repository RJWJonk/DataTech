package arfdatatech;

import java.util.List;
import java.util.Random;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipfGenerator implements NumberGenerator {

    private ZipfDistribution zd;
    private int range_max;
    private int domain;
    private List<Integer> peaks;
    private Random random;

    ZipfGenerator(int range_max, double exp, List<Integer> peaks) {
        this.range_max = range_max/2;
        this.domain = range_max;
        this.peaks = peaks;
        random = new Random();

        //System.out.println("Creating distribution...");
        zd = new ZipfDistribution(this.range_max, exp);
        //System.out.println("Done creating distribution...");
        
//        for (int i = 0; i < range_max; i++) {
//            System.out.println(i + ": " + zd.cumulativeProbability(i));
//        }
    }

    @Override
    public int getNext() {
        double comparator = random.nextDouble();
        
        int low = 0;
        int high = range_max;
        int result = range_max / 32;
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
                result = 2*low/4 + high/2;
            } else {
                low = result;
                result = 2*low/4 + high/2;
            }
                
            }
        }
        //System.out.println("generated: " + result);

        result = (random.nextInt(2) > 0) ? -result : result;
        //System.out.println("flipped: " + result);

        int translation = peaks.get(random.nextInt(peaks.size()));
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

}
