package rafradek.tf2weapons.entity;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntityStatue extends Entity implements IEntityAdditionalSpawnData {
	public LivingEntity entity;
	public CompoundTag data;
	public GameProfile profile;
	public boolean player;
	public boolean useHand;
	public boolean clientOnly;
	public boolean isFeign;
	public int ticksLeft = -1;

	public EntityStatue(Level level) {
		super(EntityType.MARKER, level);
	}

	public EntityStatue(Level level, LivingEntity toCopy) {
		this(level);
		this.entity = toCopy;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {}

	@Override
	public void writeSpawnData(ByteBuf buffer) {}

	@Override
	public void readSpawnData(ByteBuf additionalData) {}
}
