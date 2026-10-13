package rafradek.tf2weapons.weapon.testing;

import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.registry.TF2Items;
import rafradek.tf2weapons.weapon.*;

@GameTestHolder(TF2StuffReloaded.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponGameTests {
    @GameTest(template = "weapon_test_empty")
    public static void registeredCatalogAndLegacyType(GameTestHelper helper) {
        helper.assertTrue(TF2Items.WEAPONS.size() == 188, "188 catalog registrations");
        HashSet<Object> ids = new HashSet<>();
        TF2Items.WEAPONS.forEach((id, entry) -> {
            helper.assertTrue(entry.isPresent() && ids.add(entry.getId()), "Unique active registration: " + id);
            helper.assertTrue(ForgeRegistries.ITEMS.getValue(entry.getId()) == entry.get(), "Registry binding: " + id);
            ItemStack stack = entry.get().getDefaultInstance();
            helper.assertTrue(((CatalogWeaponItem) stack.getItem()).definition(stack).id().equals(id), "Default Type: " + id);
        });
        ItemStack carrier = new ItemStack(TF2Items.LEGACY_CARRIERS.get("bullet").get());
        carrier.getOrCreateTag().putString("Type", "forcenature");
        CatalogWeaponItem item = (CatalogWeaponItem) carrier.getItem();
        helper.assertTrue(item.stats(carrier).clip() == 2, "Legacy carrier uses individual stats");
        helper.assertTrue(item.definition(carrier).render().endsWith(":force_nature"), "Legacy carrier uses individual Render");
        carrier.getOrCreateTag().putString("Type", "unknown_weapon");
        helper.assertTrue(item.definition(carrier) == null && !item.canExecute(carrier), "Unknown Type cannot fire");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void ammoPersistenceAndUpgradeAttributes(GameTestHelper helper) {
        ItemStack stack = TF2Items.SCATTERGUN.get().getDefaultInstance();
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        item.initialize(stack);
        stack.getOrCreateTag().putInt("Ammo", 2);
        stack.getOrCreateTag().putInt("ReserveAmmo", 7);
        var attributes = new net.minecraft.nbt.CompoundTag();
        attributes.putFloat("0", 1.5F); // Original numeric DamageBonus ID.
        stack.getOrCreateTag().put("Attributes", attributes);
        ItemStack restored = ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(item.readAmmo(restored, item.stats(restored)).ammo() == 2, "Clip survives NBT serialization");
        helper.assertTrue(item.readAmmo(restored, item.stats(restored)).reserve() == 7, "Reserve survives NBT serialization");
        helper.assertTrue(Math.abs(item.stats(restored).damage() - 0.9) < 0.0001, "Numeric attributes applied");
        restored.getOrCreateTag().putString("Type", "pistol");
        item.initialize(restored);
        helper.assertTrue(item.readAmmo(restored, item.stats(restored)).ammo() == 0, "Changing Type cannot grant ammo");
        ItemStack fan = TF2Items.WEAPONS.get("forcenature").get().getDefaultInstance();
        fan.getOrCreateTag().put("Attributes", attributes);
        helper.assertTrue(Math.abs(((CatalogWeaponItem) fan.getItem()).stats(fan).damage() - 0.81) < 0.0001,
                "Stack upgrades multiply the built-in damage penalty");
        ItemStack legacyClip = new ItemStack(TF2Items.LEGACY_CARRIERS.get("bullet").get());
        legacyClip.getOrCreateTag().putString("Type", "scattergun");
        legacyClip.getOrCreateTag().putInt("Clip", 2);
        CatalogWeaponItem legacyItem = (CatalogWeaponItem) legacyClip.getItem();
        legacyItem.initialize(legacyClip);
        helper.assertTrue(legacyItem.readAmmo(legacyClip, legacyItem.stats(legacyClip)).ammo() == 2, "Legacy Clip survives migration");
        helper.succeed();
    }
    private static Player player(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockSurvivalPlayer();
        Vec3 position = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
        player.moveTo(position.x, position.y, position.z, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }
    @GameTest(template = "weapon_test_empty")
    public static void hitscanDamageCooldownAndAmmo(GameTestHelper helper) {
        ItemStack stack = TF2Items.WEAPONS.get("sniperrifle").get().getDefaultInstance();
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        Player player = player(helper, stack);
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(1.5, 3, 6));
        cow.setNoGravity(true);
        float initialHealth = cow.getHealth();
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        item.releaseUsing(stack, helper.getLevel(), player, 72000);
        helper.assertTrue(cow.getHealth() == initialHealth - 5, "Hitscan damage: initial=" + initialHealth + ", actual=" + cow.getHealth());
        helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() == 24, "One round consumed");
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        item.releaseUsing(stack, helper.getLevel(), player, 72000);
        helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() == 24, "Cooldown prevents second shot");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void hitscanBlockedByWorldAndPendingLauncher(GameTestHelper helper) {
        ItemStack stack = TF2Items.WEAPONS.get("sniperrifle").get().getDefaultInstance();
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        Player player = player(helper, stack);
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(1.5, 3, 6));
        float initialHealth = cow.getHealth();
        helper.setBlock(new BlockPos(1, 3, 3), Blocks.STONE);
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        item.releaseUsing(stack, helper.getLevel(), player, 72000);
        helper.assertTrue(cow.getHealth() == initialHealth, "Blocks occlude hitscan");
        ItemStack launcher = TF2Items.WEAPONS.get("grapplinghook").get().getDefaultInstance();
        CatalogWeaponItem rocket = (CatalogWeaponItem) launcher.getItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, launcher);
        rocket.initialize(launcher);
        int pendingAmmo = rocket.readAmmo(launcher, rocket.stats(launcher)).ammo();
        rocket.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(rocket.readAmmo(launcher, rocket.stats(launcher)).ammo() == pendingAmmo, "Pending projectiles never consume ammo");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void pelletDamageAndMelee(GameTestHelper helper) {
        ItemStack stack = TF2Items.SCATTERGUN.get().getDefaultInstance();
        var attributes = new net.minecraft.nbt.CompoundTag();
        attributes.putFloat("8", 0); // Disable spread to test aggregation deterministically.
        attributes.putFloat("NoRandomCrit", 1);
        stack.getOrCreateTag().put("Attributes", attributes);
        Player player = player(helper, stack);
        var target = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(1.5, 3, 4));
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        float before = target.getHealth();
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(before - target.getHealth() > 6, "All pellets are aggregated instead of hurt immunity discarding them");
        ItemStack bat = TF2Items.WEAPONS.get("bat").get().getDefaultInstance();
        var meleeAttributes = new net.minecraft.nbt.CompoundTag(); meleeAttributes.putFloat("NoRandomCrit", 1);
        bat.getOrCreateTag().put("Attributes", meleeAttributes);
        player.setItemInHand(InteractionHand.MAIN_HAND, bat);
        var close = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(1.5, 3, 2.8));
        float health = close.getHealth();
        ((CatalogWeaponItem) bat.getItem()).onLeftClickEntity(bat, player, close);
        helper.assertTrue(Math.abs(health - close.getHealth() - 3.5) < 0.0001, "Melee uses catalog damage once");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void suppliesAndClientReadOnlyViews(GameTestHelper helper) {
        ItemStack weapon = TF2Items.SCATTERGUN.get().getDefaultInstance();
        CatalogWeaponItem item = (CatalogWeaponItem) weapon.getItem();
        var before = weapon.getTag().copy();
        item.getBarWidth(weapon);
        item.getBarColor(weapon);
        item.appendHoverText(weapon, null, new java.util.ArrayList<>(), net.minecraft.world.item.TooltipFlag.NORMAL);
        helper.assertTrue(before.equals(weapon.getTag()), "Client item views do not mutate NBT");
        item.initialize(weapon);
        weapon.getOrCreateTag().putInt("ReserveAmmo", 30);
        Player player = player(helper, weapon);
        ItemStack supply = new ItemStack(TF2Items.AMMO.get("ammo_shotgun").get(), 5);
        player.setItemInHand(InteractionHand.OFF_HAND, supply);
        supply.getItem().use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        helper.assertTrue(supply.getCount() == 3, "Supply only consumes the two units accepted");
        helper.assertTrue(item.readAmmo(weapon, item.stats(weapon)).reserve() == 32, "Reserve capped at catalog maximum");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 50)
    public static void continuousFireUsesServerTiming(GameTestHelper helper) {
        ItemStack stack = TF2Items.WEAPONS.get("pistol").get().getDefaultInstance();
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        Player player = player(helper, stack);
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() == 11, "Initial automatic shot");
        for (int tick = 1; tick <= 8; tick++) {
            int time = tick;
            helper.runAtTickTime(time, () -> {
                player.getCooldowns().tick();
                item.onUseTick(helper.getLevel(), player, stack, 72000 - time);
                if (time == 8) {
                    helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() <= 10, "Holding use fires again after server cooldown");
                    helper.succeed();
                }
            });
        }
    }
}
