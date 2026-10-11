package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RiftFeederBlockEntity extends BlockEntity {
    public static final int MAX_ESSENTIA = 200;
    public static final int MAX_RIFT_SIZE = 200;
    public static final int MAX_RANGE = 12;
    public static final int GROW_INTERVAL = 5;
    private int storedEssentia;
    private int ticks;

    public RiftFeederBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_FEEDER.get(), pos, state);
    }

    public int getStoredEssentia() {
        return storedEssentia;
    }

    public int addEssentia(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, MAX_ESSENTIA - storedEssentia);
        if (accepted > 0) {
            storedEssentia += accepted;
            setChanged();
            syncToClient();
        }
        return accepted;
    }

    public static int essentiaRequiredFor(int riftSize) {
        return (int) Math.sqrt(Math.max(0, riftSize));
    }

    public int getTicks() {
        return ticks;
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Essentia", storedEssentia);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        storedEssentia = input.getIntOr("Essentia", 0);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), ImpetusRift.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, RiftFeederBlockEntity feeder) {
        feeder.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (++ticks % 20 == 0) {
            ImpetusRift.LOGGER.debug("[rift_feeder] {} 骨架 tick，缓存源质={}", worldPosition, storedEssentia);
        }
        // TODO 引流仪逻辑：
        //  1. 从前端以外的任意一面抽取 Vitium 源质（IEssentiaTransport）
        //  2. 每 GROW_INTERVAL tick 在 MAX_RANGE 内沿 FACING 方向找最近的裂隙（需视线）
        //  3. 源质足够时消耗 essentiaRequiredFor(size) 点，令裂隙尺寸 +1（上限 MAX_RIFT_SIZE）
    }

    @Nullable
    public static RiftFeederBlockEntity at(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RiftFeederBlockEntity feeder ? feeder : null;
    }
}
