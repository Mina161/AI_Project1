package code;

import code.Constants.*;

public class State {
	
	int prosperity;
	int food;
	int materials;
	int energy;
	int delay;
	Resource requestedResources;
	
	public State(int prosperity, int food, int materials, int energy, int delay,Resource requestedResources) {
		super();
		this.prosperity = prosperity;
		this.food = food;
		this.materials = materials;
		this.energy = energy;
		this.delay = delay;
		this.requestedResources = requestedResources;
	}
	
	public static State parseInitialState(String initialProsperityString, String initialResources) {
		String[] resources = initialResources.split(",");
		int initialProsperity = Integer.parseInt(initialProsperityString);
		int initialFood = Integer.parseInt(resources[0]);
		int initialMaterials = Integer.parseInt(resources[1]);
		int initialEnergy = Integer.parseInt(resources[2]);
		return new State(initialProsperity, initialFood, initialMaterials, initialEnergy, 0, null);
	}

	@Override
	public String toString() {
		return "State [prosperity=" + prosperity + ", food=" + food + ", materials=" + materials + ", energy=" + energy
				+ ", delay=" + delay + ", requestedResources=" + requestedResources + "]";
	}
	
	@Override
	public int hashCode() {
	    int result = 17;
	    result = 31 * result + prosperity;
	    result = 31 * result + food;
	    result = 31 * result + materials;
	    result = 31 * result + energy;
	    result = 31 * result + delay;
	    result = 31 * result + (requestedResources == null ? 0 : requestedResources.hashCode());

	    return result;
	}

	@Override
	public boolean equals(Object o) {
		State comingState = (State)o;
		return (this.energy <= comingState.energy && this.food <= comingState.food && this.materials <= comingState.materials &&
				this.prosperity <= comingState.prosperity && 
				this.delay >= comingState.delay && this.requestedResources == comingState.requestedResources);
	}
	
	

}
