package org.networkvisualizer.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.RoadsFlagEncoder;
import com.graphhopper.util.PMap;

import static com.graphhopper.routing.util.EncodingManager.getKey;

// there are issues with inheriting from RoadFlagEncoder, which causes GH to combine all 3 profiles when routing (e.g. train on water, barge on land). This seems to be a hacky fix
public class TrainFlagEncoder extends FootFlagEncoder {


    public TrainFlagEncoder(PMap properties) {
        super();
    }



    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        // certain segments of railway is classified as subway as it goes underground
        //if (way.hasTag("railway", "rail") || way.hasTag("railway", "subway") )
        if (way.hasTag("railway") && !way.hasTag("railway", "tram") )
            return EncodingManager.Access.WAY;
        return EncodingManager.Access.CAN_SKIP;

    }

    @Override
    public String getName() {
        return "train";
    }

}
