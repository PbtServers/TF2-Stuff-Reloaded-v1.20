package net.minecraft.profiler;

import java.util.Collections;
import java.util.List;

public class Profiler {
	public void startSection(String name) {}

	public void endSection() {}

	public List<Result> getProfilingData(String profilerName) {
		return Collections.emptyList();
	}

	public static class Result {
		public final String profilerName;
		public final double usePercentage;
		public final double totalUsePercentage;

		public Result(String profilerName, double usePercentage, double totalUsePercentage) {
			this.profilerName = profilerName;
			this.usePercentage = usePercentage;
			this.totalUsePercentage = totalUsePercentage;
		}
	}
}
