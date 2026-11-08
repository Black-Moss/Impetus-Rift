package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class IRItemTags {
    public static final TagKey<Item> GLIDERS = TagKey.create(Registries.ITEM, ImpetusRift.identifier("gliders"));
}
