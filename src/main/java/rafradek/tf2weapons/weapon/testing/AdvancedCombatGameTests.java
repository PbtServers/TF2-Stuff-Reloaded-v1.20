package rafradek.tf2weapons.weapon.testing;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.registry.*;
import rafradek.tf2weapons.weapon.*;
import rafradek.tf2weapons.weapon.projectile.*;

@GameTestHolder(TF2StuffReloaded.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedCombatGameTests {
    private static ItemStack weapon(String id) {
        ItemStack stack = TF2Items.WEAPONS.get(id).get().getDefaultInstance();
        CompoundTag attributes = new CompoundTag(); attributes.putFloat("NoRandomCrit", 1); attributes.putFloat("SpreadBonus", 0);
        stack.getOrCreateTag().put("Attributes", attributes);
        ((CatalogWeaponItem) stack.getItem()).initialize(stack);
        return stack;
    }
    private static Player player(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockSurvivalPlayer();
        Vec3 position = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
        player.moveTo(position.x, position.y, position.z, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }
    private static LivingEntity target(GameTestHelper helper, Vec3 position) {
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, position);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        cow.setHealth(100); cow.setNoGravity(true);
        return cow;
    }
    private static WeaponProjectile projectile(GameTestHelper helper, Player owner, ItemStack stack, WeaponProjectile.Flight flight, Vec3 position) {
        String key = flight == WeaponProjectile.Flight.STICKY ? "stickybomb" : flight.name().toLowerCase(java.util.Locale.ROOT);
        WeaponProjectile projectile = TF2EntityTypes.projectile(key).create(helper.getLevel());
        projectile.configure(owner, stack, flight); projectile.setPos(helper.absoluteVec(position)); projectile.setNoGravity(true);
        helper.getLevel().addFreshEntity(projectile);
        return projectile;
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 30)
    public static void rocketImpactHasOneDamageAndOneAmmoCost(GameTestHelper helper) {
        ItemStack stack = weapon("rocketlauncher"); Player owner = player(helper, stack);
        LivingEntity target = target(helper, new Vec3(1.5, 3, 6));
        CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem();
        item.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() == 3, "One rocket consumes one round");
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(target.getHealth() < 92 && target.getHealth() > 85, "Impact plus explosion is one TF2 damage call: " + target.getHealth());
            helper.assertTrue(ModernProjectiles.owned(helper.getLevel(), owner, WeaponProjectile.Flight.ROCKET).isEmpty(), "Rocket removed after impact");
            helper.succeed();
        });
    }
    @GameTest(template = "weapon_test_empty")
    public static void explosionFalloffWallAndIdempotence(GameTestHelper helper) {
        ItemStack stack = weapon("rocketlauncher"); Player owner = player(helper, stack);
        LivingEntity direct = target(helper, new Vec3(4, 3, 4));
        LivingEntity far = target(helper, new Vec3(6, 3, 4));
        LivingEntity occluded = target(helper, new Vec3(4, 3, 6));
        helper.setBlock(new BlockPos(4, 3, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 4, 5), Blocks.STONE);
        WeaponProjectile projectile = projectile(helper, owner, stack, WeaponProjectile.Flight.ROCKET, new Vec3(4, 3.5, 4));
        helper.assertTrue(projectile.detonate(direct, 1), "First explosion executes");
        float after = direct.getHealth();
        helper.assertTrue(!projectile.detonate(direct, 1) && direct.getHealth() == after, "Second detonation never duplicates damage");
        helper.assertTrue(direct.getHealth() < far.getHealth(), "Area damage falls off");
        helper.assertTrue(occluded.getHealth() == 100, "Solid wall occludes blast");
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(4, 3, 5));
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 80)
    public static void grenadeBouncesAndFuseDetonates(GameTestHelper helper) {
        ItemStack stack = weapon("grenadelauncher"); Player owner = player(helper, stack);
        helper.setBlock(new BlockPos(4, 3, 5), Blocks.STONE);
        WeaponProjectile grenade = projectile(helper, owner, stack, WeaponProjectile.Flight.GRENADE, new Vec3(4.5, 3.5, 4));
        grenade.setDeltaMovement(0, 0, 1);
        helper.runAtTickTime(3, () -> helper.assertTrue(!grenade.isRemoved() && grenade.getDeltaMovement().z < 0, "Pipe bounces without impact explosion"));
        helper.runAtTickTime(50, () -> { helper.assertTrue(grenade.isRemoved(), "46-tick fuse detonates"); helper.succeed(); });
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 30)
    public static void stickiesArmPersistAndOnlyOwnerDetonates(GameTestHelper helper) {
        ItemStack stack = weapon("stickybomblauncher"); Player owner = player(helper, stack);
        Player other = helper.makeMockSurvivalPlayer();
        helper.assertTrue(!owner.getUUID().equals(other.getUUID()), "Test players have distinct ownership");
        helper.setBlock(new BlockPos(4, 3, 5), Blocks.STONE);
        WeaponProjectile bomb = projectile(helper, owner, stack, WeaponProjectile.Flight.STICKY, new Vec3(4.5, 3.5, 4));
        bomb.setDeltaMovement(0, 0, 1);
        helper.assertTrue(ModernProjectiles.detonate(owner, stack) == 0, "Unarmed sticky cannot detonate");
        helper.runAtTickTime(3, () -> helper.assertTrue(bomb.stuck() && bomb.getDeltaMovement().lengthSqr() == 0, "Sticky attaches to block"));
        helper.runAtTickTime(18, () -> {
            CompoundTag saved = new CompoundTag(); bomb.save(saved);
            WeaponProjectile restored = TF2EntityTypes.projectile("stickybomb").create(helper.getLevel()); restored.load(saved);
            helper.assertTrue(restored.stuck() && restored.armed() && restored.ownedBy(owner.getUUID()), "Sticky state and owner survive save/load");
            helper.assertTrue(ModernProjectiles.detonate(other, stack) == 0, "Other owner cannot detonate");
            helper.assertTrue(ModernProjectiles.detonate(owner, stack) == 1 && bomb.isRemoved(), "Owner detonates armed sticky once");
            helper.assertTrue(ModernProjectiles.detonate(owner, stack) == 0, "Repeated request is harmless");
            helper.succeed();
        });
    }
    @GameTest(template = "weapon_test_empty")
    public static void criticalMiniBackstabAndResistance(GameTestHelper helper) {
        ItemStack stack = weapon("sniperrifle"); Player owner = player(helper, stack);
        LivingEntity target = target(helper, new Vec3(4, 3, 4));
        WeaponDefinition data = ((CatalogWeaponItem) stack.getItem()).definition(stack);
        CombatDamage.hit(target, owner, owner, stack, data, 5, CombatMath.Critical.FULL, CombatDamage.Kind.BULLET, 200, owner.position(), false, false);
        helper.assertTrue(target.getHealth() == 85, "Full critical is 3x without distance falloff");
        CombatDamage.hit(target, owner, owner, stack, data, 5, CombatMath.Critical.MINI, CombatDamage.Kind.BULLET, 200, owner.position(), false, false);
        helper.assertTrue(Math.abs(target.getHealth() - 78.25) < 0.001, "Mini is 1.35x and custom cadence does not lose shots to vanilla immunity");
        ItemStack resistant = weapon("fistofsteel"); target.setItemSlot(EquipmentSlot.MAINHAND, resistant);
        helper.assertTrue(CombatDamage.resistance(target, CombatDamage.Kind.BULLET) < 1, "Held ranged resistance applies");
        LivingEntity victim = target(helper, new Vec3(1.5, 3, 2.8)); victim.setYRot(0);
        helper.assertTrue(CombatDamage.backstab(owner, victim), "Same facing, attacker behind");
        victim.setYRot(180);
        helper.assertTrue(!CombatDamage.backstab(owner, victim), "Front attack is not a backstab");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void sniperChargeConsumesOnceAndMinigunSpinsUp(GameTestHelper helper) {
        ItemStack sniper = weapon("sniperrifle"); Player owner = player(helper, sniper);
        CatalogWeaponItem rifle = (CatalogWeaponItem) sniper.getItem();
        rifle.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(rifle.readAmmo(sniper, rifle.stats(sniper)).ammo() == 25, "Scoping consumes no round");
        for (int i = 0; i < 66; i++) rifle.onUseTick(helper.getLevel(), owner, sniper, 72000 - i);
        helper.assertTrue(rifle.chargeFraction(sniper) == 1, "Original full charge at 66 ticks");
        rifle.primaryScoped(owner, sniper);
        helper.assertTrue(rifle.readAmmo(sniper, rifle.stats(sniper)).ammo() == 24, "Scoped primary consumes one round");
        rifle.releaseUsing(sniper, helper.getLevel(), owner, 71934);
        helper.assertTrue(rifle.readAmmo(sniper, rifle.stats(sniper)).ammo() == 24, "Unscoping after primary does not fire a second shot");
        ItemStack minigun = weapon("minigun"); owner.setItemInHand(InteractionHand.MAIN_HAND, minigun);
        CatalogWeaponItem gun = (CatalogWeaponItem) minigun.getItem(); gun.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 17; i++) gun.onUseTick(helper.getLevel(), owner, minigun, 72000 - i);
        helper.assertTrue(gun.readAmmo(minigun, gun.stats(minigun)).ammo() == 200, "No shot before spin-up");
        gun.onUseTick(helper.getLevel(), owner, minigun, 71982);
        helper.assertTrue(gun.readAmmo(minigun, gun.stats(minigun)).ammo() == 199, "Fire at 18-tick spin-up");
        gun.releaseUsing(minigun, helper.getLevel(), owner, 71982);
        helper.assertTrue(minigun.getTag().getInt("SpinTicks") == 0, "Release clears spin-up state");
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 50)
    public static void flareIgnitesAndHuntsmanCharges(GameTestHelper helper) {
        ItemStack flare = weapon("flaregun"); Player owner = player(helper, flare);
        LivingEntity target = target(helper, new Vec3(1.5, 3, 6));
        ((CatalogWeaponItem) flare.getItem()).use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        helper.runAtTickTime(8, () -> {
            helper.assertTrue(target.hasEffect(TF2Effects.AFTERBURN.get()), "Flare applies attributed afterburn");
            ItemStack bow = weapon("huntsman"); owner.setItemInHand(InteractionHand.MAIN_HAND, bow);
            owner.getInventory().add(new ItemStack(net.minecraft.world.item.Items.ARROW, 2));
            CatalogWeaponItem item = (CatalogWeaponItem) bow.getItem(); item.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            for (int i = 0; i < 20; i++) item.onUseTick(helper.getLevel(), owner, bow, 72000 - i);
            item.releaseUsing(bow, helper.getLevel(), owner, 71980);
            helper.assertTrue(ModernProjectiles.owned(helper.getLevel(), owner, WeaponProjectile.Flight.ARROW).size() == 1, "Huntsman releases one charged entity");
            helper.assertTrue(item.readAmmo(bow, item.stats(bow)).ammo() == 1, "One arrow consumes one actual inventory arrow");
            helper.succeed();
        });
    }
    @GameTest(template = "weapon_test_empty")
    public static void spawnDataPreservesFlightCriticalAndVelocity(GameTestHelper helper) {
        ItemStack stack = weapon("rocketlauncher"); stack.getOrCreateTag().putInt("ShotCritical", 2);
        Player owner = player(helper, stack);
        WeaponProjectile source = projectile(helper, owner, stack, WeaponProjectile.Flight.ROCKET, new Vec3(4, 3, 4));
        source.setDeltaMovement(5, 0.1, -2);
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            source.writeSpawnData(buffer);
            WeaponProjectile restored = TF2EntityTypes.projectile("rocket").create(helper.getLevel()); restored.readSpawnData(buffer);
            helper.assertTrue(restored.flight() == WeaponProjectile.Flight.ROCKET && restored.criticalLevel() == 2 && restored.getDeltaMovement().equals(source.getDeltaMovement()), "Spawn payload preserves metadata and unclipped velocity");
            helper.assertTrue(source.getAddEntityPacket() != null, "Forge spawn packet exists");
        } finally { buffer.release(); }
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty", timeoutTicks = 40)
    public static void afterburnDoesNotDuplicateVanillaFireDamage(GameTestHelper helper) {
        LivingEntity target = target(helper, new Vec3(4, 3, 4));
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(TF2Effects.AFTERBURN.get(), 40));
        helper.runAtTickTime(21, () -> {
            helper.assertTrue(Math.abs(target.getHealth() - 98.8) < 0.002, "Initial tick plus two periodic 0.4 ticks, no extra vanilla burn: " + target.getHealth());
            helper.assertTrue(target.getRemainingFireTicks() <= 1, "Fire visuals do not introduce a second damage schedule");
            helper.succeed();
        });
    }
    @GameTest(template = "weapon_test_empty")
    public static void cancelledMultiProjectileSpawnRollsBackWithoutAmmoCost(GameTestHelper helper) {
        ItemStack stack = weapon("rocketlauncher"); stack.getOrCreateTag().getCompound("Attributes").putFloat("PelletBonus", 2);
        Player owner = player(helper, stack);
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof WeaponProjectile projectile && projectile.ownedBy(owner.getUUID()) && attempts.incrementAndGet() == 2) event.setCanceled(true);
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            CatalogWeaponItem item = (CatalogWeaponItem) stack.getItem(); item.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(attempts.get() == 2, "Second entity spawn was rejected by Forge event");
            helper.assertTrue(item.readAmmo(stack, item.stats(stack)).ammo() == 4, "Failed shot consumes no ammo");
            helper.assertTrue(ModernProjectiles.owned(helper.getLevel(), owner, WeaponProjectile.Flight.ROCKET).isEmpty(), "Partial spawn rolled back, no free projectile");
        } finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener); }
        helper.succeed();
    }
    @GameTest(template = "weapon_test_empty")
    public static void huntsmanCannotFireWithoutArrowsAndNoBackstabIsRespected(GameTestHelper helper) {
        ItemStack bow = weapon("huntsman"); Player owner = player(helper, bow);
        CatalogWeaponItem item = (CatalogWeaponItem) bow.getItem(); item.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        item.releaseUsing(bow, helper.getLevel(), owner, 72000);
        helper.assertTrue(ModernProjectiles.owned(helper.getLevel(), owner, WeaponProjectile.Flight.ARROW).isEmpty(), "No inventory arrow means no projectile");
        LivingEntity target = target(helper, new Vec3(1.5, 3, 2.8)); target.setYRot(0);
        ItemStack knife = weapon("butterflyknife"); owner.setItemInHand(InteractionHand.MAIN_HAND, knife);
        CatalogWeaponItem blade = (CatalogWeaponItem) knife.getItem(); blade.onLeftClickEntity(knife, owner, target);
        helper.assertTrue(target.getHealth() == 52, "Original knife bonus 4x plus critical 3x, applied once");
        target.discard();
        LivingEntity protectedTarget = target(helper, new Vec3(1.5, 3, 2.8)); protectedTarget.setYRot(0);
        ItemStack noBackstab = weapon("sharpdresser"); noBackstab.getTag().getCompound("Attributes").putFloat("NoBackstab", 1);
        owner.setItemInHand(InteractionHand.MAIN_HAND, noBackstab);
        ((CatalogWeaponItem) noBackstab.getItem()).onLeftClickEntity(noBackstab, owner, protectedTarget);
        helper.assertTrue(protectedTarget.getHealth() == 96, "NoBackstab attribute keeps ordinary melee damage");
        helper.succeed();
    }
}
