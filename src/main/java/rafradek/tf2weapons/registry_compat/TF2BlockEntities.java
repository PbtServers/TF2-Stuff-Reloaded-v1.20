package rafradek.tf2weapons.registry;

public final class TF2BlockEntities {
	public static final Holder<Object> AMMO_FURNACE = new Holder<>();
	public static final Holder<Object> OVERHEAD_DOOR = new Holder<>();
	public static final Holder<Object> ROBOT_DEPLOY = new Holder<>();
	public static final Holder<Object> RESUPPLY_CABINET = new Holder<>();
	public static final Holder<Object> CAPTURE_POINT = new Holder<>();
	public static final Holder<Object> GAME_CONFIGURE = new Holder<>();
	public static final Holder<Object> UPGRADE_STATION = new Holder<>();
	public static final Registry REGISTRY = new Registry();

	private TF2BlockEntities() {}

	public static final class Holder<T> {
		public T get() {
			return null;
		}
	}

	public static final class Registry {
		public void register(Object bus) {}
	}
}
