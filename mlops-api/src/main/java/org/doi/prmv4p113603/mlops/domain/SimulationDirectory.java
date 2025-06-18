package org.doi.prmv4p113603.mlops.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimulationDirectory {

    private final String path;
    private final SimulationArtifactScope scope;
    private final List<SimulationDirectory> children = new ArrayList<>();
    private int number = -1;
    private SimulationDirectory parent;

    public SimulationDirectory(String path, SimulationArtifactScope scope) {
        this.path = path;
        this.scope = scope;
    }

    public SimulationDirectory(String path, SimulationArtifactScope scope, int number) {
        this.path = path;
        this.scope = scope;
        this.number = number;
    }

    public void addChild(SimulationDirectory child) {
        child.setParent(this); // Set parent reference
        children.add(child); // Add to this directory's children
    }

}
