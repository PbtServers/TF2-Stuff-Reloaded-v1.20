package rafradek.tf2weapons.weapon;

import java.io.StringReader;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class WeaponCatalogTest {
    @Test public void bundledCatalogAndInheritedVariants() {
        WeaponCatalog catalog = WeaponCatalog.get();
        assertEquals(188, catalog.all().size());
        assertEquals(188, catalog.all().stream().map(WeaponDefinition::id).distinct().count());
        assertEquals(catalog.require("scattergun"), catalog.find("rafradek_tf2_weapons:scattergun"));
        WeaponDefinition fan = catalog.require("forcenature");
        WeaponStats stats = WeaponStats.of(fan, fan.attributes());
        assertEquals(2, stats.clip());
        assertEquals(12, stats.pellets());
        assertEquals(0.54, stats.damage(), 0.000001);
        assertEquals(7, stats.cooldown());
        assertEquals(256, stats.range(), 0); // Efficient range must not truncate a hitscan ray.
        assertTrue(stats.fullReload());
        assertEquals("rafradek_tf2_weapons:force_nature", fan.render());
        assertEquals(WeaponDefinition.Behavior.PROJECTILE, catalog.require("onyxblaster").behavior());
        assertEquals(WeaponDefinition.Behavior.MELEE, catalog.require("bat").behavior());
    }
    @Test public void orderIndependentInheritanceAndMergedAttributes() throws Exception {
        WeaponCatalog catalog = WeaponCatalog.parse(new StringReader("{\"child\":{\"Based on\":\"base\",\"Attributes\":{\"DamageBonus\":1.2}},\"base\":{\"Class\":\"bullet\",\"Damage\":2,\"Attributes\":{\"ClipSizePenalty\":0.5}}}"));
        assertEquals(2, catalog.require("child").attributes().size());
        assertEquals(2, catalog.require("child").number("Damage", 0), 0);
        catalog.require("child").rawData().addProperty("Damage", 99);
        assertEquals(2, catalog.require("child").number("Damage", 0), 0);
    }
    @Test public void rejectsDuplicateIdsCyclesAndMissingParents() throws Exception {
        for (String json : List.of(
                "{\"a\":{\"Class\":\"bullet\"},\"a\":{\"Class\":\"melee\"}}",
                "{\"a\":{\"Based on\":\"b\"},\"b\":{\"Based on\":\"a\"}}",
                "{\"a\":{\"Based on\":\"absent\"}}",
                "{\"Bad ID\":{\"Class\":\"bullet\"}}",
                "{\"a\":{\"Class\":\"bullet\",\"Damage\":-1}}")) {
            assertThrows(IllegalArgumentException.class, () -> WeaponCatalog.parse(new StringReader(json)));
        }
    }
    @Test public void shellReloadConservesAmmoAndCanBeInterrupted() {
        WeaponDefinition data = WeaponCatalog.get().require("scattergun");
        WeaponStats stats = WeaponStats.of(data, data.attributes());
        AmmoState state = new AmmoState(0, 3, 0, false).start(stats);
        for (int i = 0; i < stats.firstReload(); i++) state = state.tick(stats);
        assertEquals(1, state.ammo());
        assertEquals(2, state.reserve());
        assertTrue(state.reloading());
        state = state.fire(false, 1);
        assertFalse(state.reloading());
        assertEquals(0, state.ammo());
        assertEquals(2, state.reserve());
        state = state.start(stats);
        for (int i = 0; i < 500; i++) state = state.tick(stats);
        assertEquals(2, state.ammo());
        assertEquals(0, state.reserve());
        assertFalse(state.reloading());
    }
    @Test public void magazineReloadNeverCreatesAmmoAndCreativeDoesNotConsume() {
        WeaponDefinition data = WeaponCatalog.get().require("pistol");
        WeaponStats stats = WeaponStats.of(data, data.attributes());
        AmmoState state = new AmmoState(5, 3, 0, false).start(stats);
        for (int i = 0; i < stats.firstReload(); i++) state = state.tick(stats);
        assertEquals(8, state.ammo());
        assertEquals(0, state.reserve());
        assertEquals(8, state.fire(true, 1).ammo());
        assertFalse(state.reloading());
        assertEquals(0, new AmmoState(-5, -10, -8, false).bounded(stats).ammo());
        assertEquals(stats.clip(), new AmmoState(999, 999, 999, true).bounded(stats).ammo());
    }
}
