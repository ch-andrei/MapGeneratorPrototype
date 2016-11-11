package MapComponents;

public class Edge {
	
	private Node n1, n2;
	
	private boolean direction; // true for n1 to n2, false for n2 to n1
	
	public Edge(Node n1, Node n2){
		this.n1 = n1;
		this.n2 = n2;
		this.n1.connectWith(n2);
		this.n2.connectWith(n1);
		computeDirection();
	}
	
	public void computeDirection(){
		// TODO based on elevation of nodes
	}
	
	
}
