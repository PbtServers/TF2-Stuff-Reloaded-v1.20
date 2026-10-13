package rafradek.tf2weapons.weapon;

import org.junit.Test;
import static org.junit.Assert.*;

public class CombatMathTest {
    @Test public void criticalDamageIgnoresFalloffAndMiniNeverFallsBelowBase() {
        WeaponDefinition rocket = WeaponCatalog.get().require("rocketlauncher");
        assertEquals(1, CombatMath.falloff(rocket, 200, CombatMath.Critical.FULL), 0);
        assertEquals(1, CombatMath.falloff(rocket, 200, CombatMath.Critical.MINI), 0);
        assertEquals(0.53, CombatMath.falloff(rocket, 200, CombatMath.Critical.NORMAL), 0.0001);
        assertEquals(1.35, CombatMath.Critical.MINI.multiplier, 0);
        assertEquals(3, CombatMath.Critical.FULL.multiplier, 0);
    }
    @Test public void chargeHonorsOriginalDelayAndMaximum() {
        assertEquals(0, CombatMath.sniperCharge(26, 1), 0);
        assertEquals(0.5, CombatMath.sniperCharge(46, 1), 0.0001);
        assertEquals(1, CombatMath.sniperCharge(66, 1), 0);
        assertEquals(1, CombatMath.sniperCharge(10000, 1), 0);
        assertTrue(CombatMath.sniperCharge(46, 2) > CombatMath.sniperCharge(46, 1));
    }
    @Test public void explosionCannotCreateNegativeDamageOutsideRadius() {
        assertEquals(1, CombatMath.blastFalloff(0, 3.05, false), 0);
        assertEquals(0, CombatMath.blastFalloff(4, 3.05, false), 0);
        assertEquals(1, CombatMath.blastFalloff(3, 3.05, true), 0);
    }
    @Test public void randomCriticalChanceHonorsOriginalLimits() {
        assertEquals(0.025, CombatMath.criticalChance(0, false), 0);
        assertEquals(0.125, CombatMath.criticalChance(10000, false), 0);
        assertEquals(0.15, CombatMath.criticalChance(0, true), 0);
        assertEquals(0.6, CombatMath.criticalChance(10000, true), 0);
    }
}
