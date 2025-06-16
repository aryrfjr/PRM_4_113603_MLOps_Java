package org.doi.prmv4p113603.mlops.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SimulationDirectory {

    private final String path;
    private final SimulationArtifactScope scope;
    private SimulationDirectory parent;
    private List<SimulationDirectory> children = new ArrayList<>();
    private final int number;

    public void addChild(SimulationDirectory child) {
        child.parent = this; // Set parent reference
        children.add(child); // Add to this directory's children
    }

}
