package com.blackmoss.impetusrift.data.lang;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ZhCnProvider extends LanguageProvider {

    public ZhCnProvider(PackOutput output) {
        super(output, ImpetusRift.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.impetusrift", "元动裂隙");

        addBlock(IRBlocks.IMPETUS_DRAINER, "元动汲取仪");
        addBlock(IRBlocks.IMPETUS_DIFFUSER, "元动弥散仪");
        addBlock(IRBlocks.IMPETUS_GENERATOR, "元动发电机");
        addBlock(IRBlocks.IMPETUS_MATRIX, "元动矩阵");
        addBlock(IRBlocks.IMPETUS_ALTAR_BASE, "元动祭坛底座");
        addBlock(IRBlocks.IMPETUS_MIRROR, "元动之镜");
        addBlock(IRBlocks.IMPETUS_RELAY, "元动中继器");
        addBlock(IRBlocks.IMPETUS_GATE, "元动阀门");
        addBlock(IRBlocks.RIFT_FEEDER, "咒波引流仪");
        addBlock(IRBlocks.RIFT_MOVER_INPUT, "裂隙封装仪");
        addBlock(IRBlocks.RIFT_MOVER_OUTPUT, "裂隙释放仪");
        addBlock(IRBlocks.RIFT_JAR, "裂隙罐子");
        addBlock(IRBlocks.RIFT_MONITOR, "裂隙监测仪");
        addBlock(IRBlocks.RIFT_BARRIER, "裂隙屏障");

        addItem(IRItems.IMPETUS_CELL, "元动能量单元");
        addItem(IRItems.IMPETUS_GEM, "元动宝石");
        addItem(IRItems.RIFT_SEED_FLUX, "咒波裂隙种子");
        addItem(IRItems.RIFT_COMPASS, "裂隙指南针");
        addItem(IRItems.IMPETUS_LINKER, "元动链接器");

        addResearch("impetus_drainer", "元动汲取仪",
                "元动汲取仪是一种可以从附近源中汲取元动能量的装置。",
                "这个装置可以用来收集元动能量，供其他机器使用。它需要附近的元动源才能工作。");

        addResearch("rift_feeder", "裂隙释放仪",
                "裂隙释放仪是一种可以将物品送入裂隙的装置。",
                "这个装置可以用来自动将物品送入裂隙进行处理或存储。");

        jade();
        impetusLinker();
    }

    private void addResearch(String researchId, String title, String... stage) {
        add("research.%s.%s.title".formatted(ImpetusRift.MODID, researchId), title);
        for (int i = 0; i < stage.length; i++) {
            add("research.%s.%s.stage_%d".formatted(ImpetusRift.MODID, researchId, i), stage[i]);
        }
    }

    private void jade() {
        add("jade.impetusrift.input_nodes", "输入节点：%1$s / %2$s");
        add("jade.impetusrift.output_nodes", "输出节点：%1$s / %2$s");
        add("jade.impetusrift.energy", "元动能量 %1$s / %2$s");
        add("jade.impetusrift.disabled", "已禁用（红石关闭）");
        add("jade.impetusrift.draining", "汲取中");
        add("jade.impetusrift.rift_size", "裂隙大小 %1$s");
        add("jade.impetusrift.stability", "稳定度 %1$s");
        add("jade.impetusrift.collapsing", "裂隙正在坍缩");
        add("jade.impetusrift.no_rift", "未绑定裂隙");
        add("jade.impetusrift.nearby_rifts", "附近 %1$s 个裂隙");
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
        add("item.impetusrift.node.unknown", "未知节点");
    }
}