package MapComponents;

public class Map {
	
	private Region[][] regions;
	
	private static Map map; // singleton
	
	private Map(){
		
	}
	
	public Map getInstance(){
		if (map == null)
		{	
			map = new Map();
		}
		return map;
	}
	
	public void generate (int MAP_SIZE){
		regions = new Region[MAP_SIZE][MAP_SIZE];
	}
	
}
