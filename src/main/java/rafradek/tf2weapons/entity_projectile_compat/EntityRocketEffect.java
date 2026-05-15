package rafradek.tf2weapons.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityRocketEffect extends Entity {
	public EntityRocketEffect(Level level, EntityRocket rocket) { super(EntityType.SNOWBALL, level); }
	@Override protected void defineSynchedData() {}
	@Override protected void readAdditionalSaveData(CompoundTag tag) {}
	@Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
