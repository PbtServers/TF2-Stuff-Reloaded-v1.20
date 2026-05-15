package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockProp extends Block {

	public static final EnumProperty<EnumBlockType> TYPE = EnumProperty.<EnumBlockType>create("type",
			EnumBlockType.class);

	public BlockProp(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public BlockProp(BlockBehaviour.Properties properties, MapColor blockMapColorIn) {
		super(properties.mapColor(blockMapColorIn));
	}

	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeModeTab tab, NonNullList<ItemStack> list) {

	}

	@Override
	public BlockState getStateFromMeta(int meta) {
		return this.defaultBlockState().setValue(TYPE, EnumBlockType.values()[meta]);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return (state.getValue(TYPE)).ordinal();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(TYPE); }

	public enum EnumBlockType implements StringRepresentable {
		DIAMOND("diamond"), IRON("iron"), GOLD("gold"), OBSIDIAN("obsidian");

		private final String name;

		private EnumBlockType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}
}
