package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaTransfer;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class RiftFeederBlockEntity extends BlockEntity implements IEssentiaTransport, IEssentiaStorage {
    public static final int MAX_ESSENTIA = 250;
    public static final int MAX_RIFT_SIZE = EntityFluxRift.MAX_RIFT_SIZE;
    public static final int MAX_RANGE = 12;
    public static final int GROW_INTERVAL = 5;
    private static final int SUCTION = 128;

    private int storedEssentia;
    private int ticks;

    public RiftFeederBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_FEEDER.get(), pos, state);
    }

    public static int essentiaRequiredFor(int riftSize) {
        return (int) Math.sqrt(Math.max(0, riftSize));
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, RiftFeederBlockEntity feeder) {
        feeder.tick();
    }

    @Nullable
    public static RiftFeederBlockEntity at(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RiftFeederBlockEntity feeder ? feeder : null;
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

    public int getTicks() {
        return ticks;
    }

    @Nullable
    public EntityFluxRift findClosestRift(Direction face) {
        if (level == null) {
            return null;
        }
        Vec3 center = Vec3.atCenterOf(worldPosition);
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());

        List<EntityFluxRift> rifts = level.getEntitiesOfClass(
                EntityFluxRift.class, new AABB(worldPosition).inflate(MAX_RANGE));
        if (rifts.isEmpty()) {
            return null;
        }
        rifts.sort(Comparator.comparingDouble(r -> r.position().distanceToSqr(center)));

        for (EntityFluxRift candidate : rifts) {
            if (candidate.position().subtract(center).dot(normal) <= 0.0D) {
                continue;
            }
            Vec3 start = center.add(normal.scale(0.5D));
            BlockHitResult hit = level.clip(new ClipContext(start, candidate.position(),
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == BlockHitResult.Type.MISS) {
                return candidate;
            }
            if (hit.getLocation().distanceToSqr(center) > candidate.position().distanceToSqr(center)) {
                return candidate;
            }
        }
        return null;
    }

    private void spawnFeedParticles(EntityFluxRift rift) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 from = Vec3.atCenterOf(worldPosition);
        Vec3 to = rift.position();
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 0.01D) {
            return;
        }
        int steps = (int) Math.min(24, length * 2);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.add(dir.scale(i / (double) steps));
            server.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    @Nullable
    private Holder<IAspect> vitium() {
        if (level == null) {
            return null;
        }
        return level.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(TTAspects.VITIUM)
                .orElse(null);
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side != getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return isConnectable(side);
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int amount) {
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction side) {
        return vitium();
    }

    @Override
    public int getSuctionAmount(Direction side) {
        return isConnectable(side) && storedEssentia < MAX_ESSENTIA ? SUCTION : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int amount, Direction side) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int amount, Direction side) {
        if (amount <= 0 || !canInputFrom(side)) {
            return 0;
        }
        Holder<IAspect> held = vitium();
        if (held == null || !held.equals(type)) {
            return 0;
        }
        return addEssentia(amount);
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction side) {
        return isConnectable(side) ? vitium() : null;
    }

    @Override
    public int getEssentiaAmount(Direction side) {
        return isConnectable(side) ? storedEssentia : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> type, Direction side) {
        return Math.max(0, MAX_ESSENTIA - storedEssentia);
    }

    @Override
    public @NonNull AspectList contents() {
        Holder<IAspect> held = vitium();
        if (held == null || storedEssentia <= 0) {
            return AspectList.EMPTY;
        }
        return AspectList.of(new AspectInstance(held, storedEssentia));
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, @Nullable TransactionContext transaction) {
        if (amount <= 0) {
            return 0;
        }
        Holder<IAspect> held = vitium();
        if (held == null || !held.equals(aspect)) {
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

    @Override
    public int extract(Holder<IAspect> aspect, int amount, @Nullable TransactionContext transaction) {
        return 0;
    }

    @Override
    public long contentRevision() {
        return storedEssentia;
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

    private void pullEssentia() {
        if (level == null || storedEssentia >= MAX_ESSENTIA) {
            return;
        }
        Holder<IAspect> held = vitium();
        if (held == null) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        for (Direction side : Direction.values()) {
            if (side == facing || storedEssentia >= MAX_ESSENTIA) {
                continue;
            }
            BlockPos neighbor = worldPosition.relative(side);
            try {
                IEssentiaTransport source = level.getCapability(
                        EssentiaCapabilities.TRANSPORT, neighbor, side.getOpposite());
                if (source == null) {
                    continue;
                }
                int moved = EssentiaTransfer.transfer(source, side.getOpposite(), this, side, held, 1, false);
                if (moved > 0) {
                    storedEssentia += moved;
                    setChanged();
                    syncToClient();
                }
            } catch (Exception e) {
                ImpetusRift.LOGGER.warn("[rift_feeder] {} error while extracting essentia from {}: {}", worldPosition, neighbor, e.toString());
            }
        }
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (level.hasNeighborSignal(worldPosition)) {
            return;
        }
        pullEssentia();

        if (++ticks % GROW_INTERVAL != 0) {
            return;
        }
        if (storedEssentia <= 0) {
            return;
        }

        Direction face = getBlockState().getValue(BlockStateProperties.FACING);
        EntityFluxRift rift = findClosestRift(face);
        if (rift == null || rift.getCollapse() || rift.getRiftSize() >= MAX_RIFT_SIZE) {
            return;
        }

        int required = essentiaRequiredFor(rift.getRiftSize());
        if (storedEssentia < required) {
            return;
        }

        storedEssentia -= required;
        rift.setRiftSize(rift.getRiftSize() + 1);
        rift.addStability();
        spawnFeedParticles(rift);
        setChanged();
        syncToClient();
    }
}
