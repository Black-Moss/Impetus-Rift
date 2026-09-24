package com.blackmoss.impetusrift.energy.network;

public record ImpetusNodePorts(int inputs, int outputs) {
    public static final ImpetusNodePorts NONE = new ImpetusNodePorts(0, 0);

    public static ImpetusNodePorts of(int inputs, int outputs) {
        return new ImpetusNodePorts(inputs, outputs);
    }

    public boolean canInput() {
        return inputs > 0;
    }

    public boolean canOutput() {
        return outputs > 0;
    }

    public boolean isBidirectional() {
        return canInput() && canOutput();
    }
}
