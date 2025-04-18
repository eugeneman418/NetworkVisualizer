package org.networkvisualizer.topology.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

public class Router {
    GraphHopper gh;
    public Router(String ghLoc) {
        gh = new GraphHopper();
        gh.setFlagEncoderFactory(new CustomFlagEncoderFactory());

        gh.setOSMFile(ghLoc);

        gh.setGraphHopperLocation("target/routing-graph-cache");

        gh.setProfiles(
                new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false),
                new Profile("train").setVehicle("train").setWeighting("shortest").setTurnCosts(false),
                new Profile("barge").setVehicle("barge").setWeighting("fastest").setTurnCosts(false)
        );

        gh.getCHPreparationHandler().setCHProfiles(new CHProfile("car"), new CHProfile("train"),new CHProfile("barge"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        gh.importOrLoad();
    }

    public ResponsePath routeTruck(double fromLat, double fromLon, double toLat, double toLon) {
        return routeHelper(fromLat, fromLon, toLat, toLon, "car");
    }
    public ResponsePath routeTrain(double fromLat, double fromLon, double toLat, double toLon) {
        return routeHelper(fromLat, fromLon, toLat, toLon, "train");
    }
    public ResponsePath routeBarge(double fromLat, double fromLon, double toLat, double toLon) {
        return routeHelper(fromLat, fromLon, toLat, toLon, "barge");
    }
    private ResponsePath routeHelper(double fromLat, double fromLon, double toLat, double toLon, String mode) {
        GHRequest request = new GHRequest(fromLat, fromLon, toLat, toLon).setProfile(mode);
        GHResponse response = gh.route(request);
        return response.getBest();
    }
}
