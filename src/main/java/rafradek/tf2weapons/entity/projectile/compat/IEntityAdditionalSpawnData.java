package rafradek.tf2weapons.entity.projectile.compat;

import io.netty.buffer.ByteBuf;

public interface IEntityAdditionalSpawnData {
	void writeSpawnData(ByteBuf buffer);

	void readSpawnData(ByteBuf buffer);
}
