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
	
	String strategy;
	
	int removedNodes=0;
	
	public static void main(String[] args) {
		String init = "17;" +
                "49,30,46;" +
                "7,57,6;" +
                "7,1;20,2;29,2;" +
                "350,10,9,8,28;" +
                "408,8,12,13,34;";
		solve(init,"ID", false);
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
		LLAP.setStrategy(strategy);
		Node solution;
		switch (strategy) {
			case "BF":
				solution = LLAP.BF();
				break;
			case "ID":
				solution = LLAP.ID();
				break;
			case "DF":
				solution = LLAP.DF();
				break;
			case "UC":
				solution = LLAP.UC();
				break;
			default:
				solution = LLAP.DF();
				break;
		}
		String formulatedSolution = LLAP.formulateSolution(solution);
		System.out.println(formulatedSolution);
		return formulatedSolution;
	}
	
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	
	public void parseProblem(String problem) {
		String[] parsedProblem = problem.split(";");
		State initialState = State.parseInitialState(parsedProblem[0], parsedProblem[1]);
		
		this.root = new Node(null, 0, null, initialState, 0);
		nodesInQueue.add(root);
		
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
			System.out.println("Path to goal:");
			return getPlan(goalNode) + ";" + Integer.toString(goalNode.pathCost) + ";" + this.expandedNodes.size();
		}
	}
	
	public String getPlan(Node node) {
		if(node.parent == null) return "";
		String parentPlan = getPlan(node.parent);
		System.out.println(node); // Printing nodes' order to reach goal
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
		
		State stateRequestFood = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestFood, Resource.FOOD);
		Node nodeRequestFood = new Node(node, node.depth+1, Action.REQUESTFOOD, stateRequestFood, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeRequestFood);
		
		
		State stateRequestMaterials = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestMaterials, Resource.MATERIALS);
		Node nodeRequestMaterials = new Node(node, node.depth+1, Action.REQUESTMATERIALS, stateRequestMaterials, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
		children.add(nodeRequestMaterials);
		
		State stateRequestEnergy = new State(node.state.prosperity, Math.min(node.state.food-1+food, Constants.RESOURCE_LIMIT), Math.min(node.state.materials-1+materials, Constants.RESOURCE_LIMIT), Math.min(node.state.energy-1+energy, Constants.RESOURCE_LIMIT), delayRequestEnergy, Resource.ENERGY);
		Node nodeRequestEnergy = new Node(node, node.depth+1, Action.REQUESTENERGY, stateRequestEnergy, node.pathCost+ unitPriceEnergy+ unitPriceFood+ unitPriceMaterials);
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
		
		removeInvalidNodes(children);
		removeReduntantNodes(children);
		
		return children;
	}
	
	public void removeReduntantNodes(ArrayList<Node> nodes) {
		for (int i=nodes.size()-1;i>=0;i--) {
			if(nodesInQueue.contains(nodes.get(i))) {
				nodes.remove(i);
			}else {
				nodesInQueue.add(nodes.get(i));
			}
		}
	}
	
	public boolean isNodeInvalid(Node node) {
		 return notEnoughResources(node) || invalidRequestAction(node) || !canWait(node) || fullResource(node);
	}
	
	public boolean notEnoughResources(Node node) {
		return node.state.energy < 0 || node.state.food < 0 || node.state.materials < 0 ||
				node.pathCost > Constants.INITIAL_MONEY || node.depth > maxDepth;
	}
	
	public boolean invalidRequestAction(Node node) {
		return node.parent.state.requestedResources != null &&
				(node.opertor == Action.REQUESTENERGY || node.opertor == Action.REQUESTFOOD || node.opertor == Action.REQUESTMATERIALS);
	}
	
	public boolean fullResource(Node node) {
		return (node.opertor == Action.REQUESTENERGY && (node.state.energy == 50)) || (node.opertor == Action.REQUESTFOOD && (node.state.food == 50)) || (node.opertor == Action.REQUESTMATERIALS && (node.state.materials == 50));
	}
	
	public boolean canWait(Node node) {
		return !(node.parent.state.requestedResources == null && node.opertor == Action.WAIT);
	}
	
	public void removeInvalidNodes(ArrayList<Node> nodes) {
		for (int i=nodes.size()-1;i>=0;i--) {
			if(isNodeInvalid(nodes.get(i))) {
				nodes.remove(i);
			}
		}
	}
	
	 public Node BF() {
		System.out.println("Starting BFS ...");
		this.queue = new LinkedList<Node>();
    	return this.search();
    }
	 
	public Node DF() {
		System.out.println("Starting DFS ...");
		this.queue = new PriorityQueue<Node>((o1, o2) -> {
    		return o1.depth>o2.depth?-1:1;
    	});
		return this.search();
    }
	
	public Node LD(int limit) {
		maxDepth = limit;
		Node goal = DF();
		return goal;
    }
	
	public Node ID() {
		System.out.println("Starting IDS ...");
		Node goal = null;
		for(int i = 0; i <Integer.MAX_VALUE; i++) {
			goal = LD(i);
			this.nodesInQueue.clear();
			if(goal != null) {
				break;
			}
		}
		return goal;
    }
	
	public Node UC() {
		System.out.println("Starting UCS ...");
		this.queue = new PriorityQueue<Node>((o1, o2) -> {
    		return o1.pathCost<o2.pathCost?-1:1;
    	});
		return this.search();
    }
	

}
