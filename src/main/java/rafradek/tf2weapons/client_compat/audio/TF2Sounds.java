package rafradek.tf2weapons.client.audio;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class TF2Sounds {
	public static final Map<ResourceLocation, SoundEvent> SOUND_EVENTS = new HashMap<>();
	public static final SoundEvent DOUBLE_DONK = sound("double_donk");
	public static final SoundEvent MISC_CRIT = sound("misc_crit");
	public static final SoundEvent MISC_MINI_CRIT = sound("misc_mini_crit");
	public static final SoundEvent MISC_PAIN = sound("misc_pain");
	public static final SoundEvent MOB_SAPPER_PLANT = sound("mob_sapper_plant");
	public static final SoundEvent MOB_TELEPORTER_SEND = sound("mob_teleporter_send");
	public static final SoundEvent WEAPON_MANTREADS = sound("weapon_mantreads");
	public static final SoundEvent WEAPON_SHIELD_HIT = sound("weapon_shield_hit");
	public static final SoundEvent WEAPON_SHIELD_HIT_RANGE = sound("weapon_shield_hit_range");
	public static final SoundEvent WEAPON_STUN = sound("weapon_stun");
	public static final SoundEvent WEAPON_STUN_MAX = sound("weapon_stun_max");

	public static void registerSounds() {}

	public static SoundEvent register(ResourceLocation id) {
		return SOUND_EVENTS.computeIfAbsent(id, SoundEvent::createVariableRangeEvent);
	}

	private static SoundEvent sound(String name) {
		return register(new ResourceLocation("tf2weapons", name));
	}
}
