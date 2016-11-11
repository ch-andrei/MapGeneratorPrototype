import MapComponents.Region;
import View.MapView;

public class Driver {
	public static void main(String[] args)
	{
		long time1 = System.currentTimeMillis(),
				time3;
		
		/*
		 * parameters of the map to generate 
		 */
		
		int choice = 5,
				n = 50000,
				seed = (int)System.currentTimeMillis(),
				height = 1000; // less than 100 doesn't work well
		
		Region region;
		switch (choice)
		{
		case 0:
			region = generateOceanAndIslands(n,seed,height);
			break;
		case 1:
			region = generateOceanAndCentralizedIsland(n,seed,height);
			break;
		default:
			region = new Region(20000, (int)System.currentTimeMillis() , 1000, 0.7, 10, 0);
			break;
		}
		
		long time2 = System.currentTimeMillis();

		MapView view = new MapView(region, "World");
		time3 = System.currentTimeMillis();

		Thread thread1 = new Thread(region);
		thread1.start();
		Thread thread2 = new Thread(view);
		thread2.start();

		System.out.println("from driver: region " + (time2-time1) + " ;view  " + (time3 - time2)); 
	}

	public static Region generateOceanAndIslands(int n, int seed, int height)
	{
		return new Region(n, seed , height, 0.95, 100, 0);
	}
	
	public static Region generateOceanAndCentralizedIsland(int n, int seed, int height)
	{
		return new Region(n, seed , height, 0.7, 100, 0);
	}
}