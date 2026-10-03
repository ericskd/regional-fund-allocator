package allocator.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import allocator.model.RegionData;

public class EqualStrategy implements AllocationStrategy {

	@Override
	public Map<String, Double> weights(List<RegionData> regions) {
	    if (regions.isEmpty()) {
	        throw new IllegalArgumentException("Need at least one region");
	    }
	    double share = 1.0 / regions.size();   // 1.0, not 1: int / int would round down to 0

	    Map<String, Double> weights = new LinkedHashMap<>();
	    for (RegionData r : regions) {
	        weights.put(r.region(), share);
	    }
	    return weights;
	}

	@Override
	public String name() {
	    return "Equal split";
	
	}

}
