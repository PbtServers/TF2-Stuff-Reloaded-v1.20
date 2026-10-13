# Estado del port a 1.20.1 — 13 de octubre de 2026

Este documento describe el código actual, no una declaración de que el port
esté terminado. Se conservan LGPL 2.1, los créditos y las fuentes históricas.

## Base revisada

Se revisaron README.md, Importante.txt, missing_models.txt, configuración Gradle,
registros modernos, base de armas, catálogos y recursos, así como los commits
afe4b3c y 91a8bc2 (15 de mayo) y fb80848 (16 de mayo de 2026).

El punto de entrada activo es TF2StuffReloaded y conserva
`rafradek_tf2_weapons`. Registra tres metales y Scattergun. TF2Blocks y
TF2EntityTypes tienen DeferredRegister, pero ninguna entrada. No se inicializa
el cargador histórico MapList desde ese punto de entrada. No hay registro moderno
de menús, BlockEntities, capacidades de combate ni canal de armas.

Hay 693 archivos Java, incluidos 63 bajo namespaces Minecraft/Forge excluidos
del JAR. No se añadieron stubs ni exclusiones. Muchas copias `_compat` conservan
el paquete original: que compilen no prueba que implementen un sistema real.
Las exclusiones de build.gradle dejan pendientes renderizadores, IA, edificios,
proyectiles, bloques, interfaces, contratos, loot, atributos y JEI originales.
Las mejoras históricas de hornos, JEI y persistencia de equipos siguen presentes
en sus fuentes, pero no están conectadas a la implementación moderna.

## Cambios funcionales de esta entrega

- Scattergun conserva hitscan, dispersión, diez perdigones, munición por stack,
  recarga automática, sonidos, tooltips y cooldowns.
- El disparo acumula impactos por entidad antes de aplicar daño. Evita que la
  invulnerabilidad de Minecraft descarte los siguientes perdigones del mismo tiro.
- Se comprueba el cooldown dentro de use; se excluyen espectadores del hitscan
  y se resuelven objetivos cuya caja contiene el origen del disparo.
- Las lecturas de munición/recarga no crean NBT del lado cliente y se limitan
  los valores de munición y tiempos recibidos a los máximos del arma.
- No se registra la configuración histórica de mixins: referencia clases que
  Gradle excluye. Se conservan configuración y fuentes para migrarlas después.
- Cuatro tablas históricas de loot se reparan como JSON válido. Permanecen en
  assets y no constituyen loot migrado al sistema de datapacks 1.20.1.
- Los modelos Pomson y Pomson6000 pasan a UTF-8 y usan la textura Pomson existente
  para partículas, en lugar de una referencia local inexistente.

## Catálogos y recursos

La auditoría reproducible es `python tools/audit_resources.py --check --output
resource-audit.json`. Cuenta 188 entradas de armas, 18 cosméticos y 10 cajas:
216 entradas totales, no 216 armas funcionales. Sólo Scattergun está registrada
como arma moderna; sus estadísticas todavía son constantes del commit anterior.

Todas las referencias Render directas de weapons.json tienen un JSON de modelo.
Esto no demuestra que sean modelos únicos o visualmente correctos:
missing_models.txt sigue siendo una lista relevante de geometrías ausentes o
compartidas. La auditoría valida sintaxis JSON y referencias locales explícitas
de modelos/texturas. No valida referencias vanilla, overrides, animaciones,
sonidos ni correspondencia visual. Existen rutas heredadas como
`blocks/hardened_clay_stained_cyan` en modelos de maquinaria aún sin migrar.
La selección de modelos por NBT Type sigue pendiente; no se ha sustituido por
otro sistema provisional ni se han registrado armas sin mecánicas.

## Verificación

- `gradlew.bat clean build -Dorg.gradle.java.home=G:\Programas\zulu-17
  --gradle-user-home .gradle-local`: correcto, repetido con la configuración final.
- Auditoría: cero JSON ilegibles, cero modelos Render directos ausentes y cero
  referencias explícitas a texturas/modelos locales ausentes.
- JAR inspeccionado: contiene TF2WeaponItem, no contiene clases en
  net.minecraft/net.minecraftforge y no declara MixinConfigs en el manifest.
- Gradle indica test NO-SOURCE: no hay suite automatizada de comportamiento.
- `runServer` llegó al lanzador forgeserveruserdev y se detuvo por EULA no
  aceptada. No se aceptó automáticamente y esto no valida inicialización completa
  de registros, creación de mundo, conexión de jugadores ni combate dedicado.
- Las pruebas de daño, dispersión, munición sincronizada y perspectivas del
  modelo necesitan ejecución dentro de Minecraft. No se dan por comprobadas.

## Siguiente fase funcional

1. Portar WeaponData y carga de catálogos al sistema común moderno, validando
   estadísticas y atributos con errores explícitos y resolviendo Type por stack.
   Conservar la Scattergun actual al integrar progresivamente datos originales.
2. Incorporar familias hitscan con reservas reales y recarga por cartucho/cargador,
   distinguiendo variantes cuyos atributos o mecánicas todavía no se implementan.
3. Conectar Render del catálogo a modelos por Type mediante APIs cliente 1.20.1;
   probar inventario, ambas manos, suelo, tercera persona y recarga de recursos.
4. Recuperar proyectiles y DamageSources, críticos y atributos antes de conectar
   mercenarios. Después edificios Engineer, máquinas y menús, manteniendo las
   correcciones originales del fork y realizando pruebas en servidor dedicado.
