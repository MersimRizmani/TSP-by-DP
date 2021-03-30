import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TravelingSalesmanProblem {
    public static int numCities; // integer value denoting the number of cities
    public static Bookkeeping optimalTour; // this will be the Bookkeeping object with the minimum cost of visiting all cities starting at 0
    public static double[][] distMatrix; // 2D array containing the distances between the cities
    public static ArrayList<Integer> setOfCities; // the set of cities from 0 to n
    public static ArrayList<ArrayList<Integer>> powerSetCities; // the power set of the set of cities from 0 to n

    /*
    * this map will represent C(S, i) where S is an ArrayList<Integer>, i is an Integer and the cost is a field in the Bookkeeping class
    * Ex: costMap.get([1]).get(1).cost = 0, C({1}, 1) = 0
    */
    public static Map<ArrayList<Integer>, Map<Integer, Bookkeeping>> costMap = new HashMap<>();

    public static void main(String[] args){

        /* read in input data and set variables */
        readData(args);

        /* generate the set of cities */
        setOfCities = (ArrayList<Integer>) IntStream.rangeClosed(0, numCities-1).boxed().collect(Collectors.toList());

        /* retrieve the power set of the set of cities */
        long powerSetSize = (long) Math.pow(2, numCities);
        powerSetCities = getPowerSet(setOfCities, numCities, powerSetSize);

        /* initialize the values in the cost map to their appropriate values */
        initializeCostMap();

        /* find the cost of the optimal tour starting at city 0 */
        optimalTour = solve(numCities, powerSetCities);

        /* print the number of cities */
        System.out.println("NUMBER OF CITIES: " + numCities + "\n");

        /* print the distance matrix */
        System.out.println("DISTANCE MATRIX : ");
        printMatrix();

        /* print the power set of cities that start at city 0 */
        System.out.println("POWER SET OF CITIES (starting at City 0) : ");
        printPowerSet();

        /* print the dynamic programming "table" */
        System.out.println("DYNAMIC PROGRAMMING 'TABLE' : ");
        printMap();

        /* print the cost of the optimal tour */
        System.out.println("COST OF OPTIMAL TOUR (starting at City 0): " + optimalTour.cost);

        /* find the cities visiting on the optimal tour and print */
        System.out.println("OPTIMAL TOUR (starting at City 0): " + "\n");
    }

    public static void readData(String[] args){

        /* retrieve input file from command line */
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
                        distMatrix[i][j] = Double.parseDouble(line[j]);
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
    }

    /* this function returns the power set of the set of cities from 0 to n */
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

        /* remove subsets that don't contain 0 as we don't need them */
        powerSet.removeIf(subset -> !subset.contains(0));

        return powerSet;
    }

    /* this function initializes the values in the cost map to their appropriate values */
    public static void initializeCostMap(){

        /* populate all entries in the map with default values */
        for(ArrayList<Integer> set : powerSetCities){
            costMap.put(set, new HashMap<>());

            for(Integer i : setOfCities){
                costMap.get(set).put(i, new Bookkeeping(Double.POSITIVE_INFINITY, 0));
            }
        }
    }

    /* this function returns the Bookkeeping object with the minimum cost of visiting ALL cities starting at 0 */
    public static Bookkeeping findMinimumCost(){
        Bookkeeping minimum = new Bookkeeping(Double.POSITIVE_INFINITY, 0);

        for(int j = 0; j < numCities; j++){
            Bookkeeping candidateMin = costMap.get(setOfCities).get(j);
            double costOfCandidateMin = candidateMin.cost + distMatrix[j][0];

            if(costOfCandidateMin <= minimum.cost){
                minimum.cost = costOfCandidateMin;
            }
        }

        return minimum;
    }

    /* function that will implement the dynamic programming algorithm to solve the tsp */
    public static Bookkeeping solve(int n, ArrayList<ArrayList<Integer>> powerSetCities){

        /* C({0}, 0) = 0 */
        costMap.get(new ArrayList<>(List.of(0))).put(0, new Bookkeeping(0, 0));

        /* Pseudocode from "Solving General TSP Exactly via Dynamic Programming" */
        for(int s = 2; s <= n; s++){
            for(ArrayList<Integer> subset : powerSetCities){ // optimize to not go through whole power set but rather just skip to correct size
                if(subset.size() == s){

                    /* When |S| > 1, C(S, 0) = INFINITY since the path cannot both start and end at 0 */
                    costMap.get(subset).put(0, new Bookkeeping(Double.POSITIVE_INFINITY, 0));

                    /*
                        for all j IN subset, j != 0
                             C(S, j) = min[ C(S - {j}, i), i IN S, i != j
                    */
                    for(Integer j : subset){
                        if(j != 0){
                            Bookkeeping insertion = findMinimum(j, subset);
                            costMap.get(subset).put(j, insertion);
                        }
                    }
                }
            }
        }

        return findMinimumCost();
    }

    /* helper function for the algorithm that finds the Bookkeeping object minimum cost of visiting
    all cities in subset s starting at 0 and ending at j */
    public static Bookkeeping findMinimum(int j, ArrayList<Integer> s){

        /* initialize a default minimum */
        Bookkeeping minimum = new Bookkeeping(Double.POSITIVE_INFINITY, 0);

        /* S - {j} */
        s = (ArrayList<Integer>) s.stream().filter(e -> e != j).collect(Collectors.toList());

        for(Integer i : s){
            if(i != j){
                Bookkeeping candidateMin = costMap.get(s).get(i);
                double costOfCandidateMin = candidateMin.cost + distMatrix[i][j];

                if(costOfCandidateMin <= minimum.cost){
                    minimum.cost = costOfCandidateMin;
                }
            }
        }

        return minimum;
    }

    /* helper function to print distance matrix */
    public static void printMatrix(){
        for(int i = 0; i < numCities; i++){
            for(int j = 0; j < numCities; j++){
                System.out.print(distMatrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /* helper function used to print the dynamic programming table */
    public static void printMap(){
        System.out.print("  ");

        for(int i = 0; i < powerSetCities.size(); i++){
            if(i < 10){
                System.out.print("  " + i + "  ");
            }
            else{
                System.out.print(" " + i + "  ");
            }
        }

        System.out.println();

        for(Integer i : setOfCities){
            System.out.print(i + " ");
            for(ArrayList<Integer> subset : powerSetCities){
                if(costMap.get(subset).get(i).cost == Double.POSITIVE_INFINITY){
                    System.out.print("Infi" + " ");
                }
                else{
                    System.out.print(costMap.get(subset).get(i).cost + "  ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    /* helper function to print power set */
    public static void printPowerSet(){
        System.out.print(" ");
        for(int i = 0; i < powerSetCities.size(); i++){
            if(powerSetCities.get(i).size() < 2){
                System.out.print(i + "    ");
            }
            else if(powerSetCities.get(i).size() == 2){
                System.out.print(" " + i + "    ");
            }
            else if(powerSetCities.get(i).size() == 3){
                System.out.print("      " + i + "   ");
            }
            else{
                System.out.print("       " + i + "    ");
            }
        }

        System.out.println();

        for(ArrayList<Integer> key : powerSetCities){
            System.out.print(key + " ");
        }

        System.out.println("\n");
    }
}

/* this is the value that will be stored for each pair of keys in the map. it keeps track of the cost and optPred at that particular index */
class Bookkeeping {
    double cost; // cost of tour starting at city 0 and ending at city j
    int optPred; // preceding city in the optimal tour

    Bookkeeping(double c, int opt){
        cost = c;
        optPred = opt;
    }
}
