package rafradek.tf2weapons.world.gen.structure;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MannCoBuilding {
	public MannCoBuilding() {}

	public static class Start {
		public Start() {}
	}

	public static class MapGen {
		public MapGen() {}

		public void generate(Level world, int chunkX, int chunkZ, Object primer) {}

		public void generateStructure(Level world, Random random, Object chunkPos) {}

		public BlockPos getNearestStructurePos(Level world, BlockPos pos, boolean findUnexplored) {
			return null;
		}
	}
}
