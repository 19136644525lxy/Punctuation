package yifei.ah;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yifei.ah.item.ModItems;
import yifei.ah.loot.MobBoxLootTableModifier;
import yifei.ah.manager.FriendlyMobBehavior;
import yifei.ah.manager.FriendlyMobManager;
import yifei.ah.network.PacketHandler;
import yifei.ah.tabs.ModCreativeTabs;
import yifei.pua.api.MarkerEvent;
import yifei.pua.api.PunctuationAPIAccess;

import java.util.UUID;

public class FormingAnArmyAlone implements ModInitializer {
    public static final String MOD_ID = "ah";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Alone Host mod initialized");
        
        ModItems.register();
        ModCreativeTabs.register();
        MobBoxLootTableModifier.register();
        
        FriendlyMobManager.init();
        PacketHandler.register();
        
        PunctuationAPIAccess.getInstance().onMarkerSet(this::onMarkerEvent);
        
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;
                if (FriendlyMobManager.isFriendly(mob) && player.getStackInHand(hand).isEmpty()) {
                    UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
                    if (ownerUUID != null && ownerUUID.equals(player.getUuid())) {
                        FriendlyMobManager.cycleBehavior(mob);
                        FriendlyMobBehavior behavior = FriendlyMobManager.getBehavior(mob);
                        player.sendMessage(Text.translatable("message.ah.behavior_changed", behavior.getDisplayName()).formatted(Formatting.GOLD), true);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private void onMarkerEvent(MarkerEvent event) {
        FormingAnArmyAlone.LOGGER.info("Marker event received: type={}, entity={}", event.getType(), event.getEntity());
        if (event.getType() == MarkerEvent.Type.ENTITY && event.getEntity() != null) {
            FormingAnArmyAlone.LOGGER.info("Sending marker entity packet for entity id: {}", event.getEntity().getId());
            PacketHandler.sendMarkerEntityPacket(event.getEntity().getId());
        }
    }
}