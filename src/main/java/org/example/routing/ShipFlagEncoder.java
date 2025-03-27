package org.example.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.RoadsFlagEncoder;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

public class ShipFlagEncoder extends RoadsFlagEncoder {

    public ShipFlagEncoder(PMap properties) {
        super();
    }



    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        if (way.hasTag("waterway", "river") || way.hasTag("waterway", "canal"))
            return EncodingManager.Access.WAY;
        return EncodingManager.Access.CAN_SKIP;

    }

    @Override
    public String getName() {
        return "ship";
    }

}
