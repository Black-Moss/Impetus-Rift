# Impulse-Rift 项目记忆

## 项目概述
- **名称**: Impulse-Rift (元动裂隙) — Minecraft NeoForge 26.1.2 模组
- **目的**: 将 1.12.2 神秘进阶 (Thaumic Augmentation) 的元动+裂隙内容移植到 Thaumaturge
- **依赖**: Thaumaturge (核心)、TerraBlender、Curios API
- **参考源码**: https://github.com/TheCodex6824/ThaumicAugmentation

## 关键开发规则
1. **NeoForge 26 API**: `ValueOutput/ValueInput` 替代 `CompoundTag`；`Identifier` 替代 `ResourceLocation`；`RenderType` 在 `net.minecraft.client.renderer.rendertype` 包
2. **方块实体**: 使用 `MapCodec` 注册；`getAnalogOutputSignal` 需要 `Direction` 参数
3. **自定义模型**: Blockbench 模型**必须加 `"parent": "block/block"`**，否则物品栏扁平；OBJ 模型需显式写 `display`
4. **复用外部方块类**: `newBlockEntity()` 返回的 BE 绑定外部 `BlockEntityType`，其 `validBlocks` 不含本模组方块 → 必须自建方块类 + 自己的 `BlockEntityType`
5. **无物品方块**: 用 `generateBlockStateOnly()` + `dropOther` + 不注册 `BlockItem`
6. **多方块**: 矩阵上下需基座（下方 `facing=up`，上方 `facing=down`）；虚空基座无朝向
7. **客户端同步**: `SavedData` 只在 `ServerLevel` 存在；需 `CustomPacketPayload` + 客户端缓存
8. **渲染**: BER 用 `SubmitNodeCollector.submitCustomGeometry()`；管道：`SPARKLE_CULLED`(纯色)、`ENTITY_ADDITIVE_EMISSIVE`(纹理发光)；NeoForge 26 无线条渲染，用薄四边形替代
8.1 **BER 坐标系（关键）**: `submit()` 的 poseStack **原点是方块最小角**（局部坐标系），顶点必须用相对坐标（方块中心=`(0.5,0.5,0.5)`，其他方块=`Vec3.atCenterOf(target).subtract(Vec3.atLowerCornerOf(selfPos))`）；用世界坐标会导致渲染偏移到方块外
8.2 **BER 视锥剔除**: 渲染超出方块范围的内容（光束/连线）必须重写 `getRenderBoundingBox(T)` 返回覆盖范围（参考 Thaumaturge `VisRelayRenderer` 的 `self.minmax(new AABB(target))`），否则被剔除不可见
8.3 **光束渲染**: 用面向相机的 billboard（`right = beamDir × toCamera`）而非轴向三棱柱几何，避免倾斜/变薄问题；渲染正反两面防剔除
8.4 **光束 UV**: `impetus_beam.png` 条纹沿纹理 U 轴 → **U 映射光束长度方向**、V 映射宽度方向(0-1)；写反条纹会横跨光束
8.5 **光束显示条件**: 检查客户端 target BE 的 `canReceive()`（目标满电则不显示光束）；source 用 `getEnergyStored() > 0 || hasRecentEnergy()` 判断（避免抽取间隔较长时 buffer 被瞬间传空导致光束闪烁）
8.6 **抽取/产能间隔 vs 传输速度**: 传输(100/tick) 远快于抽取间隔(10tick) 时 buffer 会被瞬间清空 → 必须用 `hasRecentEnergy()` 时间窗口（记录 `lastEnergyGameTime` = `level.getGameTime()`，save/load 同步）让光束持续可见；汲取仪 `EXTRACT_INTERVAL=10`、发电机每 10tick 产能，均适用
8.7 **光束淡入淡出（视觉状态一律放渲染层，不要放 BE）**: 状态放 BER（`ImpetusEnergyBeamRenderer.FADE_STATES` Map + `updateFade()`），用 `System.currentTimeMillis()` 真实时间增量驱动（不受帧率影响）；**BE 只提供 `lastEnergyGameTime` + `hasRecentEnergy()` 数据**。淡入 `0.03`(1.6s)、淡出 `0.05`(1s)。
  - **不要**在 BE 加 `clientBeamFade`，**不要**为此让 BE 客户端 tick（`getTicker` 保持服务端 only）
  - **淡出触发**：`active = hasEnergy && 至少一个目标 canReceive()`（只看 `hasRecentEnergy()` 在持续传输时恒 true，永不淡出）
  - `renderBeams` 里"跳过满电目标"的 `continue` **必须加 `fade >= 1.0F` 条件**，否则淡出过程被跳过看不到
8.8 **光束呼吸**: `w = BEAM_WIDTH * fade * breathe`，`breathe = 1 + BREATHE_AMPLITUDE * sin(time * BREATHE_SPEED)`（0.15 / 0.15 → ±15% 脉动、周期约 2 秒）
9. **模型**: OBJ 用 `neoforge:obj` 加载器 + JSON 包装；MTL 的 `map_Kd` 引用 `#texture0`
10. **Gradle 缓存**: 出错时用 `--no-daemon --no-configuration-cache`
11. **音效**: `SoundEvents.GENERIC_EXPLODE` 是 `Holder.Reference`，需 `.value()` 解包
12. **指南针物品**: `RangeSelectItemModelProperty` + `NeedleDirectionHelper`
13. **分层原则（重要）**: 领域模型（枚举 / BE / 接口）只放**事实数据**；表现层（颜色 `ChatFormatting`、显示名、动画状态如 fade）一律放 client render 或显示 provider（如 Jade provider）。不要在领域枚举里塞颜色，不要在 BE 里加 `client*` 动画字段
13.1 **节点能力全部由节点自述，不用类型枚举**: `ImpetusNodeType` 枚举**已删除**。节点通过 `IImpetusNode` 自述：
  - `getPorts()` → 端口数量与方向（`ImpetusNodePorts` 记录）：汲取仪 `of(0,2)`、弥散仪 `of(2,0)`、发电机 `of(0,1)`、矩阵 `of(1,1)`
  - `getNodeLabel()` → 显示名 `Component`，**默认取方块自身名字**（`block.getDescriptionId()` 的本地化，如"元动汲取仪"），节点一般无需重写；取不到时回退 `impulserift.node.unknown`
  - `isBulkStorage()` → 是否大容量存储（矩阵 true，用于矩阵↔矩阵限速）
  - 新增一种机器只需实现 `IImpetusNode`，不必改任何枚举/类型表
13.2 **枚举常量初始化顺序坑**: 枚举常量必须声明在最前，静态字段在其后才初始化 → 作为枚举常量构造参数的常量**不能**是枚举内的 `static final` 字段（构造时读到默认值 0）。用**接口常量**（编译期内联）规避
14. **NeoForge 26 能量（FE）API**: 用 `transfer.energy.EnergyHandler`（非旧 `energy.IEnergyStorage`，后者是适配层）。
  - 查询：`level.getCapability(Capabilities.Energy.BLOCK, targetPos, side)`
  - 插入必须走事务：`try (Transaction ctx = Transaction.openRoot()) { int p = h.insert(amt, ctx); if (p>0) ctx.commit(); }`
  - 注册：`@EventBusSubscriber` 的 `RegisterCapabilitiesEvent` + `event.registerBlockEntity(Capabilities.Energy.BLOCK, beType, (be, side) -> be)`
  - 元动发电机只在方块 `FACING` 面输出 FE：`outputFace()` + 能力注册 `side == be.outputFace() ? be : null`；
    blockstate 用现成的 `BlockModelGenerators.ROTATION_FACING` 生成 6 向旋转（别自己写 switch）
  - 参考：Thaumaturge `BlockEntityVisGenerator` / `TCCapabilities`
15. **本地化约定（不要硬编码中文/英文）**: 所有玩家可见文本用 `Component.translatable` + 语言 key，key 加到 `data/lang/{EnUsProvider,ZhCnProvider}`，改完跑 `gradlew runData` 重新生成 `../../src/generated/resources/assets/impetusrift`。
  - Jade HUD 前缀：`jade.impulserift.`（如 `input_nodes`、`energy`）
  - 物品提示前缀：`item.impulserift.<物品名>.`（如 `item.impulserift.impetus_linker.too_far`）
  - 带参数用 `%1$s`、`%2$s`；颜色用 `ChatFormatting` 而非 `§` 硬编码

## 已注册方块
### 元动系统 (有BE: *标注)
IMPETUS_DRAINER*, IMPETUS_DIFFUSER*, IMPETUS_GENERATOR*, IMPETUS_MATRIX*, IMPETUS_MATRIX_BASE, IMPETUS_MIRROR, IMPETUS_RELAY, IMPETUS_GATE, IMPETUS_ALTAR_BASE
### 裂隙系统
RIFT_FEEDER, RIFT_MOVER_INPUT, RIFT_MOVER_OUTPUT, RIFT_JAR*, RIFT_MONITOR, RIFT_BARRIER
### 物品
IMPETUS_ENERGY_CELL, IMPETUS_GEM, RIFT_SEED_FLUX, RIFT_LOCATOR, IMPETUS_LINKER, IMPETUS_ENERGY_UNIT, VOID_PEDESTAL

## 元动能量系统架构
- **网络管理器**: `ImpetusNetworkManager` (SavedData)，管理连接和传输
- **节点接口**: `IImpetusNode`（**无节点类型枚举**）；端口 `getPorts()`、显示名 `getNodeLabel()`、大容量存储 `isBulkStorage()` 均由节点自述
- **连接**: 需谐振器绑定，最大8格曼哈顿距离，需视线
- **渲染**: `ImpetusNodeRenderer`(通用BER) + `ImpetusEnergyBeamRenderer`(光束)
- **客户端缓存**: `ClientConnectionCache` + `ClientboundConnectionsSyncPayload`

## Thaumaturge EntityFluxRift API（关键）
- `rift.points` — `List<Vec3>` 局部坐标视觉点（用于渲染+碰撞+视线检测）
- `rift.getRiftSize()` / `rift.setRiftSize()` — 裂隙大小
- `rift.setRiftStability()` — 设置稳定性
- `rift.getCollapse()` — `boolean`（是否坍缩中）
- `rift.hurtTouching()` — 每tick自动对附近实体造成15点魔法伤害（内置，无需实现）
- Sources jar: `C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\com.leclowndu93150\thaumaturge\0.1.4\*\thaumaturge-0.1.4-sources.jar`

## 重要 Bug 修复记录
1. **汲取仪 maxReceive=0**: `ImpetusEnergyStorage(BUFFER_CAPACITY, 0, MAX_TRANSFER)` 导致 `addEnergy()` 返回 0 → 改为 `MAX_TRANSFER`
2. **能量束渲染**: `ImpetusNodeRenderer` 需检查 `hasEnergy` + `beamTargets` 非空
3. **汲取仪能量不流动**: `extractEnergyFromRift()` 只在 `findRift()` (每60tick) 中调用 → 分离：搜索60tick，抽取每tick
4. **视线检测点不一致**: 用自己生成的点 vs 实体实际 `points` 字段 → 改用 `rift.points`
5. **矩阵没存（装不了能量单元）**: `ImpetusAltarBaseBlock` 只实现 `useWithoutItem`（空手）没实现 `useItemOn`（手持物品）→ 装单元（手持 ImpetusCellItem 右键）永远不触发 → 矩阵容量恒 0。修复：添加 `useItemOn` + 客户端返回 `SUCCESS`
6. **能量束不显示**: source 节点（Drainer/Generator）`onEnergyChanged()` 只 `setChanged()` 没 `syncToClient()` → 客户端 energy 恒 0 → `hasEnergy` 恒 false。修复：`onEnergyChanged()` 加 `syncToClient()`
7. **谐振器框架重启不显示**: `ClientConnectionCache` 客户端缓存只在连接变化时同步，登录时不同步 → 修复：`NetworkTickHandler` 订阅 `PlayerEvent.PlayerLoggedInEvent` 登录时发 `ClientboundConnectionsSyncPayload`

## 能量束渲染 / 谐振器交互要点
- 能量束显示条件：`getBeamTargetPositions()` 非空 + `getEnergyStored() > 0`，所以 source 节点能量必须 `syncToClient()`
- `ImpetusEnergyStorage.receiveEnergy()` 内部调用的 `onEnergyChanged()` 是 `IImpetusEnergyStorage` 的空 default 方法，**不是** BlockEntity 的 `IImpetusNode.onEnergyChanged()`（网络传输里 processConnection 手动调用后者）
- 谐振器交互三步：普通右键切换高亮节点（显示连接）、Shift 选源、选源后右键连目标；高亮节点存 `LINKER_HIGHLIGHT_NODE` DataComponent
- `ServerPlayer` 无 `serverLevel()`，用 `player.level()`

## NeoForge 26 方块交互 API
- `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)` — 空手右键
- `useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)` — 手持物品右键（对应旧版 `use`）
- **装物品类交互必须实现 `useItemOn`**；客户端返回 `InteractionResult.SUCCESS`（不是 PASS），服务端才正确执行+同步
- 参考 Thaumaturge `BlockRechargePedestal`：客户端 `if (level.isClientSide()) return SUCCESS;`，服务端执行实际逻辑

## Jade 集成（诊断 HUD）
- **依赖**: `maven.modrinth:nvQzSEkH:5jKeKXwB`（Jade 26.1.x+neoforge），mod id = `jade`
- **插件入口**: `compat/jade/ImpulseRiftJadePlugin.java`，`@WailaPlugin(ImpulseRift.MODID)` 注解自动被 Jade 扫描发现
- **provider 必须分离（MC 1.21.6+ 强制）**：
  - `compat/jade/ImpetusNodeDataProvider.java` — 服务端，只实现 `IServerDataProvider<BlockAccessor>`（写数据）
  - `compat/jade/ImpetusNodeClientProvider.java` — 客户端，只实现 `IBlockComponentProvider`（读数据 + 显示）
  - 两者 `getUid()` 返回相同 Identifier 关联
- **关键 API**:
  - 服务端注册: `registerBlockDataProvider(provider, BlockEntity.class)`
  - 客户端注册: `registerBlockComponent(provider, Block.class)`
  - `appendServerData(CompoundTag, BlockAccessor)` / `appendTooltip(ITooltip, BlockAccessor, IPluginConfig)`
  - `accessor.getServerData()` 返回 `CompoundTag`，用 `getBooleanOr/getIntOr/getLongOr/getFloatOr/getStringOr` 读
  - `getUid()` 返回 `net.minecraft.resources.Identifier`
- **绑定节点数**: `ImpetusNetworkManager.get(serverLevel).getConnectionsAt(pos).size()`
- **neoforge.mods.toml** 已声明 `jade` optional 依赖 `[26,)`
- **注意**: `Component` 接口无 `append()`，链式 append 前的方法返回类型须为 `MutableComponent`
- **注意**: `IServerDataProvider` 不能同时实现 `IComponentProvider`，否则运行时抛 `IllegalArgumentException`（MC 1.21.6+）
