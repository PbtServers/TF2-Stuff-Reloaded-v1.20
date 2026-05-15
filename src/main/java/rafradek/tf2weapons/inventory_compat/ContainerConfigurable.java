package rafradek.tf2weapons.inventory;

public class ContainerConfigurable {
	public Object config;

	public ContainerConfigurable(Object player, Object inventory, Object config, int x, int y, int z) {
		this.config = config;
	}
}
