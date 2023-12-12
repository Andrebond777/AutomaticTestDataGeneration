import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static java.lang.System.exit;

public class Main {
    static class Algorithms
    {
        static class Test
        {
            String ns;
            String ps;
            boolean[] coverageTargets;
            short fitness;

            public void setNs(String ns)
            {
                nsInitializer(ns);
            }

            private void nsInitializer(String ns)
            {
                //performs check whether String ns is integer and positive
                try
                {
                    if(Integer.parseInt(ns) < 0)
                        ns = String.valueOf(Math.abs(Integer.valueOf(ns)));
                    else if(ns.charAt(0) == '-')
                        ns = generateRandomNs();
                    this.ns = ns;
                } catch (NumberFormatException e) {
                    this.ns = generateRandomNs();
                }
            }

            public void setPs(String ps)
            {
                psInitializer(ps);
            }

            private void psInitializer(String ps)
            {
                //performs check whether String ps is integer, positive and prime
                try{
                    if(Integer.parseInt(ps) < 0)
                        ps = String.valueOf(Math.abs(Integer.valueOf(ps)));
                    else if (!isPrime(new BigInteger(ps)))
                        ps = generateRandomPrime().toString();
                    this.ps = ps;
                } catch (NumberFormatException e) {
                    this.ps = generateRandomPrime().toString();
                }
            }

            public Test(String ns, String ps)
            {
                nsInitializer(ns);
                psInitializer(ps);

                this.coverageTargets = new boolean[targetsQuantity];
                fitness = 0;
            }
        }

        //population
        public static List<Test> testDataPool;
        public static List<Test> resultTests;
        //all targets in the program
        private static int targetsQuantity;
        //population size
        private static int poolSize;
        private static double crossoverRate;
        private static double mutationRate;
        private static short bestFitness;
        //array of covered and uncovered targets
        private static boolean[] totalTargetsCoverage;
        //endPointTargets: true - target where program terminates, false - all other targets
        private static boolean[] endPointTargets;
        //Index of the target which seeks the algorithm
        private static int currentTargetID;
        //Top limit for generating random number for ns, bottom limit is always 0
        private static int nsRandomLimit;
        //Top limit for generating random number for ps, bottom limit is always 2
        private static BigInteger psRandomLimit;

        public Algorithms(int poolSize, int nsRandomLim, String psRandomLim)
        {
            if(poolSize < 4)
            {
                System.out.println("Error! Pool size must be not less than 4!");
                exit(0);
            }
            this.poolSize = poolSize;
            crossoverRate = 0.66;
            mutationRate = 0.07;
            bestFitness = 0;
            targetsQuantity = 8;
            resultTests = new ArrayList<>();
            totalTargetsCoverage = new boolean[targetsQuantity];
            endPointTargets = new boolean[targetsQuantity];
            //Terminating(end point) targets assigned true values
            endPointTargets[0] = true;
            endPointTargets[1] = true;
            endPointTargets[5] = true;
            endPointTargets[6] = true;
            endPointTargets[7] = true;
            nsRandomLimit = nsRandomLim;
            psRandomLimit = new BigInteger(psRandomLim);
        }

        public static long[] runAlgorithm(String algorithmName, int nsRandomLim, String psRandomLim)
        {
            int populationSize = 7;
            Algorithms algorithm = new Algorithms(populationSize, nsRandomLim, psRandomLim);
            long j = 0;
            long start = System.currentTimeMillis();
            for(int i = 0; i < targetsQuantity; i++)
            {
                if(totalTargetsCoverage[i])
                    continue;
                bestFitness = 0;
                currentTargetID = i;
                if (algorithmName.equalsIgnoreCase("GeneticAlgorithm"))
                    initialiseTestDataPool();

                while (!algorithm.stopCondition())
                {
                    if (algorithmName.equalsIgnoreCase("GeneticAlgorithm"))
                    {
                        var selectedPopulation = algorithm.selection();
                        algorithm.reproduction(selectedPopulation);
                        algorithm.mutation();
                    }
                    else if (algorithmName.equalsIgnoreCase("RandomAlgorithm"))
                    {
                        fitnessFunc(generateRandomTest());
                    } else
                    {
                        System.out.println("Algorithm name not found.");
                        return new long[0];
                    }
                    j++;
                }
            }
            long end = System.currentTimeMillis();
            //print(resultTests);
            System.out.println("AllTargetsCovered = " + allTargetsCovered());
            return new long[]{j, end-start};
        }

        private static boolean allTargetsCovered()
        {
            for(int i = 0; i < targetsQuantity; i++)
                if(totalTargetsCoverage[i] == false)
                    return false;
            return true;
        }

        public static boolean isPrime(BigInteger number)
        {
            var counter = number.subtract(new BigInteger("1"));
            BigInteger i = new BigInteger("2");
            while (i.compareTo(counter) < 0)
            {
                if (number.mod(i).compareTo(BigInteger.valueOf(0)) == 0)
                {
                    return false;
                }
                i = i.add(new BigInteger("1"));
            }
            return true;
        }

        public static BigInteger generateRandomPrime()
        {
            for(int i = 0; i < 1000; i++) {
                BigInteger maxLimit = psRandomLimit;
                BigInteger minLimit = new BigInteger("2");
                BigInteger bigInteger = maxLimit.subtract(minLimit);
                Random randNum = new Random();
                int len = maxLimit.bitLength();
                BigInteger res = new BigInteger(len, randNum);
                if (res.compareTo(minLimit) < 0)
                    res = res.add(minLimit);
                if (res.compareTo(bigInteger) >= 0)
                    res = res.mod(bigInteger).add(minLimit);
                if (isPrime(res))
                    return res;
            }
            return new BigInteger("2");
        }

        public static BigInteger generateRandomPrime(BigInteger minLimit, BigInteger maxLimit)
        {
            for(int i = 0; i < 1000; i++) {
                BigInteger bigInteger = maxLimit.subtract(minLimit);
                Random randNum = new Random();
                int len = maxLimit.bitLength();
                BigInteger res = new BigInteger(len, randNum);
                if(res.compareTo(new BigInteger("1")) < 0 || bigInteger.compareTo(new BigInteger("1")) < 0 )
                    break;
                if (res.compareTo(minLimit) < 0)
                    res = res.add(minLimit);
                if (res.compareTo(bigInteger) >= 0)
                    res = res.mod(bigInteger).add(minLimit);
                if (isPrime(res))
                    return res;
            }
            return new BigInteger("2");
        }

        public static String generateRandomNs()
        {
            return String.valueOf((int)(nsRandomLimit * Math.random()));
        }

        public static Test generateRandomTest()
        {
            String ns = generateRandomNs();
            String ps = String.valueOf(generateRandomPrime());
            return new Test(ns, ps);
        }

        private static void initialiseTestDataPool()
        {
            testDataPool = new ArrayList<>();
            for(int i = 0; i < poolSize; i++)
            {
                testDataPool.add(generateRandomTest());
            }
        }

        private static void printTest(Test test)
        {
            System.out.println("ns = " + test.ns);
            System.out.println("ps = " + test.ps);
            System.out.print("Targets coverage:");
            for (int i = 0; i < targetsQuantity; i++)
                System.out.print(" " + test.coverageTargets[i]);
            System.out.println("");
        }

        public static void print(List<Test> data)
        {
            int testNumber = 0;
            for (var s:
                    data)
            {
                printTest(s);
                testNumber++;
            }
        }

        private static short fitnessFunc(Test test)
        {
            test.coverageTargets = CipollasAlgorithm.c(test.ns, test.ps).coverageTargets;

            //if target has been reached
            if(test.coverageTargets[currentTargetID])
            {
                resultTests.add(test);
                for(int i = 0; i < targetsQuantity; i++)
                    if(test.coverageTargets[i])
                        totalTargetsCoverage[i] = true;
                bestFitness = -1;
                return bestFitness;
            }

            //fitness is a sum of all targets before the one we need to reach
            //if target reaches endpoint we add 1 to fitness, if not we add 10
            short fitness = 0;
            for (int i = 0; i < currentTargetID; i++)
            {
                if(test.coverageTargets[i])
                {
                    if (endPointTargets[i])
                        fitness += 1;
                    else
                        fitness += 10;
                }
            }
            return fitness;
        }

        private static void updatePoolFitness()
        {
            for (var s:
                    testDataPool)
            {
                s.fitness = fitnessFunc(s);
            }
        }

        public static Integer TournamentSelectRandomMax(Integer id)
        {
            List<Integer> selectedID = new ArrayList<>();
            for (int i = 0; i < poolSize; i++)
            {
                int index = (int)((poolSize-1) * Math.random());
                int j = 0;
                while(selectedID.contains(index) && j < 100)
                {
                    index = (int) ((poolSize - 1) * Math.random());
                    j++;
                }
                selectedID.add(index);
            }

            short maxFitness;
            Integer maxIndex;
            if(id == 0)
            {
                maxFitness = testDataPool.get(selectedID.get(1)).fitness;
                maxIndex = 1;
            }
            else
            {
                maxFitness = testDataPool.get(selectedID.get(0)).fitness;
                maxIndex = 0;
            }
            for (int i = 0; i < selectedID.size(); i++)
            {
                if(selectedID.get(i) == id)
                    continue;
                if (testDataPool.get(selectedID.get(i)).fitness > maxFitness)
                {
                    maxIndex = i;
                    maxFitness = testDataPool.get(selectedID.get(i)).fitness;
                }
            }
            return selectedID.get(maxIndex);
        }

        public  static List<Integer> selection2()
        {
            Test max1 = testDataPool.get(0);
            for (int i = 1; i < poolSize / 2; i++)
            {
                if(max1.fitness < testDataPool.get(i).fitness)
                    max1 = testDataPool.get(i);
            }
            Test max2 = new Test("0", "0");
            for (int i = 0; i < poolSize; i++)
            {
                var tmp = testDataPool.get(i);
                if(!tmp.equals(max1) && max2.fitness <= tmp.fitness)
                    max2 = testDataPool.get(i);
            }

            var result = new ArrayList<Integer>();
            result.add(Integer.valueOf(testDataPool.indexOf(max1)));
            result.add(Integer.valueOf(testDataPool.indexOf(max2)));
            return result;
        }

        public List<Integer> selection()
        {
            updatePoolFitness();
            List<Integer> parentsID = new ArrayList<>();
            parentsID.add(TournamentSelectRandomMax(-1));
            parentsID.add(TournamentSelectRandomMax(parentsID.get(0)));
            int i = 0;
            while (parentsID.get(0).equals(parentsID.get(1)) && i < 10)
            {
                parentsID.set(0, TournamentSelectRandomMax(parentsID.get(0)));
                i++;
            }

            return parentsID;
        }

        private String[] nsReproduction(List<Integer> parentsID)
        {
            StringBuilder ns0 = new StringBuilder(testDataPool.get(parentsID.get(0)).ns);
            StringBuilder ns1 = new StringBuilder(testDataPool.get(parentsID.get(1)).ns);

            int end0 = (int)Math.floor(Math.random() * ns0.length());
            int start0 = (int)Math.floor(Math.random() * end0);
            int end1 = (int)Math.floor(Math.random() * ns1.length());
            int start1 = (int)Math.floor(Math.random() * end1);

            int j = start1;
            for (int i = start0; i < end0; i++)
            {
                if(j >= end1)
                    break;
                ns0.setCharAt(i, testDataPool.get(parentsID.get(1)).ns.charAt(j));
                ns1.setCharAt(j, testDataPool.get(parentsID.get(0)).ns.charAt(i));
            }

            return new String[]{String.valueOf(ns0), String.valueOf(ns1)};
        }

        private String[] psReproduction(List<Integer> parentsID)
        {
            String ps0 = testDataPool.get(parentsID.get(0)).ps;
            String ps1 = testDataPool.get(parentsID.get(1)).ps;
            var ps0BigInt = new BigInteger(ps0);
            var ps1BigInt = new BigInteger(ps1);
            if(ps0BigInt.compareTo(ps1BigInt) >= 1)
            {
                var tmpBigInt = ps0BigInt;
                ps0BigInt = ps1BigInt;
                ps1BigInt = tmpBigInt;
                var tmp = ps0;
                ps0 = ps1;
                ps1 = tmp;
            }

            BigInteger coefficient2 = new BigInteger("2");
            if(ps0BigInt.subtract(ps0BigInt.divide(coefficient2)).compareTo(new BigInteger("1")) > 0)
                ps0BigInt = ps0BigInt.subtract(ps0BigInt.divide(coefficient2));

            BigInteger randomCoefficient = BigInteger.valueOf((long) Math.floor(Math.random() * 100) + 1);
            if(ps1BigInt.compareTo(new BigInteger("10")) < 0)
                ps1BigInt = ps1BigInt.multiply(randomCoefficient);
            ps1BigInt = ps1BigInt.add(ps0BigInt.multiply(coefficient2));

            ps0 = generateRandomPrime(ps0BigInt, ps1BigInt).toString();
            ps1 = generateRandomPrime(ps0BigInt, ps1BigInt).toString();

            int i = 0;
            while (ps0.compareTo(ps1) == 0 && i < 100)
            {
                if(ps0BigInt.subtract(BigInteger.valueOf(i)).compareTo(BigInteger.valueOf(0)) > 1)
                    ps0BigInt = ps0BigInt.subtract(BigInteger.valueOf(i));
                ps1BigInt = ps1BigInt.add(BigInteger.valueOf(i));

                ps0 = generateRandomPrime(ps0BigInt, ps1BigInt).toString();
                ps1 = generateRandomPrime(ps0BigInt, ps1BigInt).toString();
                i++;
            }

            return new String[]{ps0, ps1};
        }

        public void reproduction(List<Integer> parentsID)
        {
            double crossoverProbability = Math.random();
            if(crossoverProbability > crossoverRate)
                return;

            String[] nsArr = nsReproduction(parentsID);
            String[] psArr = psReproduction(parentsID);

            Test test0 = new Test(nsArr[0], psArr[0]);
            Test test1 = new Test(nsArr[1], psArr[1]);
            testDataPool.set(parentsID.get(0), test0);
            testDataPool.set(parentsID.get(1), test1);
        }

        private String mutateNs(String testNs)
        {
            int randomNsPos;
            int nsLength = testNs.length();
            StringBuilder ns = new StringBuilder(testNs);
            for (int i = 0; i < nsLength / 3; i++)
            {
                randomNsPos = (int)Math.floor(Math.random() * (nsLength - 1));
                String randomDigit = String.valueOf(((int)Math.floor(Math.random() * 9)));
                ns.setCharAt(randomNsPos, randomDigit.charAt(0));
            }

            return String.valueOf(ns);
        }

        private String mutatePs(String testPs)
        {
            int ps = Integer.valueOf(testPs);
            if(ps / 2 > 1)
                ps /= 2;
            var minPs = new BigInteger(String.valueOf((ps)));
            var maxPs = new BigInteger(String.valueOf((ps * 2)));

            return generateRandomPrime(minPs, maxPs).toString();
        }

        public void mutation()
        {
            double mutationProbability = Math.random();
            if(mutationProbability > mutationRate)
                return;

            int testID = (int)(poolSize * Math.random());
            Test test = testDataPool.get(testID);

            test.setNs(mutateNs(test.ns));
            test.setPs(mutatePs(test.ps));

            testDataPool.set(testID, test);
        }

        private boolean stopCondition()
        {
            if(bestFitness == -1)
                return true;
            else
                return false;
        }
    }

    public static void main(String[] args)
    {
        int nsRandomLimit = 2147483640;
        String psRandomLimit = "250000";
        nsRandomLimit = 500000;
        psRandomLimit = String.valueOf(500);
        long[] GAx = Algorithms.runAlgorithm("GeneticAlgorithm", nsRandomLimit, psRandomLimit);
        System.out.println("TotalIterations: " + GAx[0]);
        System.out.println("TotalTime: " + GAx[1]);
        long[] RAx = Algorithms.runAlgorithm("RandomAlgorithm", nsRandomLimit, psRandomLimit);
        System.out.println("TotalIterations: " + RAx[0]);
        System.out.println("TotalTime: " + RAx[1]);
/*
        StringBuilder result = new StringBuilder();
        result.append("Size limit of numbers,Mean iterations GA,Mean iterations RA,Elapsed Time ms GA,Elapsed Time ms RA");
        int pointsQuantity = 7;
        for (int p = 0; p < pointsQuantity; p++)
        {
            long[] GA = new long[2];
            long totalIterationsGA = 0;
            long totalTimeElapsedGA = 0;
            long[] RA = new long[2];
            long totalIterationsRA = 0;
            long totalTimeElapsedRA = 0;
            int limit = 0;
            if(p < 4)
                limit = (int) (Math.pow(8, p + 2));
            else if(p < 6)
                limit = (int) (Math.pow(5, p + 2));
            else if(p == 6)
                limit = (int) (Math.pow(8, p)) / 2;
            int iterationsQuantity = 100 / (p + 1);
            for (int i = 0; i < iterationsQuantity; i++)
            {
                GA = Algorithms.runAlgorithm("GeneticAlgorithm", limit, String.valueOf(limit));
                RA = Algorithms.runAlgorithm("RandomAlgorithm", limit, String.valueOf(limit));
                totalIterationsGA += GA[0];
                totalTimeElapsedGA += GA[1];
                totalIterationsRA += RA[0];
                totalTimeElapsedRA += RA[1];
            }

            totalIterationsGA /= iterationsQuantity;
            totalIterationsRA /= iterationsQuantity;
            totalTimeElapsedGA /= iterationsQuantity;
            totalTimeElapsedRA /= iterationsQuantity;

            System.out.println("Limit size: " + limit);
            System.out.println("Mean iterations GA: " + totalIterationsGA);
            System.out.println("Mean iterations RA: " + totalIterationsRA);
            System.out.println("Elapsed Time ms GA: " + totalTimeElapsedGA);
            System.out.println("Elapsed Time ms RA: " + totalTimeElapsedRA);

            result.append("\n" + limit + "," + totalIterationsGA + "," + totalIterationsRA +
                    "," + totalTimeElapsedGA + "," + totalTimeElapsedRA);
        }
        try {
            String fileName = "GA_RA_Comparison.csv";
            for(int i = 0; i < 1000; i++)
            {
                fileName = "GA_RA_Comparison" + i + ".csv";
                File f = new File(fileName);
                if (!f.exists())
                    break;
            }

            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(result.toString());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }*/
    }
}