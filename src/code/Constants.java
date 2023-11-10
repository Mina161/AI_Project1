package code;

public class Constants {
	
    public static final int INITIAL_MONEY = 100000;
    public static final int GOAL_PROSPERITY_LEVEL = 100;
    public static final int RESOURCE_LIMIT = 50;
    
    enum Resource {
    	FOOD,
    	MATERIALS,
    	ENERGY
    }

    enum Action {
        REQUESTFOOD,
        REQUESTMATERIALS,
        REQUESTENERGY,
        WAIT,
        BUILD1,
        BUILD2
    }

	enum Strategy {
	    BF,
	    DF,
	    ID,
	    UC,
	    GR1,
	    GR2,
	    AS1,
	    AS2
	}
}
