package rafradek.tf2weapons.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class ItemArmor extends ArmorItem {
	public final EquipmentSlot armorType;

	public ItemArmor(ArmorMaterial material, int renderIndex, EquipmentSlot slot) {
		super(material, slot, new Item.Properties());
		this.armorType = slot;
	}
}
