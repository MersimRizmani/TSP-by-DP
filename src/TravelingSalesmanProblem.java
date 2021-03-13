import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TravelingSalesmanProblem {
    public static int numCities;
    public static Bookkeeping optimalTour;
    public static double[][] distMatrix;
    public static ArrayList<Integer> setOfCities;
    public static ArrayList<ArrayList<Integer>> powerSetCities;

    /*
    * this map will represent C(S, i) where S is an ArrayList<Integer>, i is an Integer and the cost is a field in the Bookkeeping class
    * Ex: costMap.get([1]).get(1).cost = 0, C({1}, 1) = 0
    */
    public static Map<ArrayList<Integer>, Map<Integer, Bookkeeping>> costMap = new HashMap<>();

    public static void main(String args[]){
        File inFile = null;
        if (0 < args.length) {
            inFile = new File(args[0]);
        } else {
            System.err.println("Invalid arguments count:" + args.length);
            System.exit(0);
        }

        BufferedReader br = null;

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(inFile));

            /* read in the number of cities first */
            if((sCurrentLine = br.readLine()) != null)
                numCities = Integer.parseInt(sCurrentLine);

            /* create distance matrix of size N x N */
            distMatrix = new double[numCities][numCities];

            Scanner sc = new Scanner(br);

            /* populate distance matrix with values */
            while (sc.hasNextLine()) {
                for (int i=0; i<distMatrix.length; i++) {
                    String[] line = sc.nextLine().trim().split(" ");
                    for (int j=0; j<line.length; j++) {
                        distMatrix[i][j] = Integer.parseInt(line[j]);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* retrieve the power set of the set of cities */
        setOfCities = (ArrayList<Integer>) IntStream.rangeClosed(0, numCities-1).boxed().collect(Collectors.toList());
        long powerSetSize = (long) Math.pow(2, numCities);
        powerSetCities = getPowerSet(setOfCities, numCities, powerSetSize);

        /* find the length of the optimal tour */
        optimalTour = solve(numCities, distMatrix, powerSetCities, costMap);

        System.out.println(costMap.get(new ArrayList<>(List.of(0,1,2))).get(0).cost);
    }

    public static ArrayList<ArrayList<Integer>> getPowerSet(ArrayList<Integer> cities, int numCities, long powerSet_size){
        ArrayList<ArrayList<Integer>> powerSet = new ArrayList<>();
        ArrayList<Integer> intList;
        for(int i = 0; i < powerSet_size; i++) {
            intList = new ArrayList<>();
            for(int j = 0; j < numCities; j++) {
                if((i & (1 << j)) > 0)
                    intList.add(cities.get(j));
            }
            powerSet.add(intList);
        }

        powerSet.remove(0);
        Comparator<ArrayList<Integer>> setCompare = Comparator.comparing((Function<ArrayList<Integer>, Integer>) ArrayList::size).thenComparing(a -> a.get(0));
        powerSet.sort(setCompare);
        powerSet.add(0, new ArrayList<>());

        return powerSet;
    }

    public static Bookkeeping solve(int n, double[][] dist, ArrayList<ArrayList<Integer>> powerSetCities, Map<ArrayList<Integer>, Map<Integer, Bookkeeping>> cost){

        /* When |S| > 1, C(S, 0) = INFINITY since the path cannot both start and end at 0 */
        for(ArrayList<Integer> s : powerSetCities){
            if(s.size() > 1){
                cost.put(s, Map.of(0, new Bookkeeping(Double.POSITIVE_INFINITY, new ArrayList<>())));
            }
        }

        /* C({0}, 0) = 0 */
        cost.put(new ArrayList<>(List.of(0)), Map.of(0, new Bookkeeping(0, new ArrayList<>(List.of(0)))));

        /* Pseudocode from "Solving General TSP Exactly via Dynamic Programming" */
        for(int s = 2; s < n; s++){
            for(ArrayList<Integer> subset : powerSetCities){
                if((subset.size() == s) && (subset.contains(0))){
                    // rest of code
                }
            }
        }

        return null;
    }
}

/* this is the value that will be stored for each pair of keys in the map. it keeps track of the cost and optTour at that particular index */
class Bookkeeping {
    double cost;
    ArrayList<Integer> optTour;

    Bookkeeping(double c, ArrayList<Integer> opt){
        cost = c;
        optTour = opt;
    }
}
