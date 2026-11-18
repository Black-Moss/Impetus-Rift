package com.blackmoss.impetusrift.data.lang;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRCreativeModeTabs;
import com.blackmoss.impetusrift.registry.IRItems;
import com.blackmoss.lazycat.lang.LazyCatLanguageProvider;
import net.minecraft.data.PackOutput;

public class ZhCnProvider extends LazyCatLanguageProvider {
    public ZhCnProvider(PackOutput output) {
        super(output, ImpetusRift.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        addCreativeModeTab(IRCreativeModeTabs.IMPETUS_RIFT.get(), "元动裂隙");

        addBlock(IRBlocks.IMPETUS_DRAINER, "元动汲取仪");
        addBlock(IRBlocks.IMPETUS_DIFFUSER, "元动弥散仪");
        addBlock(IRBlocks.IMPETUS_GENERATOR, "元动发电机");
        addBlock(IRBlocks.IMPETUS_MATRIX, "元动矩阵");
        addBlock(IRBlocks.IMPETUS_ALTAR_BASE, "元动祭坛底座");
        addBlock(IRBlocks.IMPETUS_MIRROR, "元动之镜");
        addBlock(IRBlocks.IMPETUS_RELAY, "元动中继器");
        addBlock(IRBlocks.IMPETUS_GATE, "元动阀门");
        addBlock(IRBlocks.FLUX_INDUCER, "咒波引流仪");
        addBlock(IRBlocks.METASPATIAL_ACCUMULATOR, "裂隙封装仪");
        addBlock(IRBlocks.METASPATIAL_EXTRUDER, "裂隙释放仪");
        addBlock(IRBlocks.RIFT_JAR, "裂隙罐子");
        addBlock(IRBlocks.EXTRADIMENSIONAL_MONITOR, "裂隙屈光仪");

        addItem(IRItems.IMPETUS_CELL, "元动能量单元");
        addItem(IRItems.IMPETUS_GEM, "元动宝石");
        addItem(IRItems.RIFT_SEED_FLUX, "咒波裂隙种子");
        addItem(IRItems.RIFT_COMPASS, "裂隙指南针");
        addItem(IRItems.IMPETUS_LINKER, "元动谐振器");
        addItem(IRItems.RIFT_TRAVELLER_BOOTS, "裂隙行者之靴");
        addItem(IRItems.THAUMOSTATIC_ELYTRA, "奥法鞘翅");
        addItem(IRItems.ELYTRA_THRUSTER, "鞘翅助推器");

        research();
        configuration();
        jade();
        tooltip();
        impetusLinker();
    }

    private void research() {
        addResearchCategory("impetuology", "元动学");
        addResearch("unlock_impetuology", "发现：元动学");
    }

    private void configuration() {
        addConfiguration("title", "元动裂隙配置文件");

        addConfiguration("thruster_super_boost_hold_time", "助推器超级加速长按时间");

        addConfiguration("thruster_super_boost_multiplier", "助推器超级加速倍率");
        addConfiguration("thruster_super_boost_min_speed", "助推器超级加速最小速度");
    }

    private void jade() {
        add("config.jade.plugin_impetusrift.impetus_node", "元动节点");
        add("config.jade.plugin_impetusrift.impetus_energy", "元动能量");
        add("jade.impetusrift.impetus_node.input_nodes", "输入节点：%1$s / %2$s");
        add("jade.impetusrift.impetus_node.output_nodes", "输出节点：%1$s / %2$s");
        add("jade.impetusrift.impetus_node.rift_size", "裂隙大小：%1$s");
        add("jade.impetusrift.impetus_node.stability", "稳定度：%1$s");
        add("jade.impetusrift.impetus_node.no_target", "未监测到裂隙");
        add("jade.impetusrift.impetus_node.gate_enabled", "状态：导通");
        add("jade.impetusrift.impetus_node.gate_disabled", "状态：阻断");
        add("jade.impetusrift.impetus_node.monitor_mode_size", "模式：尺寸");
        add("jade.impetusrift.impetus_node.monitor_mode_stability", "模式：稳定度");
        add("jade.impetusrift.impetus_energy.energy", "元动能量：%1$s / %2$s");
    }

    private void tooltip() {
        addTooltip("unknown", "未知节点");
        addTooltip("impetus_energy", "元动能量：%s / %s");
        addTooltip("charge", "元动能量：%s / %s");
        addTooltip("thruster", "助推器燃料：%s / %s");
    }

    // 元动谐振器
    private void impetusLinker() {
        add("item.impetusrift.impetus_linker.too_far", "距离超出限制（最大 %1$s 格）");
        add("item.impetusrift.impetus_linker.blocked", "连接被不透明方块阻挡");
        add("item.impetusrift.impetus_linker.no_source", "源节点不存在");
        add("item.impetusrift.impetus_linker.no_target", "目标节点不存在");
        add("item.impetusrift.impetus_linker.cannot_output", "源节点（%1$s）不能输出");
        add("item.impetusrift.impetus_linker.output_full", "源节点输出端口已满（%1$s/%2$s）");
        add("item.impetusrift.impetus_linker.cannot_input", "目标节点（%1$s）不能输入");
        add("item.impetusrift.impetus_linker.input_full", "目标节点输入端口已满（%1$s/%2$s）");
        add("item.impetusrift.impetus_linker.disconnected", "已断开连接：%1$s ✕ %2$s");
        add("item.impetusrift.impetus_linker.connected", "已建立连接：%1$s → %2$s");
        add("item.impetusrift.impetus_linker.failed", "连接失败（未知原因）");

        add("item.impetusrift.impetus_linker.mirror_bound", "已绑定元动之镜");
        add("item.impetusrift.impetus_linker.mirror_unbound", "已解除元动之镜的绑定");
        add("item.impetusrift.impetus_linker.mirror_need_pair", "需要两面元动之镜才能绑定");
        add("item.impetusrift.impetus_linker.mirror_select_source", "请先潜行右键一面元动之镜作为起点");
        add("item.impetusrift.impetus_linker.mirror_self", "不能把镜子绑定到自己身上");
        add("item.impetusrift.impetus_linker.mirror_target_busy", "这面镜子已经绑定了另一面镜子");
        add("item.impetusrift.impetus_linker.mirror_source_busy", "起点镜子已经绑定了另一面镜子");
    }
}
