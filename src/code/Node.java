package code;

import code.Constants.Action;

public class Node implements Comparable<Node>{
	State state; 
	Node parent;
	Action opertor;
	int depth;
	int pathCost;
	
	public Node(Node parent,int depth, Action operator, State state, int pathCost) {
		this.state = state;
		this.parent = parent;
		this.opertor = operator;
		this.depth = depth;
		this.pathCost = pathCost;
	}

	@Override
	public String toString() {
		return "Node [state=" + state + ", opertor=" + opertor + ", depth=" + depth
				+ ", pathCost=" + pathCost + "]";
	}

	@Override
	public int compareTo(Node o) {
		return  this.pathCost > o.pathCost? 1:-1;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.state.equals(((Node)o).state);
	}
	
	
}
