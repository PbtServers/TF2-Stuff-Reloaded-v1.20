package rafradek.tf2weapons;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;

import java.util.function.Supplier;

public class FastProfiler implements ProfilerFiller {
	@Override public void startTick() {}
	@Override public void endTick() {}
	@Override public void push(String name) {}
	@Override public void push(Supplier<String> name) {}
	@Override public void pop() {}
	@Override public void popPush(String name) {}
	@Override public void popPush(Supplier<String> name) {}
	@Override public void markForCharting(MetricCategory category) {}
	@Override public void incrementCounter(String name, int amount) {}
	@Override public void incrementCounter(Supplier<String> name, int amount) {}
}
