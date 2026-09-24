package com.blackmoss.impetusrift.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ImpetusMatrixRenderState extends BlockEntityRenderState {
    public float animationTime;
    public float energyRatio;
    public float stability;
    public boolean active;
    public boolean unstable;
    public float startUp;
    public List<BlockPos> beamTargets = new ArrayList<>();
    public BlockPos blockPos;
}