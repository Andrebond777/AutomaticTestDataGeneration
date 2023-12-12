import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CipollasAlgorithm {
    private static final BigInteger BIG = BigInteger.TEN.pow(50).add(BigInteger.valueOf(151));
    private static final BigInteger BIG_TWO = BigInteger.valueOf(2);

    private static class Point {
        BigInteger x;
        BigInteger y;

        //Approach Level Coverage
        boolean[] coverageTargets;

        Point(BigInteger x, BigInteger y, boolean[] coverageTargets) {
            this.x = x;
            this.y = y;
            this.coverageTargets = coverageTargets;
        }

        @Override
        public String toString() {
            short coverage = 0;
            for(int i = 0; i < coverageTargets.length; i++)
            {
                if(coverageTargets[i])
                    coverage++;
            }
            return String.format("(%s, %s, %d)", this.x, this.y, coverage);
        }
    }

    public static class Triple {
        BigInteger x;
        BigInteger y;
        boolean b;

        //Approach Level Coverage
        boolean[] coverageTargets;

        Triple(BigInteger x, BigInteger y, boolean b, boolean[] coverageTargets) {
            this.x = x;
            this.y = y;
            this.b = b;
            this.coverageTargets = coverageTargets;
        }

        @Override
        public String toString()
        {
            StringBuilder coverage = new StringBuilder();
            for (int i = 0; i < coverageTargets.length; i++)
                if(coverageTargets[i])
                    coverage.append(" true");
                else
                    coverage.append(" false");
            return String.format("(%s, %s, %s, %s)", this.x, this.y, this.b, coverage);
        }
    }

    public static Triple c(String ns, String ps) {

        //Assigning each target initial level value
        //class c has 8 targets
        //Since all targets have only one level, I decided to have just a boolean array
        //Initially all members are false - not reached the target, true - reached the target
        boolean[] coverageTargets = new boolean[8];

        BigInteger n = new BigInteger(ns);
        BigInteger p = !ps.isEmpty() ? new BigInteger(ps) : BIG;

        // Legendre symbol, returns 1, 0 or p - 1
        Function<BigInteger, BigInteger> ls = (BigInteger a)
                -> a.modPow(p.subtract(BigInteger.ONE).divide(BIG_TWO), p);

        // Step 0, validate arguments
        if (!ls.apply(n).equals(BigInteger.ONE)) {
            coverageTargets[0] = true;
            return new Triple(BigInteger.ZERO, BigInteger.ZERO, false, coverageTargets);
        }

        // Step 1, find a, omega2
        BigInteger a = BigInteger.ZERO;
        BigInteger omega2;
        while (true) {
            omega2 = a.multiply(a).add(p).subtract(n).mod(p);
            if (ls.apply(omega2).equals(p.subtract(BigInteger.ONE))) {
                coverageTargets[1] = true;
                break;
            }
            coverageTargets[2] = true;
            a = a.add(BigInteger.ONE);
        }

        // multiplication in Fp2
        BigInteger finalOmega = omega2;
        BiFunction<Point, Point, Point> mul = (Point aa, Point bb) -> new Point(
                aa.x.multiply(bb.x).add(aa.y.multiply(bb.y).multiply(finalOmega)).mod(p),
                aa.x.multiply(bb.y).add(bb.x.multiply(aa.y)).mod(p),
                coverageTargets
        );

        // Step 2, compute power
        Point r = new Point(BigInteger.ONE, BigInteger.ZERO, coverageTargets);
        Point s = new Point(a, BigInteger.ONE, coverageTargets);
        BigInteger nn = p.add(BigInteger.ONE).shiftRight(1).mod(p);
        while (nn.compareTo(BigInteger.ZERO) > 0) {
            if (nn.and(BigInteger.ONE).equals(BigInteger.ONE)) {
                coverageTargets[3] = true;
                r = mul.apply(r, s);
            }
            coverageTargets[4] = true;
            s = mul.apply(s, s);
            nn = nn.shiftRight(1);
        }

        // Step 3, check x in Fp
        if (!r.y.equals(BigInteger.ZERO)) {
            coverageTargets[5] = true;
            return new Triple(BigInteger.ZERO, BigInteger.ZERO, false, coverageTargets);
        }

        // Step 5, check x * x = n
        if (!r.x.multiply(r.x).mod(p).equals(n)) {
            coverageTargets[6] = true;
            return new Triple(BigInteger.ZERO, BigInteger.ZERO, false, coverageTargets);
        }

        coverageTargets[7] = true;
        // Step 4, solutions
        return new Triple(r.x, p.subtract(r.x), true, coverageTargets);
    }

    public static void main(String[] args) {
//        System.out.println(c("10", "13"));
//        System.out.println(c("56", "101"));
//        System.out.println(c("8218", "10007"));
//        System.out.println(c("8219", "10007"));
//        System.out.println(c("331575", "1000003"));
//        System.out.println(c("665165880", "1000000007"));
//        System.out.println(c("881398088036", "1000000000039"));
//        System.out.println(c("34035243914635549601583369544560650254325084643201", ""));
        System.out.println(c("0920", "2"));
    }
}