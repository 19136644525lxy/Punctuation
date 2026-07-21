package yifei.ah.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yifei.ah.manager.FriendlyMobManager;
import yifei.ah.network.PacketHandler;

import java.util.List;
import java.util.UUID;

public class MobBoxItem extends Item {
    public static final int MAX_CAPACITY = 10;

    public MobBoxItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private NbtList getStoredList(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return new NbtList();

        if (nbt.contains("StoredEntity", 10)) {
            NbtList list = new NbtList();
            list.add(nbt.getCompound("StoredEntity"));
            nbt.put("StoredEntities", list);
            nbt.remove("StoredEntity");
            nbt.remove("EntityId");
        }

        return nbt.getList("StoredEntities", 10);
    }

    private int getStoredCount(ItemStack stack) {
        return getStoredList(stack).size();
    }

    private void updateStoredList(ItemStack stack, NbtList list) {
        if (list.isEmpty()) {
            stack.removeSubNbt("StoredEntities");
        } else {
            stack.getOrCreateNbt().put("StoredEntities", list);
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        World world = player.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        if (!(entity instanceof MobEntity)) return ActionResult.PASS;

        MobEntity mob = (MobEntity) entity;

        if (!FriendlyMobManager.isFriendly(mob)) {
            if (isHostile(mob)) {
                PacketHandler.sendMarkerEntityPacket(entity.getId());
                return ActionResult.CONSUME;
            }
            player.sendMessage(Text.translatable("message.ah.not_friendly").formatted(Formatting.RED), true);
            return ActionResult.PASS;
        }

        UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
        if (ownerUUID == null || !ownerUUID.equals(player.getUuid())) {
            player.sendMessage(Text.translatable("message.ah.not_your_mob").formatted(Formatting.RED), true);
            return ActionResult.PASS;
        }

        ItemStack heldStack = player.getStackInHand(hand);
        
        NbtList storedList = getStoredList(heldStack);
        if (storedList.size() >= MAX_CAPACITY) {
            player.sendMessage(Text.translatable("message.ah.cage_full").formatted(Formatting.RED), true);
            return ActionResult.PASS;
        }

        NbtCompound entityTag = new NbtCompound();
        mob.saveNbt(entityTag);

        storedList.add(entityTag);
        updateStoredList(heldStack, storedList);
        
        player.currentScreenHandler.sendContentUpdates();
        
        FriendlyMobManager.removeFriendly(mob);
        mob.discard();

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 1.0f, 1.2f);
        
        yifei.ah.FormingAnArmyAlone.LOGGER.info("Stored mob: {} ({}), total stored: {}", 
                mob.getName().getString(), entityTag.getString("id"), storedList.size());

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null || !player.isSneaking()) return ActionResult.PASS;

        Hand hand = context.getHand();
        ItemStack heldStack = player.getStackInHand(hand);
        NbtList storedList = getStoredList(heldStack);
        if (storedList.isEmpty()) {
            player.sendMessage(Text.translatable("message.ah.empty_box").formatted(Formatting.RED), true);
            return ActionResult.PASS;
        }

        BlockPos hitPos = context.getBlockPos().offset(context.getSide());
        double spawnX = hitPos.getX() + 0.5;
        double spawnY = hitPos.getY();
        double spawnZ = hitPos.getZ() + 0.5;

        int lastIndex = storedList.size() - 1;
        NbtCompound entityTag = storedList.getCompound(lastIndex);
        storedList.remove(lastIndex);

        String entityId = entityTag.getString("id");
        EntityType<?> entityType = EntityType.get(entityId).orElse(null);
        if (entityType == null) {
            updateStoredList(heldStack, storedList);
            player.currentScreenHandler.sendContentUpdates();
            return ActionResult.FAIL;
        }

        Entity entity = entityType.create(world);
        if (entity != null) {
            entityTag.remove("UUID");
            entity.readNbt(entityTag);
            entity.refreshPositionAndAngles(spawnX, spawnY, spawnZ, entity.getYaw(), entity.getPitch());
            world.spawnEntity(entity);

            if (entity instanceof MobEntity) {
                FriendlyMobManager.makeFriendly((MobEntity) entity, player);
            }

            world.playSound(null, spawnX, spawnY, spawnZ,
                    SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        updateStoredList(heldStack, storedList);
        player.currentScreenHandler.sendContentUpdates();
        return ActionResult.SUCCESS;
    }

    private boolean isHostile(MobEntity mob) {
        if (mob instanceof HostileEntity) return true;
        if (mob instanceof SlimeEntity) return true;
        if (mob instanceof MagmaCubeEntity) return true;
        if (mob instanceof ShulkerEntity) return true;
        if (mob instanceof GuardianEntity) return true;
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        int count = getStoredCount(stack);
        tooltip.add(Text.literal(String.format("容量：%d / %d", count, MAX_CAPACITY))
                .formatted(Formatting.GRAY));

        if (count > 0) {
            NbtList list = getStoredList(stack);
            int showMax = Math.min(count, 3);
            for (int i = 0; i < showMax; i++) {
                NbtCompound tag = list.getCompound(i);
                String id = tag.getString("id");
                EntityType.get(id).ifPresent(type ->
                        tooltip.add(Text.literal(" • ").append(type.getName())
                                .formatted(Formatting.DARK_GRAY))
                );
            }
            if (count > 3) {
                tooltip.add(Text.literal(String.format("  等 %d 只生物", count))
                        .formatted(Formatting.DARK_GRAY));
            }
        }
    }
}