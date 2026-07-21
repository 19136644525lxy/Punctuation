package yifei.ah.tabs;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import yifei.ah.FormingAnArmyAlone;
import yifei.ah.item.ModItems;

public class ModCreativeTabs {
    public static final ItemGroup MOB_BOX_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModItems.MOB_BOX))
        .displayName(Text.translatable("itemGroup.ah.mob_box"))
        .entries((context, entries) -> {
            entries.add(ModItems.MOB_BOX);
        })
        .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(FormingAnArmyAlone.MOD_ID, "mob_box"), MOB_BOX_TAB);
    }
}