package code;

import java.lang.reflect.Field;
import java.util.PriorityQueue;

import code.Constants.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class LLAPSearch extends GenericSearch{
	static int unitPriceFood; 
	static int unitPriceMaterials;
	static int unitPriceEnergy;
	
	static int amountRequestFood;
	static int delayRequestFood;
	
	static int amountRequestMaterials;
	static int delayRequestMaterials;
	
	static int amountRequestEnergy;
	static int delayRequestEnergy;
	
	static int priceBUILD1;
	static int foodUseBUILD1;
	static int materialsUseBUILD1;
	static int prosperityBUILD1;
	static int energyUseBUILD1;
	
	static int priceBUILD2;
	static int foodUseBUILD2;
	static int materialsUseBUILD2;
	static int prosperityBUILD2;
	static int energyUseBUILD2;
	
	static int maxDepth = Integer.MAX_VALUE;
	
	public static void main(String[] args) {
		String init = "50;"+
				"22,22,22;" +
				"50,60,70;" +
				"30,2;19,1;15,1;" +
				"300,5,7,3,20;" +
				"500,8,6,3,40;";
		System.out.println(solve(init,"BF", false));
		printStaticVariables();
    }

    public static void printStaticVariables() {
        Field[] fields = LLAPSearch.class.getDeclaredFields();

        System.out.println("Static variables in LLAPSearch:");

        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                try {
                    field.setAccessible(true);
                    int value = field.getInt(null);
                    System.out.println(field.getName() + " = " + value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
	public static String solve(String initalState, String strategy, boolean visualize) {
		LLAPSearch LLAP = new LLAPSearch();
		LLAP.parseProblem(initalState);
		Node solution;
		switch (strategy) {
			case "BF":
				solution = LLAP.BF();
				break;
			default:
				solution = LLAP.DF();
				break;
		}
		return LLAP.formulateSolution(solution);
	}
	
	public void parseProblem(String problem) {
		String[] parsedProblem = problem.split(";");
		State initialState = State.parseInitialState(parsedProblem[0], parsedProblem[1]);
		
		this.root = new Node(null, 0, null, initialState, 0);
		
		String[] unitPrices = parsedProblem[2].split(",");
		unitPriceFood = Integer.parseInt(unitPrices[0]);
		unitPriceMaterials = Integer.parseInt(unitPrices[1]);
		unitPriceEnergy = Integer.parseInt(unitPrices[2]);
		
		String[] requestFoodCost = parsedProblem[3].split(",");
		amountRequestFood = Integer.parseInt(requestFoodCost[0]);
		delayRequestFood = Integer.parseInt(requestFoodCost[1]);
		
		String[] requestMaterialsCost = parsedProblem[4].split(",");
		amountRequestMaterials = Integer.parseInt(requestMaterialsCost[0]);
		delayRequestMaterials = Integer.parseInt(requestMaterialsCost[1]);
		
		String[] requestEnergyCost = parsedProblem[5].split(",");
		amountRequestEnergy = Integer.parseInt(requestEnergyCost[0]);
		delayRequestEnergy = Integer.parseInt(requestEnergyCost[1]);
		
		String[] build1Effect =  parsedProblem[6].split(",");
		priceBUILD1 = Integer.parseInt(build1Effect[0]);
		foodUseBUILD1 = Integer.parseInt(build1Effect[1]);
		materialsUseBUILD1 = Integer.parseInt(build1Effect[2]);
		energyUseBUILD1 = Integer.parseInt(build1Effect[3]);
		prosperityBUILD1 = Integer.parseInt(build1Effect[4]);
		
		String[] build2Effect =  parsedProblem[7].split(",");
		priceBUILD2 = Integer.parseInt(build2Effect[0]);
		foodUseBUILD2 = Integer.parseInt(build2Effect[1]);
		materialsUseBUILD2 = Integer.parseInt(build2Effect[2]);
		energyUseBUILD2 = Integer.parseInt(build2Effect[3]);
		prosperityBUILD2 = Integer.parseInt(build2Effect[4]);
	}
	
	public String formulateSolution(Node goalNode) {
		if (goalNode == null) {
			return "NOSOLUTION";
		}else {
			return getPlan(goalNode) + ";" + Integer.toString(goalNode.pathCost) + ";" + this.expandedNodes.size();
		}
	}
	
	public String getPlan(Node node) {
		if(node.parent == null) return "";
		String parentPlan = getPlan(node.parent);
		String plan = parentPlan + (parentPlan.equals("")?"":",") + node.opertor.name();
		return plan;
	}

	@Override
	public boolean goalTest(Node node) {
		return node.state.prosperity >= Constants.GOAL_PROSPERITY_LEVEL;
	}

	@Override
	public ArrayList<Node> getChildren(Node node) {
		ArrayList<Node> children = new ArrayList<>();
		
		int delay;
		Resource requestedResource;
		int resourceAmount;
		int food = 0;
		int materials = 0;
		int energy = 0;
		
		if(node.state.delay>0) {
			delay = node.state.delay-1;
			resourceAmount = delay>0? 0 : node.state.requestedResources == Resource.FOOD? amountRequestFood : node.state.requestedResources == Resource.ENERGY? amountRequestEnergy : amountRequestMaterials;
			switch(node.state.requestedResources) {
				case FOOD: food = resourceAmount;break;
				case MATERIALS: materials = resourceAmount;break;
				case ENERGY: energy = resourceAmount;break;
				default: break;
			}
			requestedResource = delay>0? node.state.requestedResources:null;
		}else {
			delay = 0;
			requestedResource = null;
		}
		
		State stateRequestFood = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestFood+1, Resource.FOOD);
		Node nodeRequestFood = new Node(node, node.depth+1, Action.REQUEST_FOOD, stateRequestFood, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeRequestFood);
		
		
		State stateRequestMaterials = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestMaterials+1, Resource.MATERIALS);
		Node nodeRequestMaterials = new Node(node, node.depth+1, Action.REQUEST_MATERIALS, stateRequestMaterials, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeRequestMaterials);
		
		State stateRequestEnergy = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestEnergy+1, Resource.ENERGY);
		Node nodeRequestEnergy = new Node(node, node.depth+1, Action.REQUEST_ENERGY, stateRequestEnergy, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeRequestEnergy);
		
		State stateWait = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delay, requestedResource);
		Node nodeWait = new Node(node, node.depth+1, Action.WAIT, stateWait, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeWait);
		
		State stateBuild1 = new State(Math.min(node.state.prosperity+prosperityBUILD1, Constants.GOAL_PROSPERITY_LEVEL), Math.min(node.state.food-foodUseBUILD1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-materialsUseBUILD1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-energyUseBUILD1+energy, Constants.RESOURCE_LIMIT), delay, requestedResource);
		int build1PathCost = node.pathCost+ unitPriceEnergy*energyUseBUILD1+ unitPriceFood*foodUseBUILD1 + unitPriceMaterials*materialsUseBUILD1 +priceBUILD1;
		Node nodeBuild1 = new Node(node, node.depth+1, Action.BUILD1, stateBuild1, build1PathCost);
		children.add(nodeBuild1);
		
		State stateBuild2 = new State(Math.min(node.state.prosperity+prosperityBUILD2, Constants.GOAL_PROSPERITY_LEVEL), Math.min(node.state.food-foodUseBUILD2+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-materialsUseBUILD2+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-energyUseBUILD2+energy, Constants.RESOURCE_LIMIT), delay, requestedResource);
		int build2PathCost = node.pathCost+ unitPriceEnergy*energyUseBUILD2+ unitPriceFood*foodUseBUILD2 + unitPriceMaterials*materialsUseBUILD2 +priceBUILD2;
		Node nodeBuild2 = new Node(node, node.depth+1, Action.BUILD2, stateBuild2, build2PathCost);
		children.add(nodeBuild2);
		
		removeReduntantNodes(children);
		removeInvalidNodes(children);
		
		return children;
	}
	
	public void removeReduntantNodes(ArrayList<Node> nodes) {
		for(int i=0;i<nodes.size();i++) {
			for(int j=i+1;j<nodes.size();j++) {
				if(nodes.get(i).equals(nodes.get(j))) {
					if(nodes.get(i).compareTo(nodes.get(j)) == 1) {
						System.out.println("Removing repeated child 1:");
						System.out.println(nodes.remove(i));
					}else if(nodes.get(i).compareTo(nodes.get(j)) == -1) {
						System.out.println("Removing repeated child 2:");
						System.out.println(nodes.remove(j));
					}
				}
			}
		}
		for (int i=nodes.size()-1;i>=0;i--) {
			for (Node expandedNode : this.expandedNodes) {
				if(nodes.get(i).equals(expandedNode)) {
					System.out.println("Removing redundant child:");
					System.out.println(nodes.remove(i));
					break;
				}
			}
		}
	}
	
	public void removeInvalidNodes(ArrayList<Node> nodes) {
		for (int i=nodes.size()-1;i>=0;i--) {
			if(nodes.get(i).state.energy < 0 || nodes.get(i).state.food < 0 || nodes.get(i).state.materials < 0 ||
					nodes.get(i).pathCost > Constants.INITIAL_MONEY || nodes.get(i).depth > maxDepth ) {
				nodes.remove(i);
			}else if(nodes.get(i).parent.state.requestedResources != null &&
					(nodes.get(i).opertor == Action.REQUEST_ENERGY || nodes.get(i).opertor == Action.REQUEST_FOOD || nodes.get(i).opertor == Action.REQUEST_MATERIALS)) {
				nodes.remove(i);
			}else if(nodes.get(i).parent.state.requestedResources == null && nodes.get(i).opertor == Action.WAIT) {
				nodes.remove(i);
			}
		}
	}
	
	 public Node BF() {
		System.out.println("Starting BFS ...");
    	return this.search(new LinkedList<Node>());
    }
	 
	public Node DF() {
		System.out.println("Starting DFS ...");
		return this.search( new PriorityQueue<Node>((o1, o2) -> {
    		return o1.depth>o2.depth?-1:1;
    	}));
    }
	
}
