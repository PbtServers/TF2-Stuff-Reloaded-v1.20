package rafradek.tf2weapons.world.gen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ScatteredFeatureTF2Base {

	public static String[] templateNames = { "red", "blu" };

	public ScatteredFeatureTF2Base() {}

	public static class Start {
		public Start() {}
	}

	public static class MapGen {
		public MapGen(Object provider) {}

		public void generate(Level world, int chunkX, int chunkZ, Object primer) {}

		public void generateStructure(Level world, Random random, Object chunkPos) {}

		public BlockPos getNearestStructurePos(Level world, BlockPos pos, boolean findUnexplored) {
			return null;
		}
	}
}
