package VoronoiGenerator;

import java.util.ArrayList;
import java.util.List;

import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Voronoi;
import MapComponents.Node;
import MapComponents.NodeUtilities;
import MapComponents.Region;

/**
 * 
 * @author Andrei C
 *
 */
public class VoronoiAdapter extends Voronoi{

	public VoronoiAdapter(double minDistanceBetweenSites)
	{
		super(minDistanceBetweenSites);
	}

	public void generateVoronoiNodesToArray(Region region, double size, List<Node> nodesVoronoi)
	{
		List<Node> nodes = region.getNodes();
		int length = nodes.size();
		double[] xValuesIn = new double[length],
				yValuesIn = new double[length];
		int counter = 0;
		long t1 = System.currentTimeMillis();
		for (Node node : nodes)
		{
			xValuesIn[counter] = node.getX();
			yValuesIn[counter] = node.getY();
			counter++;
		}
		long t2 = System.currentTimeMillis();
		System.out.println("voronoi: first loop " + (t2-t1));
		t1 = System.currentTimeMillis();
		List<GraphEdge> edges = generateVoronoi(xValuesIn, yValuesIn, 0, size, 0, size);
		t2 = System.currentTimeMillis();
		System.out.println("inside voronoi " + (t2-t1));
		t1 = System.currentTimeMillis();
		System.out.println("edges size " + edges.size());
		for (GraphEdge edge : edges)
		{
			Node node1 = new Node((int)edge.x1, (int)edge.y1),
					node2 = new Node((int)edge.x2, (int)edge.y2),
					node3, node4;
			int index1 = NodeUtilities.binaryInsert(nodesVoronoi, node1);
			if (node1.equalsTo(node2)) 
				continue;
			int index2 = NodeUtilities.binaryInsert(nodesVoronoi, node2);
			if (index1 < index2)
			{
				node3 = nodesVoronoi.get(index1);
				node4 = nodesVoronoi.get(index2);
			}
			else 
			{
				if (node1.equalsTo(nodesVoronoi.get(index1)))
					node3 = nodesVoronoi.get(index1);
				else
					node3 = nodesVoronoi.get(++index1);
				node4 = nodesVoronoi.get(index2);
			}
			node3.connectWith(node4);
			node4.connectWith(node3);
		}
		t2 = System.currentTimeMillis();
		System.out.println("voronoi 2nd loop " + (t2-t1));
	}
}
