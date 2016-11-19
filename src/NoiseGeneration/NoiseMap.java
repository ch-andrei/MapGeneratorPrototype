package NoiseGeneration;

import MapComponents.NodeUtilities;

public class NoiseMap
{
	private Noise noise;
	private int noise_function;
	private float[][] elevations;
	
	public NoiseMap(int seed, int noise_function)
	{
		this.noise = new Noise(seed, 500);
		this.noise_function = noise_function;
        this.elevations = noise.generateMultipleLevelPerlinNoise(8,5);
        this.noise_function = 4;
        switch (this.noise_function){
            default:
                break;
            case 0:
                elevations = applyNormalizedHalfSphere(elevations, elevations.length, 1f);
                break;
            case 1:
                applyLogisticsFunctionToElevations(elevations);
                break;
            case 2:
                amplifyElevations(elevations, 3);
                break;
            case 3:
                smoothenConvolutionFilter(elevations, 0.05f);
                break;
            case 4:
                elevations = applyNormalizedHalfSphere(elevations, elevations.length, 1f);
                amplifyElevations(elevations, 3);
                //applyLogisticsFunctionToElevations(elevations);
                break;
        }
        normalizeToNElevationLevels(elevations, 25);
        normalize(elevations);
        System.out.println("Noise Map generated.");
	}

    public void normalizeToNElevationLevels(float[][] elevations, int levels){
        for (int i = 0; i < elevations.length; i++){
            for (int j = 0; j < elevations[i].length; j++){
                elevations[i][j] = (elevations[i][j] * levels) % levels / levels;
            }
        }
    }

    public int getNoiseRes(){
        return this.noise.getNoiseRes();
    }

    // smooth_faactor < 0.2 gives best results
    public void smoothenConvolutionFilter(float[][] elevations, float smooth_factor){
        float weights[][] = {{smooth_factor,smooth_factor,smooth_factor},
                            {smooth_factor,0,smooth_factor},
                            {smooth_factor,smooth_factor,smooth_factor}};
        convolutionFilter(elevations, weights);
    }

    // smooth_faactor < 0.2 gives best results
    public void embossConvolutionFilter(float[][] elevations, float amplify_factor){
        float weights[][] = {{-2*amplify_factor,-amplify_factor,0},
                            {-amplify_factor,amplify_factor,amplify_factor},
                            {0,amplify_factor,2*amplify_factor}};
        convolutionFilter(elevations, weights);
    }

	public float logisticsFunction(float value){
		final float growth_rate = 5.0f;
		return (float)(1.0/(1 + Math.exp(growth_rate/2 + -growth_rate*value)));
	}

    // flattens the terrain
	public void applyLogisticsFunctionToElevations(float[][] elevations){
        System.out.println("Applying logistics equation!");
		for (int i = 0; i < elevations.length; i++){
			for (int j = 0; j < elevations.length; j++){
				elevations[i][j] = logisticsFunction(elevations[i][j]);
			}
		}
	}

    // results in elevation = elevation ^ amplify_factor
	public void amplifyElevations(float[][] elevations, int amplify_factor){
        System.out.println("Amplifying!");
		float sumBefore = 0, sumAfter = 0;
		for (int i = 0; i < elevations.length; i++){
			for (int j = 0; j < elevations[i].length; j++){
				sumBefore += elevations[i][j];
				elevations[i][j] = (float) Math.pow(elevations[i][j], amplify_factor);
				sumAfter += elevations[i][j];
			}
		}
		sumBefore /= (elevations.length * elevations[0].length);
		sumAfter /= (elevations.length * elevations[0].length);
		System.out.println("BEFORE " + sumBefore + " AFTER " + sumAfter);
	}

    public void convolutionFilter(float[][] elevations, float[][] weights){
        System.out.println("Convolution filter!");
        for (int i = 1; i < elevations.length - 1; i++){
            for (int j = 1; j < elevations[0].length - 1; j++){
                for (int ii = -1; ii < 2; ii++){
                    for (int jj = -1; jj < 2; jj++){
                        elevations[i][j] += weights[ii+1][jj+1] * elevations[i+ii][j+jj];
                    }
                }
                if (elevations[i][j] < 0){
                    elevations[i][j] = 0;
                }
                if (elevations[i][j] > 1){
                    elevations[i][j] = 1;
                }
            }
        }
    }

    public void normalize(float[][] elevations){
        System.out.println("Normalizing!");
        float avg = 0, max = 0;
        for (int i = 0; i < elevations.length; i++){
            for (int j = 0; j < elevations[0].length; j++){
                avg += elevations[i][j];
                max = (elevations[i][j] > max) ? elevations[i][j] : max;
            }
        }
        System.out.println("Found max = " + max);
        avg /= elevations.length * elevations.length;
        float adjustment = 1.0f / max;
        for (int i = 0; i < elevations.length; i++){
            for (int j = 0; j < elevations[i].length; j++){
                elevations[i][j] = Math.abs(elevations[i][j] * adjustment);
            }
        }
    }

    public void flatten(float[][] elevations){

    }

    // TODO
    public float[][] applyNormalizedHalfSphere(float[][] elevations, int size, float intensity)
    {
        System.out.println("Applying half sphere!");
        float[][] temp = new float[size][size];
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                float val = (float)(Math.pow(size/2, 2) - Math.pow(i - size/2, 2) - Math.pow(j - size/2, 2));
                val = (val > 0) ? val : 0;
                temp[i][j] = (float)Math.sqrt(val);
            }
        }
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                temp[i][j] /= size*intensity;
            }
        }
        return NodeUtilities.mergeArrays(temp, elevations,1,3);
    }

    public float[][] getElevations(){
        return this.elevations;
    }
}
