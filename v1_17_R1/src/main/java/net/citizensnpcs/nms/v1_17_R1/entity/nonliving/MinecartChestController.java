package net.citizensnpcs.nms.v1_17_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMinecartChest;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_17_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_17_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_17_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_17_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MinecartChestController extends MobEntityController {
    public MinecartChestController() {
        super(EntityMinecartChestNPC.class);
    }

    @Override
    public Minecart getBukkitEntity() {
        return (Minecart) super.getBukkitEntity();
    }

    public static class EntityMinecartChestNPC extends MinecartChest implements NPCHolder {
        private final CitizensNPC npc;

        public EntityMinecartChestNPC(EntityType<? extends MinecartChest> types, Level level) {
            this(types, level, null);
        }

        public EntityMinecartChestNPC(EntityType<? extends MinecartChest> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new MinecartChestNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        protected AABB makeBoundingBox() {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox());
        }

        @Override
        public void push(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.push(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void push(Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.push(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public void tick() {
            super.tick();
            if (npc != null) {
                npc.update();
                NMSImpl.minecartItemLogic(this);
            }
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> Tag, double d0) {
            if (npc == null) {
                return super.updateFluidHeightAndDoFluidPushing(Tag, d0);
            }
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(Tag, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }

    public static class MinecartChestNPC extends CraftMinecartChest implements ForwardingNPCHolder {
        public MinecartChestNPC(EntityMinecartChestNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
