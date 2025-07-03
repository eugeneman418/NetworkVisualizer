package org.networkvisualizer.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.RoadsFlagEncoder;
import com.graphhopper.util.PMap;

// there are issues with inheriting from RoadFlagEncoder, which causes GH to combine all 3 profiles when routing (e.g. train on water, barge on land). This seems to be a hacky fix
public class BargeFlagEncoder extends BikeFlagEncoder {

    public BargeFlagEncoder(PMap properties) {
        super();
    }



    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        //System.out.println("Calling barge");
        if (way.hasTag("waterway", "river") || way.hasTag("waterway", "canal"))
            return EncodingManager.Access.WAY;
        return EncodingManager.Access.CAN_SKIP;

    }

    @Override
    public String getName() {
        return "barge";
    }

}
