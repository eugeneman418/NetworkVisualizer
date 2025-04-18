package org.networkvisualizer.state;

import org.networkvisualizer.topology.NetworkParser;

public class Main {
    public static void main(String[] args) {
        String osmPath = args[0];
        String networkPath = args[1];

        try {
            NetworkParser.parseJson(networkPath);
        }
        catch (Exception e) {
            System.out.println(e);
        }

    }
}
