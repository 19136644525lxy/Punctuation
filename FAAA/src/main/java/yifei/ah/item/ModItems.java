package yifei.ah.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yifei.ah.FormingAnArmyAlone;

public class ModItems {
    public static final Item MOB_BOX = new MobBoxItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier(FormingAnArmyAlone.MOD_ID, "mob_box"), MOB_BOX);
    }
}