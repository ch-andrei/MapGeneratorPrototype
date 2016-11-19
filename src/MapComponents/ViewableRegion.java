package MapComponents;

import java.util.List;

/**
 * Created by Andrei-ch on 2016-11-19.
 */
public interface ViewableRegion {
    public List<Node> getViewableNodes();
    public int getMinimumElevation();
    public int getMaximumElevation();
    public int getAverageElevation();
    public int getWaterLevel();
    public int computeMaximumElevation();
    public int getViewableSize();
    public long getViewableSeed();
}
