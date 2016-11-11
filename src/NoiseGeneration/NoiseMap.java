package NoiseGeneration;

public class NoiseMap 
{
	private Noise noise;
	private int noise_function;
	
	public NoiseMap(int seed, int noise_function)
	{
		this.noise = new Noise(seed);
		this.noise_function = noise_function;
	}
}
