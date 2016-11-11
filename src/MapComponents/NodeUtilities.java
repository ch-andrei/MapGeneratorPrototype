package MapComponents;

import java.util.List;

/**
 * Decoupled Utilities class for Node related methods.
 * includes: sorting, inserting, checking if sorted, connecting closest.
 * @author Andrei C
 *
 */
public class NodeUtilities {


	/**
	 * inserts a node into a sorted list of nodes in O(log2n)
	 * @param nodes
	 * @param node
	 */
	public static int binaryInsert(List<Node> nodes, Node node)
	{
		int index = binarySearch(nodes, node);	
		insertToNodeListAtIndex(nodes, node, index);
		return index;
	}
	
	public static void insertToNodeListAtIndex(List<Node> nodes, Node node, int index)
	{
		if (index >= nodes.size())
		{
			nodes.add(node);
		}	
		else if (index == -1)
		{
			nodes.add(0, node);
		}
		else 
		{
			if (nodes.get(index).equalsTo(node))
			{
				// dont insert if a node at these coordinates is already present
				// data merge instead
				nodes.get(index).mergeConnected(node);
				return;
			}
			nodes.add(index, node);
		}
	}

	/**
	 * searches for a node whose lexicographic index in the Node list is closest or equal to that of val
	 * @param nodes
	 * @param val
	 * @return
	 */
	public static int binarySearch(List<Node> nodes, Node val)
	{
		int mid = 0, left = 0, right = nodes.size(), prev = -1;
		if (left > right) {
			return -1;
		}
		while (left < right)
		{
			mid = (right + left) / 2;	
			int comparison = nodes.get(mid).compareWith(val);
			if (comparison == 1)
			{
				right = mid;
			}
			else if (comparison == -1)
			{
				left = mid;
			}
			else
			{
				return mid;
			}
			if (prev == mid) 
			{
				return mid + 1;
			}
			prev = mid;
		}
		return mid;
	}

	public static Node getClosestNode(int i, List<Node> nodes, Node current, Node closest, double closestD)
	{
		// find left closest node
		int size = nodes.size(), 
				deltaXLeft = 1, 
				deltaXRight = 1, 
				counter = 1;
		Node currentCompared;
		while (closestD > deltaXRight && (i + counter) < size)
		{
			currentCompared = nodes.get(i+counter);
			double comparedD = current.getDistanceTo(currentCompared);
			if (closestD > comparedD)
			{
				closestD = comparedD;
				closest = currentCompared;
			}
			deltaXRight = Math.abs(current.getX() - currentCompared.getX());
			counter++;
		}

		counter = 1;
		while (closestD > deltaXLeft && (i - counter) >= 0)
		{
			currentCompared = nodes.get(i-counter);
			double comparedD = current.getDistanceTo(currentCompared);
			if (closestD > comparedD)
			{
				closestD = comparedD;
				closest = currentCompared;
			}
			deltaXLeft = Math.abs(current.getX() - currentCompared.getX());
			counter++;
		}

		return closest;
	}

	public static Node getClosestNodeToCoordinate(Node val, List<Node> nodes)
	{
		int node_index = binarySearch(nodes, val);
		// TODO FIX OUT OF BOUNDS
		if (node_index >= nodes.size() || node_index < 0) 
			return null;
		Node current = nodes.get(node_index);
		double closestD;
		if (nodes.size() <= 1) 
		{ 
			return null;
		}

		closestD = current.getDistanceTo(val);

		// find left closest node
		return getClosestNode(node_index, nodes, current, null, closestD);
	}

	/**
	 * finds the closest node to node at index i (distance based, not lexicographic)
	 * @param i
	 * @param nodes
	 * @return
	 */
	public static Node getClosestNodeToIndex(int i, List<Node> nodes)
	{
		Node current = nodes.get(i);
		Node closest = null;
		double closestD;
		if (nodes.size() <= 1) 
		{ 
			return null;
		}
		if (i != 0)
		{
			// find closest along y axis but for same x coordinate as current node
			// in order to set initial boundaries for search
			closestD = current.getDistanceTo(nodes.get(i-1));
			closest = nodes.get(i-1);
			if (i != nodes.size() - 1 && closestD > current.getDistanceTo(nodes.get(i+1))){
				closestD = current.getDistanceTo(nodes.get(i+1));
				closest = nodes.get(i+1);
			}
		}
		else 
		{
			closestD = current.getDistanceTo(nodes.get(i+1));
			closest = nodes.get(i+1);
		}

		// find left closest node
		return getClosestNode(i, nodes, current, closest, closestD);
	}

	/**
	 * functionality:
	 * finds and connects closest nodes. 
	 * operation:
	 * WARNING: requires lexicographically sorted data structure for node list (sorted by x,y).
	 * iterates through nodes linearly starting from current node. 
	 * closestD tracks currently smallest distance to a closest node.
	 * the algorithm searches laterally along positive and negative direction over the x axis 
	 * with deltaX measuring current x-displacement from current node.
	 * search stops when deltaX exceeds closestD, i.e. no closer nodes exists in the list.
	 */
	public static void connectClosestNodes(List<Node> nodes)
	{
		int i = 0;
		for (Node current : nodes){
			Node closest = getClosestNodeToIndex(i, nodes);
			if (closest != null)
			{
				connectTwoNodes(current, closest);
			}
			i++;
		}
	}	
	
	/**
	 * returns a new array with the merged weighted data from the input arrays.
	 * @param a
	 * @param b
	 * @param weightA
	 * @param weightB
	 * @return
	 */
	public static float[][] mergeArrays(float[][] a, float[][] b, int weightA, int weightB)
	{
		// must be able to work with arrays of different size
		boolean choice = a.length > b.length;
		float[][] c = (choice) ? new float[a.length][a.length] : new float[b.length][b.length];
		double ratio = (double) a.length / b.length;
		for (int i = 0; i < c.length; i++){
			for (int j = 0; j < c.length; j++)
			{
				// sum weighted values
				if (choice)
				{
					c[i][j] = weightA * a[i][j] + weightB * b[(int) (i/ratio)][(int) (j/ratio)];
				}
				else {
					c[i][j] = weightA * a[(int) (i*ratio)][(int)(j*ratio)] + weightB * b[i][j];
				}
				// rescale the values back
				c[i][j] /= (weightA + weightB);
			}
		}
		return c;
	}

	/**
	 * uses connection method from Node class to connect n1 to n2 and n2 to n1.
	 * @param n1 node1
	 * @param n2 node2
	 */
	public static void connectTwoNodes(Node n1, Node n2){
		n1.connectWith(n2);
		n2.connectWith(n1);
	}

	/**
	 * linearly checks if duplicate nodes are present and if so merges node connectivity data
	 * @param nodes
	 */
	public static void mergeAndRemoveDuplicateNodes(List<Node> nodes)
	{
		if (nodes.size() <= 1)
		{
			return;
		}
		for (int i = 0; i < nodes.size(); i++)
		{
			Node current = nodes.get(i);
			while (i+1 < nodes.size() && current.equalsTo(nodes.get(i+1))){
				current.mergeConnected(nodes.get(i+1));
				nodes.remove(i+1);
			}
		}
	}

	public static boolean checkForDuplicateNodes(List<Node> nodes)
	{
		int i = 0;
		while (i < nodes.size() - 1)
		{
			if (nodes.get(i).equalsTo(nodes.get(i+1))) {
				return true;
			}
			i++;
		}
		return false;
	}

	public static boolean checkLexicographicSort(List<Node> nodes)
	{
		int size = nodes.size(), index = 0;
		Node current, next;
		while (index < size - 1)
		{
			current = nodes.get(index);
			next = nodes.get(index + 1);
			if (current.getX() > next.getX() || 
					(current.getX() == next.getX() && current.getY() > next.getY()))
			{
				System.out.println("failed at " + index);
				return false;
			}
			index++;
		}
		return true;
	}


	/**
	 * inserts node in a list linearly. 
	 * DEPRECATED OLD CODE, NOT USED ANYMORE
	 * @param nodes
	 * @param nodeToAdd
	 */
	public static void linearInsert(List<Node> nodes, Node nodeToAdd)
	{
		Node current = null, previous = null;
		int size = nodes.size();
		// want to insert nodes in an ordered way (lexicographic by x,y coordinate)
		if (size == 0){
			nodes.add(nodeToAdd);
		}
		else if (size == 1){

			current = nodes.get(0);

			if (current.getX() <= nodeToAdd.getX() ){
				nodes.add(nodeToAdd);
			}
			else {
				nodes.add(0, nodeToAdd);
			}
		}

		else {
			for (int j = 0; j < size-1; j++){

				previous = nodes.get(j);
				current = nodes.get(j+1);

				// 5 cases based on x coordinate: 
				// less than both, equal to first, in between, equal to second, more than both

				if (previous.getX() > nodeToAdd.getX()){
					nodes.add(j,nodeToAdd);
					break;
				}

				if (previous.getX() == nodeToAdd.getX() 
						){
					if (previous.getY() >= nodeToAdd.getY()){
						nodes.add(j,nodeToAdd);
						break;
					}
					else if (nodeToAdd.getX() != current.getX() && 
							previous.getX() != current.getX()){
						if (previous.getY() >= nodeToAdd.getY()){
							nodes.add(j,nodeToAdd);
							break;
						}
						else {
							nodes.add(j+1,nodeToAdd);
							break;
						}
					}
				}

				if (current.getX() > nodeToAdd.getX()){
					nodes.add(j+1,nodeToAdd);
					break;
				}

				if (current.getX() == nodeToAdd.getX() ){
					if (current.getY() >= nodeToAdd.getY()){
						nodes.add(j+1,nodeToAdd);
						break;
					}
					else if (j == size - 2){
						nodes.add(nodeToAdd);
						break;
					}
				}

				if (j == size - 2){
					nodes.add(nodeToAdd);
					break;
				}
			}
		}

		// System.out.println("iteration " + i + "\n" + this.toString());
	}

}
