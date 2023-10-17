package code;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public abstract class GenericSearch {
	Node root;
	Queue<Node> expandedNodes;
	

	public GenericSearch() {
		this.expandedNodes = new LinkedList<Node>();
	}
	
	public Node search(Queue<Node> queue) {
		queue.add(root);
		while(!queue.isEmpty()) {
			Node currNode = queue.poll();
			System.out.println("Expanding Node: ");
			System.out.println(currNode);
			System.out.println();
			expandedNodes.add(currNode);
			if(this.goalTest(currNode))
				return currNode;
			ArrayList<Node> children = this.getChildren(currNode);
			for (Node node : children) {
				queue.add(node);
			}
		}
		return null;
	}
	
	public abstract ArrayList<Node> getChildren(Node node);
	
	public abstract boolean goalTest(Node node);
}
