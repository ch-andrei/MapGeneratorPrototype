package NoiseGeneration;

import java.util.Random;

import MapComponents.NodeUtilities;

/**
 * java implementation and further modifications done by Andrei C.
 * original C# implementation of perlin noise generator coded by Dev.mag
 * http://devmag.org.za/2009/04/25/perlin-noise/
 *
 */
public class Noise {

	private final int NOISE_RESOLUTION = 500;
	private int seed;

	public Noise(int seed)
	{
		this.seed = seed;
	}

	public float[][] generatePerlinNoise(int octaveCount)
	{
		float[][] baseNoise = generateWhiteNoise(NOISE_RESOLUTION, this.seed);
		return generatePerlinNoise(baseNoise, octaveCount);
	}

	public float[][] generateMultipleLevelPerlinNoise(int octaveCount, int levels)
	{
		float[][] perlinNoiseCombined = new float[NOISE_RESOLUTION][NOISE_RESOLUTION];
		// generate 0,1,...,levels of perlin noise patterns and merge these
		for (int i = 1; i <= levels; i++)
		{
			float[][] baseNoise = generateWhiteNoise(NOISE_RESOLUTION, this.seed + i - 1);
			float[][] perlinNoise = generatePerlinNoise(baseNoise, octaveCount);
			// merge results of new perlin level with previous perlinNoise
			perlinNoiseCombined = NodeUtilities.mergeArrays(perlinNoise, perlinNoiseCombined,1,1);
		}
		perlinNoiseCombined = NodeUtilities.mergeArrays(generateNormalizedHalfSphere(this.NOISE_RESOLUTION, 2), perlinNoiseCombined,1,3);
		return perlinNoiseCombined;
	}

	// TODO generator with custom parameters
	public float[][] generateWhiteNoise(int size, int seed)
	{
		Random random = new Random(seed);
		float[][] noise = new float[size][size];
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				noise[i][j] = (float)random.nextDouble() % 1;
			}
		}
		return noise;
	}
	
	// TODO
	public float[][] generateNormalizedHalfSphere(int size, int intensity)
	{
		float[][] elevations = new float[size][size];
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				float val = (float)(Math.pow(size/2, 2) - Math.pow(i-size/2, 2) - Math.pow(j - size/2, 2));
				val = (val > 0) ? val : 0;
				elevations[i][j] = (float)Math.sqrt(val);
			}
		}
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				elevations[i][j] /= size*intensity;
				if (elevations[i][j] > 1) System.out.println("wuuut");
			}
		}
		return elevations;
	}

	public float[][] generateSmoothNoise(float[][] baseNoise, int octave)
	{
		int length = baseNoise.length;
		float[][] smoothNoise = new float[length][length];

		int samplePeriod = (int)Math.pow(2, octave); // calculates 2 ^ k
		float sampleFrequency = 1.0f / samplePeriod;

		for (int i = 0; i < length; i++)
		{
			//calculate the horizontal sampling indices
			int sample_i0 = (i / samplePeriod) * samplePeriod;
			int sample_i1 = (sample_i0 + samplePeriod) % length; //wrap around
			float horizontal_blend = (i - sample_i0) * sampleFrequency;

			for (int j = 0; j < length; j++)
			{
				//calculate the vertical sampling indices
				int sample_j0 = (j / samplePeriod) * samplePeriod;
				int sample_j1 = (sample_j0 + samplePeriod) % length; //wrap around
				float vertical_blend = (j - sample_j0) * sampleFrequency;

				//blend the top two corners
				float top = Interpolate(baseNoise[sample_i0][sample_j0],
						baseNoise[sample_i1][sample_j0], horizontal_blend);

				//blend the bottom two corners
				float bottom = Interpolate(baseNoise[sample_i0][sample_j1],
						baseNoise[sample_i1][sample_j1], horizontal_blend);

				//final blend
				smoothNoise[i][j] = Interpolate(top, bottom, vertical_blend);                    
			}
		}
		return smoothNoise;
	}

	public float[][] generatePerlinNoise(float[][] baseNoise, int octaveCount)
	{
		int length = baseNoise.length;
		float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing
		float persistance = 0.75f;

		//generate smooth noise
		for (int i = 0; i < octaveCount; i++)
		{
			smoothNoise[i] = generateSmoothNoise(baseNoise, i);
		}

		float[][] perlinNoise = new float[length][length]; //an array of floats initialized to 0

		float amplitude = 2.0f;
		float totalAmplitude = 0.0f;

		//blend noise together
		for (int octave = octaveCount - 1; octave >= 0; octave--)
		{
			amplitude *= persistance;
			totalAmplitude += amplitude;

			for (int i = 0; i < length; i++)
			{
				for (int j = 0; j < length; j++)
				{
					perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
				}
			}
		}

		//normalisation
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < length; j++)
			{
				perlinNoise[i][j] /= totalAmplitude;
			}
		}        

		return perlinNoise;
	}
	
	public float Interpolate(float x0, float x1, float alpha)
	{
		return x0 * (1 - alpha) + alpha * x1;
	}

	public int getNoiseRes()
	{
		return NOISE_RESOLUTION;
	}
}
