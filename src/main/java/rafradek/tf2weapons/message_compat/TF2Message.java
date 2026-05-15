package rafradek.tf2weapons.message;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.config.Configuration;

public abstract class TF2Message implements TF2Packet {
	public int entity;
	public int entityID;
	public int action;
	public int slot;
	public int value;
	public int gui;
	public int type;
	public int count;
	public int target;
	public boolean flag;
	public String name = "";
	public byte[] data;
	public CompoundTag tag = new CompoundTag();
	public ItemStack stack = ItemStack.EMPTY;
	public InteractionHand hand = InteractionHand.MAIN_HAND;
	public Vec3 vector = Vec3.ZERO;

	protected TF2Message(Object... args) {}

	public static class ActionMessage extends TF2Message { public ActionMessage(Object... args) { super(args); } }
	public static class DisguiseMessage extends TF2Message { public DisguiseMessage(Object... args) { super(args); } }
	public static class UseMessage extends TF2Message { public UseMessage(Object... args) { super(args); } }
	public static class PredictionMessage extends TF2Message { public PredictionMessage(Object... args) { super(args); } }
	public static class PropertyMessage extends TF2Message { public PropertyMessage(Object... args) { super(args); } }
	public static class CapabilityMessage extends TF2Message { public CapabilityMessage(Object... args) { super(args); } }
	public static class GameArenaMessage extends TF2Message { public GameArenaMessage(Object... args) { super(args); } }
	public static class PlayerCapabilityMessage extends TF2Message { public PlayerCapabilityMessage(Object... args) { super(args); } }
	public static class BulletMessage extends TF2Message { public BulletMessage(Object... args) { super(args); } }
	public static class BuildingConfigMessage extends TF2Message { public BuildingConfigMessage(Object... args) { super(args); } }
	public static class GuiConfigMessage extends TF2Message { public GuiConfigMessage(Object... args) { super(args); } }
	public static class ShowGuiMessage extends TF2Message { public ShowGuiMessage(Object... args) { super(args); } }
	public static class WeaponDataMessage extends TF2Message { public WeaponDataMessage(Object... args) { super(args); } }
	public static class WearableChangeMessage extends TF2Message { public WearableChangeMessage(Object... args) { super(args); } }
	public static class WeaponDroppedMessage extends TF2Message { public WeaponDroppedMessage(Object... args) { super(args); } }
	public static class EffectCooldownMessage extends TF2Message { public EffectCooldownMessage(Object... args) { super(args); } }
	public static class ContractMessage extends TF2Message { public ContractMessage(Object... args) { super(args); } }
	public static class ParticleSpawnMessage extends TF2Message { public ParticleSpawnMessage(Object... args) { super(args); } }
	public static class VelocityAddMessage extends TF2Message { public VelocityAddMessage(Object... args) { super(args); } }
	public static class AttackSyncMessage extends TF2Message { public AttackSyncMessage(Object... args) { super(args); } }
	public static class NetworkedSoundMessage extends TF2Message { public NetworkedSoundMessage(Object... args) { super(args); } }
	public static class NetworkedSoundStopMessage extends TF2Message { public NetworkedSoundStopMessage(Object... args) { super(args); } }
	public static class InitMessage extends TF2Message { public InitMessage(Object... args) { super(args); } }
	public static class InitClientMessage extends TF2Message {
		public Configuration conf;
		public InitClientMessage(Object... args) { super(args); if (args.length > 0 && args[0] instanceof Configuration c) this.conf = c; }
	}
	public static class ContractNewMessage extends TF2Message { public ContractNewMessage(Object... args) { super(args); } }
}
