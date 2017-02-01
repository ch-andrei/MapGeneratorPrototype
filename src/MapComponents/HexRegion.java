package MapComponents;

import NoiseGeneration.NoiseMap;

import java.util.*;

/**
 * Created by Andrei-ch on 2016-11-19.
 */
public class HexRegion implements ViewableRegion{
    private final int REGION_SIZE = 1000000000;

    private int maxElevation, minElevation, averageElevation, waterLevel;

    private Node[][] nodes;

    private boolean running = false;
    private int n, seed, noise_function, hexSize;
    private long timeStart = 0;

    public Random random;

    public HexRegion(int n, int seed, int elevation, float water, int rivers, int noise_function){
        this.random = new Random(seed);
        this.hexSize = getGridSizeForHexagonalGridWithNHexes(n);
        int array_size = 2 * hexSize + 1;
        this.nodes = new Node[array_size][array_size];
        this.n = n;
        this.seed = seed;
        this.noise_function = noise_function;
        generateRegion(elevation, water, rivers);
        System.out.println("Hex region nodes " + nodes.length*nodes.length + " generated.");
    }

    private void generateRegion(int elevation, float water, int lakes){
        generateRadialHexGridNodes(this.n);
        generateHeightMap(elevation, this.noise_function);
        computeElevationParameters();
        generateWater(water);
        generateWaterSourcesAndLakes(lakes);
        System.out.println("Elevations: min = " + this.minElevation + "; max = " + this.maxElevation + "; avg = " + this.averageElevation);
        //generateWaterSourcesAndLakes(lakes);
    }

    private void computeElevationParameters(){
        this.minElevation = this.computeMinimumElevation();
        this.maxElevation = this.computeMaximumElevation();
        this.averageElevation = this.computeAverageElevation();
    }

    private void generateHeightMap(int preferedAverageElevation, int noise_function)
    {
        NoiseMap noiseMap = new NoiseMap(this.seed, noise_function);
        int noiseIndex = noiseMap.getNoiseRes()-1; // must be getNoiseRes-1 due to array out of bounds handling
        float[][] elevations = noiseMap.getElevations(); // (n,k) -> k levels of n octave perlin noise
        for (Node node : this.getViewableNodes())
        {
            int x = (int)((double)noiseIndex/this.REGION_SIZE * node.getX());
            int y = (int)((double)noiseIndex/this.REGION_SIZE * node.getY());
            node.setElevation(preferedAverageElevation * (elevations[x][y]));
        }
    }

    private void generateWater(float waterLevelUnitFactor)
    {
        if (waterLevelUnitFactor > 1) waterLevelUnitFactor = 1;
        if (waterLevelUnitFactor < 0) waterLevelUnitFactor = 0;
        this.waterLevel = (int)(this.minElevation + ((this.maxElevation - this.minElevation) +
                (this.averageElevation - this.minElevation)) / 2 * waterLevelUnitFactor);
        for (Node node : this.getViewableNodes())
        {
            if (node.getZ() < waterLevel){
                node.setWater(true);
            }
        }
    }

    /**
     * creates randomly distributed lakesgubskjb
     * @param numberOfLakes
     */
    private void generateWaterSourcesAndLakes(int numberOfLakes)
    {
        int fails = 0;
        for (int i = 0 ; i < numberOfLakes && fails < numberOfLakes; i++)
        {
            // get random source coordinates
            int x,y;
            x = random.nextInt(this.hexSize);
            y = random.nextInt(this.hexSize);

            Node node = this.nodes[x][y];

            if(node == null || node.getWater()) {
                i--;
                fails++;
                continue;
            }

            Queue<Node> queueConnectedNodes = new LinkedList<Node>();
            queueConnectedNodes.add(node);

            int source_strength = getRandomNumberWithDistribution(random, 10, 1, 10);
            Node deepest;
            int depth;
            do {
                node = queueConnectedNodes.remove();
                node.setWater(true);
                while (node != null) {
                    depth = 0;
                    deepest = null;
                    for (Node connectedNode : node.getConnectedTo())
                    {
                        int delta = (int)(node.getZ() - connectedNode.getZ());
                        if ( !connectedNode.getWater() ){
                            if ( delta > 0 && depth < delta) {
                                deepest = connectedNode;
                                depth = delta;
                            } else if (source_strength > 0){
                                queueConnectedNodes.add(connectedNode);
                            }
                        }
                    }
                    if (deepest != null){
                        deepest.setWater(true);
                        source_strength--;
                    }
                    node = deepest;
                }
            } while (!queueConnectedNodes.isEmpty());
        }
    }

    /**
     * returns a random number with a higher probability of smallest numbers.
     * Increase probabilityPower to decrease output
     * @param random
     * @param max
     * @param min
     * @param probabilityPower
     * @return
     */
    private int getRandomNumberWithDistribution(Random random, int max, int min, double probabilityPower)
    {
        double randomDouble = random.nextDouble();
        double result = Math.floor(min + (max + 1 - min) * (Math.pow(randomDouble, probabilityPower)));
        return (int) result;
    }

    private void generateRadialHexGridNodes(int n)
    {
        int centerX = REGION_SIZE / 2;
        int centerY = REGION_SIZE / 2;
        int gridSize = this.hexSize;

        int counter = 0;
        int hexSize, hexHeight;
        hexSize = centerX / (1 + 2 * gridSize);
        hexHeight = (int)(Math.sqrt(3) / 2 * hexSize);
        // hex cube coordinatess
        for (int X = -gridSize; X <= gridSize; X++){
            for (int Y = -gridSize; Y <= gridSize; Y++){
                int i,j;
                i = X + gridSize;
                j = Y + gridSize;
                if (Math.abs(X+Y) > gridSize) {
                    nodes[i][j] = null;
                    continue;
                }
                int Z = -X - Y;
                int x = centerX + X * 2 * hexHeight;
                int y = centerY + (Y-Z)* hexSize;
                Node nodeToAdd = new Node(x,y);
                nodes[i][j] = nodeToAdd;
                counter++;
            }
        }
        // connect nodes
        for (int i = 0; i < this.nodes.length; i++){
            for (int j = 0; j < this.nodes.length; j++){
                for (int k = 0; k < 6; k++){
                    if (this.nodes[i][j] == null)
                        continue;
                    int ii = i, jj = j;
                    switch(k){
                        case 0:
                           jj++;
                           break;
                        case 1:
                           ii++;
                           break;
                        case 2:
                            ii++;
                            jj--;
                            break;
                        case 3:
                            jj--;
                            break;
                        case 4:
                            ii--;
                            break;
                        case 5:
                            ii--;
                            jj++;
                            break;
                        default:
                            ii = jj = -1;
                            break;
                    }
                    if (ii < 0 || jj < 0 || ii >= this.nodes.length || jj >= this.nodes.length)
                        continue;
                    if (nodes[ii][jj] != null)
                        nodes[i][j].connectWith(nodes[ii][jj]);
                }
            }
        }
        System.out.println("generated grid of size " + gridSize +  " with "  + counter + "/" + n + " nodes");
    }

    private int getGridSizeForHexagonalGridWithNHexes(int n){
        int numberOfHexes = 1;
        int size = 1;
        while (numberOfHexes <= n){
            numberOfHexes += (size++) * 6;
        }
        return size-2;
    }

    private int numberOfHexesForGridSize(int gridSize){
        if (gridSize <= 0) return 1;
        else {
            return 6*gridSize + numberOfHexesForGridSize(gridSize-1);
        }
    }

    public List<Node> getViewableNodes(){
        List<Node> nodesList = new ArrayList<>();
        for (Node[] nodes : this.nodes){
            for (Node n : nodes){
                if (n != null)
                    nodesList.add(n);
            }
        }
        return nodesList;
    }

    public int computeAverageElevation(){
        long sum = 0;
        List<Node> nodes = getViewableNodes();
        for (Node node : nodes){
            sum += node.getZ();
        }
        int out = (int) (sum / (nodes.size() ));
        return out;
    }

    public int computeMaximumElevation(){
        int max = 0;
        List<Node> nodes = getViewableNodes();
        for (Node node : nodes){
            if (max < node.getZ()){
                max = (int)node.getZ();
            }
        }
        return max;
    }

    public int computeMinimumElevation(){
        int min = Integer.MAX_VALUE;
        List<Node> nodes = getViewableNodes();
        for (Node node : nodes){
            if (min > node.getZ()){
                min = (int)node.getZ();
            }
        }
        if (min == Integer.MAX_VALUE) min = -1;
        return min;
    }

    @Override
    public int getMinimumElevation(){
        return this.minElevation;
    }

    @Override
    public int getMaximumElevation(){
        return this.maxElevation;
    }

    @Override
    public int getAverageElevation(){
        return this.averageElevation;
    }

    @Override
    public int getWaterLevel(){
        return this.waterLevel;
    }

    @Override
    public int getViewableSize(){
        return this.REGION_SIZE;
    }

    @Override
    public long getViewableSeed(){
        return this.seed;
    }

}
