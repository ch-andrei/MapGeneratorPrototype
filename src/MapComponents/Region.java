package MapComponents;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import NoiseGeneration.Noise;
import NoiseGeneration.NoiseMap;
import VoronoiGenerator.VoronoiAdapter;
import com.sun.org.apache.regexp.internal.RE;

/**
 * 
 * @author Andrei C
 *
 */
public class Region implements Runnable, ViewableRegion{
	private final int REGION_SIZE = 1000000000, MIN_VORONOI_DISTANCE = 1;

	private int maxElevation, minElevation, averageElevation, waterLevel;

	private List<Node> nodes;
	private List<Node> voronoiNodes;
	private boolean running = false;
	private int n, seed, noise_function;
	private long timeStart = 0;

	private Object lock = new Object();

	public Random random;

	/**
	 * generates a random region
	 * @param n number of nodes in region
	 * @param seed random seed of the region
	 */
	public Region(int n, int seed, int elevation, double water, int rivers, int noise_function){
		this.random = new Random(seed);
		this.nodes = new ArrayList<Node>();
		this.voronoiNodes = new ArrayList<Node>();
		this.n = n;
		this.seed = seed;
		this.noise_function = noise_function;
		generateRegion(n, seed, 2*elevation, water, rivers);
		System.out.println("region nodes " + nodes.size() + " voro nodes " + voronoiNodes.size());
	}

	public Region(int n, int seed, int elevation, double water, int rivers, int noise_function, boolean test){
		this.random = new Random(seed);
		this.nodes = new ArrayList<Node>();
		this.voronoiNodes = new ArrayList<Node>();
		this.n = n;
		this.seed = seed;
		this.noise_function = noise_function;
		voronoiNodes.add(new Node(10,10));
		NodeUtilities.binaryInsert(voronoiNodes, new Node(10,10));
		System.out.println("TESTING: region nodes " + nodes.size() + " voro nodes " + voronoiNodes.size());
	}

	@Override
	public void run()
	{
		timeStart = System.currentTimeMillis();
//				running = true;
		while (true) {
			while(running)
			{
				System.out.println("generating");
				Random rand = new Random();
				long time1 = System.currentTimeMillis();

//								generateWaterSourcesAndLakes(10);
//								generateWaterSourcesAndRivers(5);
//				generateRegion(n, rand.nextInt(50000000), 1000, 0.7,0 );
				computeElevationParameters();

				long time2 = System.currentTimeMillis();
				System.out.println("height: avg = " + this.averageElevation + " min = "
						+ this.minElevation + " max = " + this.maxElevation);
				System.out.println("gen reg time " + (time2-time1) 
						+  ", sizes " + nodes.size() + " " + voronoiNodes.size());
				System.out.println("runtime so far " + (time2 - timeStart));

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void generateRegion(int n, int seed, int elevation, double water, int rivers)
	{
		long t1 = System.currentTimeMillis();
        //generateRadialHexGridNodes(n);
        generateNodes(n);
        //generateFakeHexGridNodes(n);
        //generateSquareGridNodes(n);

		long t2 = System.currentTimeMillis();
		NodeUtilities.mergeAndRemoveDuplicateNodes(this.nodes);
		long t3 = System.currentTimeMillis();
		//NodeUtilities.connectClosestNodes(this.nodes);
		generateVoronoi(MIN_VORONOI_DISTANCE);
		long t4 = System.currentTimeMillis();
		NodeUtilities.mergeAndRemoveDuplicateNodes(this.voronoiNodes);
		long t5 = System.currentTimeMillis();
		generateHeightMap(elevation, this.noise_function);
		long t6 = System.currentTimeMillis();
		computeElevationParameters();
		long t7 = System.currentTimeMillis();
		generateWater(water);
		long t8 = System.currentTimeMillis();
		// TODO
		generateWaterSourcesAndLakes(rivers);
		long t9 = System.currentTimeMillis();
		System.out.println("region pars: " + "max " + getMaximumElevation() +
				", min " + getMinimumElevation() + ", avg " + getAverageElevation() +
				", water " + getWaterLevel());
		System.out.println("gen nodes " + (t2-t1));
		System.out.println("merge remove dup " + (t3-t2));
		System.out.println("gen voro " + (t4-t3));
		System.out.println("merge remove dup voro " + (t5-t4));
		System.out.println("gen height " + (t6-t5));
		System.out.println("compue elevation " + (t7-t6));
		System.out.println("gen water " + (t8-t7));
		System.out.println("water sources and lakes " + (t9-t8));
	}

	/**
	 * functionality:
	 * generates a random list of nodes of size n.
	 * operation:
	 * runs n iterations. at each iteration, a new node with random x,y coordinates is generated 
	 * and inserted in the common list of points at its correct SORTED index.
	 * order of insertion is based on the lexicographic ordering on x,y coordinates.
	 * 
	 * @param n number of nodes to generate
	 */
	public void generateNodes(int n)
	{
		for (int i = 0; i < n; i++)
		{
			int x = random.nextInt(REGION_SIZE),
					y = random.nextInt(REGION_SIZE);

			Node nodeToAdd = new Node(x,y);
			NodeUtilities.binaryInsert(nodes, nodeToAdd);
		}
	}

    public void generateSquareGridNodes(int n)
    {
        int side = (int)Math.sqrt(n);
        int increment = REGION_SIZE / side;
        for (int i = 0; i < n; i++)
        {
            int ii = (i / side);
            int jj = (i % side);
            int x = ii * increment;
            int y = jj * increment;
            x = (x > REGION_SIZE) ? REGION_SIZE : x;
            y = (y > REGION_SIZE) ? REGION_SIZE : y;
            Node nodeToAdd = new Node(x,y);
            NodeUtilities.binaryInsert(nodes, nodeToAdd);
        }
    }

    public void generateFakeHexGridNodes(int n)
    {
        int side = (int)Math.sqrt(n);
        int increment = REGION_SIZE / side;
        for (int i = 0; i < n; i++)
        {
            int ii = (i / side);
            int jj = (i % side);
            int x = ii * increment;
            int y = jj * increment;
            if (ii % 2 == 0) // fake hex offset
                y += increment / 2;
            x = (x > REGION_SIZE) ? REGION_SIZE : x;
            y = (y > REGION_SIZE) ? REGION_SIZE : y;
            Node nodeToAdd = new Node(x,y);
            NodeUtilities.binaryInsert(nodes, nodeToAdd);
        }
    }

    public void generateRadialHexGridNodes(int n)
    {
		int centerX = REGION_SIZE / 2;
        int centerY = REGION_SIZE / 2;
        int gridSize = getGridSizeForHexagonalGridWithNHexes(n); // radius of hex grid

        int counter = 0;
        int hexSize, hexHeight;
        hexSize = centerX / (1 + 2 * gridSize);
        hexHeight = (int)(Math.sqrt(3) / 2 * hexSize);
        // hex cube coordinatess
        for (int X = -gridSize; X <= gridSize; X++){
            for (int Y = -gridSize; Y <= gridSize; Y++){
                if (Math.abs(X+Y) > gridSize) continue;
                int Z = -X - Y;
                int x = centerX + X * 2 * hexSize;
                int y = centerY + (Y-Z)* hexHeight;
                Node nodeToAdd = new Node(x,y);
                NodeUtilities.binaryInsert(nodes, nodeToAdd);
                counter++;
            }
        }
        System.out.println("generated grid of size " + gridSize +  " with "  + counter + "/" + n + " nodes");
    }

    private int numberOfHexesForGridSize(int gridSize){
        if (gridSize <= 0) return 1;
        else {
            return 6*gridSize + numberOfHexesForGridSize(gridSize-1);
        }
    }

    private int getGridSizeForHexagonalGridWithNHexes(int n){
        int numberOfHexes = 1;
        int size = 1;
        while (numberOfHexes <= n){
            numberOfHexes += (size++) * 6;
        }
        return size-2;
    }

	public void generateVoronoi(int minDistance)
	{
		VoronoiAdapter voronoi = new VoronoiAdapter(minDistance);
		this.voronoiNodes = new ArrayList<Node>();
		voronoi.generateVoronoiNodesToArray(this, REGION_SIZE, voronoiNodes);
	}

	public void generateHeightMap(int preferedAverageElevation, int noise_function)
	{
		NoiseMap noiseMap = new NoiseMap(this.seed, noise_function);
		int noiseIndex = noiseMap.getNoiseRes()-1; // must be getNoiseRes-1 due to array out of bounds handling
		float[][] elevations = noiseMap.getElevations(); // (n,k) -> k levels of n octave perlin noise

		for (Node node : this.voronoiNodes)
		{
			int x = (int)((double)noiseIndex/this.REGION_SIZE * node.getX());
			int y = (int)((double)noiseIndex/this.REGION_SIZE * node.getY());
			node.setElevation(preferedAverageElevation * (elevations[x][y]));
		}

		for (Node node : this.nodes)
		{
			int x = (int)((double)noiseIndex/this.REGION_SIZE * node.getX());
			int y = (int)((double)noiseIndex/this.REGION_SIZE * node.getY());
			node.setElevation(preferedAverageElevation * (elevations[x][y]));
		}
	}

	public void generateWater(double waterLevelUnitFactor)
	{
		if (waterLevelUnitFactor > 1) waterLevelUnitFactor = 1;
		if (waterLevelUnitFactor < 0) waterLevelUnitFactor = 0;
		this.waterLevel = (int)(this.minElevation + ((this.maxElevation - this.minElevation) + 
				(this.averageElevation - this.minElevation)) / 2 * waterLevelUnitFactor);

		for (Node node : this.voronoiNodes)
		{
			if (node.getZ() < waterLevel){
				node.setWater(true);
			}
		}
		for (Node node : this.nodes)
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
	public void generateWaterSourcesAndLakes(int numberOfLakes)
	{
		int fails = 0;
		for (int i = 0 ; i < numberOfLakes && fails < numberOfLakes; i++)
		{
			// get random source coordinates
			int x = this.REGION_SIZE / 2, y = this.REGION_SIZE / 2 ;
			x += -this.REGION_SIZE/2 + random.nextInt(this.REGION_SIZE/2);
			y += -this.REGION_SIZE/2 + random.nextInt(this.REGION_SIZE/2);
			// get source index
			int node_index = NodeUtilities.binarySearch(this.voronoiNodes, new Node(x,y));
			if (node_index < 0 || node_index >= voronoiNodes.size())
			{
				i--;
				fails++;
				continue;
			}
			Node node = this.voronoiNodes.get(node_index);
			if(node.getWater())
			{
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
				while (node != null)
				{
					depth = 0;
					deepest = null;
					for (Node connectedNode : node.getConnectedTo())
					{	
						int delta = (int)(node.getZ() - connectedNode.getZ());
						if ( !connectedNode.getWater() )
						{
							if ( delta > 0 && depth < delta)
							{
								deepest = connectedNode;
								depth = delta;
							}
							else if (source_strength > 0)
							{
								queueConnectedNodes.add(connectedNode);
							} 
						}
					}
					if (deepest != null)
					{
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
	public int getRandomNumberWithDistribution(Random random, int max, int min, double probabilityPower)
	{
		double randomDouble = random.nextDouble();
		double result = Math.floor(min + (max + 1 - min) * (Math.pow(randomDouble, probabilityPower)));
		return (int) result;
	}

	public void generateWaterSourcesAndRivers(int numberOfRivers)
	{ // TODO: FIX VERTICAL LINES
		int fails = 0;
		double spacingMagnitude = 5000000;
		for (int i = 0 ; i < numberOfRivers  && fails < numberOfRivers ; i++){
			double orientation = random.nextDouble();
			System.out.println("\n spacing " + spacingMagnitude + " orientation " + orientation);

			int x,y;
			x = random.nextInt(this.REGION_SIZE);
			y = random.nextInt(this.REGION_SIZE);
			System.out.println("x " + x + " y " + y);

			int node_index = NodeUtilities.binarySearch(this.voronoiNodes, new Node(x,y));
			Node node = this.voronoiNodes.get(node_index);
			if(node == null || node.getWater())
			{
				// if node is already water, skip and find another x,y coordinate and node
				i--; 
				fails++;
				continue;
			}

			double vectorX = Math.cos(orientation*2*3.14)*spacingMagnitude,
					vectorY = Math.sin(orientation*2*3.14)*spacingMagnitude;

			System.out.println("i " + i + ", old x " + node.getX() + ", old y " 
					+ node.getY() + " vX " + vectorX + " vY " + vectorY); 

			int xSource = node.getX() , 
					ySource = node.getY(),
					length = random.nextInt(100);
			for (int j = 1; j <= length; j++)
			{
				if (node == null) 
					continue;
				double xx, yy;
				xx = node.getX() + vectorX;
				yy = node.getY() + vectorY; 
				System.out.println("xx " + (float)xx +  " yy " + (float)yy +
						" vX " + vectorX + " vY" + vectorY);
				if (xx <= 0 && yy <= 0 || xx >= this.REGION_SIZE && yy >= this.REGION_SIZE)
				{
					fails++;
					break;
				}
				System.out.println("new x " + (float)xx + " y " + (float)yy);

				node = NodeUtilities.getClosestNodeToCoordinate(new Node((int)xx,(int)yy), this.voronoiNodes);
				if(node == null || node.getWater())
				{
					// if node is already water, skip and find another x,y coordinate and node
					//				System.out.println("skipped" + node); 
					continue;
				}
				node.setWater(true);

				double sourceStrength = 1*random.nextDouble();
				Queue<Node> queueConnectedNodes = new LinkedList<Node>();
				queueConnectedNodes.addAll(node.getConnectedTo());
				while (!queueConnectedNodes.isEmpty())
				{
					Node connectedNode = queueConnectedNodes.remove(); 
					if (!connectedNode.getWater() && 
							(int)connectedNode.getZ() <=
							(int)(node.getZ()))
					{
						connectedNode.setWater(true);
						queueConnectedNodes.addAll(connectedNode.getConnectedTo());
					}
				}
			}
		}
	}

	/**
	 * returns a text based representation of a region
	 */
	public String toString()
	{
		String out = "";
		int size = (nodes != null) ? nodes.size() : -1;
		int count = 0;
		while (count < size)
		{
			count++;
		}
		return out;
	}

	public int computeAverageElevation()
	{
		long sum = 0;
		synchronized (this.lock)
		{
			for (Node node : this.voronoiNodes){
				sum += node.getZ();
			}
            for (Node node : this.nodes){
                sum += node.getZ();
            }
		}
		int out = (int) (sum / (this.voronoiNodes.size() + this.nodes.size() ));
		return out;
	}

	public int computeMaximumElevation()
	{
		int max = 0;
		synchronized (this.lock)
		{
			for (Node node : this.voronoiNodes){
				if (max < node.getZ()){
					max = (int)node.getZ();
				}
			}
		}
		return max;
	}

	public int computeMinimumElevation()
    {
		int min = Integer.MAX_VALUE;
		synchronized (this.lock){
			for (Node node : this.voronoiNodes){
				if (min > node.getZ()){
					min = (int)node.getZ();
				}
			}
		}

		if (min == Integer.MAX_VALUE) min = -1;

		return min;
	}

	public void computeElevationParameters(){
		this.minElevation = this.computeMinimumElevation();
		this.maxElevation = this.computeMaximumElevation();
		this.averageElevation = this.computeAverageElevation();
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public int getRegionSize() {
		return REGION_SIZE;
	}

	public List<Node> getVoronoiNodes() {
		return voronoiNodes;
	}

    public void printHeightMap(){
        for (Node node : this.voronoiNodes){
            System.out.println("x" + node.getX() + "y" + node.getY() + "z" + node.getZ());
        }
    }

    public Object getLock(){
        return this.lock;
    }

    @Override
	public List<Node> getViewableNodes(){
		List<Node> list = new ArrayList<>();
		list.addAll(getNodes());
		list.addAll(getVoronoiNodes());
		return list;
	}

    @Override
	public long getViewableSeed() {
		return this.seed;
	}

    @Override
	public int getViewableSize(){
        return getRegionSize();
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
	public int getWaterLevel() {
		return waterLevel;
	}
}
