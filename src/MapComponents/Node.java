package MapComponents;
import java.util.ArrayList;
import java.util.List;


public class Node {
	private int x,y; float z;

	private List<Node> connectedTo;

	private boolean water;

	public Node(int x, int y)
	{
		this.x = x;
		this.y = y;
		this.z = 0;
		this.water = false;
		connectedTo = new ArrayList<Node>();
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	/**
	 * 
	 * @param node
	 * @return boolean, true if x,y coordinates of this node and compared node are the same, false otherwise
	 */
	public boolean equalsTo(Node node)
	{
		if (this.x == node.getX() && this.y == node.getY()) return true;
		return false;
	}

	public void connectWith(Node n)
	{
		if (n == null) return;
		for (Node node : this.connectedTo)
		{
			if (node.equals(n)) return;
		}
		this.connectedTo.add(n);
	}

	public double getDistanceTo(Node n)
	{
		return Math.sqrt(Math.pow((n.getX() - this.x), 2) + Math.pow((n.getY() - this.y), 2));
	}

	public String toString()
	{
		String out = this.x + ", " + this.y + ", " + this.z;
		return out;
	}

	public String print()
	{
		String out = this.toString();
		int size = connectedTo.size();
		if (size != 0)
		{
			out +=  ", connected to ";
		}
		for (int i = 0; i < size; i++)
		{
			out += connectedTo.get(i) + ",";
		}
		return out + "\n";
	}

	public List<Node> getConnectedTo()
	{
		return connectedTo;
	}

	public void mergeConnected(Node node)
	{
		for (Node n : node.getConnectedTo())
		{
			this.connectWith(n);
		}
	}

	/**
	 * 
	 * @param n Node to compare this with
	 * @return -1 if this is less than n, 0 if same, 1 if bigger
	 */
	public int compareWith(Node n)
	{
		if (this.x < n.getX())
		{
			return -1;
		}
		if (this.x == n.getX())
		{
			if (this.y < n.getY())
			{
				return -1;
			}
			if (this.y == n.getY())
			{
				return 0;
			}
			return 1;
		}
		return 1;
	}

	public void setElevation(float z)
	{
		this.z = z;
	}

	public double getZ()
	{
		return this.z;
	}

	public boolean getWater()
	{
		return water;
	}

	public void setWater(boolean water)
	{
		this.water = water;
	}
}
