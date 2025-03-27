package org.example.routing;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.util.PMap;

public class CustomFlagEncoderFactory implements FlagEncoderFactory {
    static final String TRAIN = "train";
    static final String SHIP = "ship";

    @Override
    public FlagEncoder createFlagEncoder(String name, PMap configuration) {
        if (name.equals(CAR))
            return new CarFlagEncoder(configuration);
        if (name.equals(TRAIN))
            return new TrainFlagEncoder(configuration);
        if (name.equals(SHIP))
            return new ShipFlagEncoder(configuration);
        throw new IllegalArgumentException("entry in encoder list not supported: " + name);

    }
}
