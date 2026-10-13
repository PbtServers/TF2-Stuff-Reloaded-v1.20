# Estado del port a 1.20.1 — catálogo moderno, 13 de octubre de 2026

## Entrega actual sobre 700cc11

Rama: `development/weapon-foundation-1.20.1`. Esta sección sustituye el estado
de registros y armas de la primera entrega, conservado más abajo como historial.

| Estado | Número exacto | Alcance |
| --- | ---: | --- |
| Definiciones cargadas y registros individuales de armas | 188 | Todos los IDs de weapons.json, incluidas entradas internas/ocultas |
| Portadores históricos adicionales | 14 | IDs de Class que no colisionan con los 188; admiten NBT Type |
| Suministros de munición | 12 | IDs ammo_* y tipos de munición originales |
| Armas con ruta a recursos originales | 184 | Modelos y caras comprobados automáticamente en el cliente |
| Armas con modelo provisional explícito | 4 | meleebase, mnceye, mnceyeang, explodeonignite |
| Armas con combate básico parcial | 101 | 41 hitscan y 60 melee; no equivalen a mecánicas originales completas |
| Armas con comportamiento pendiente | 87 | Proyectiles, objetos especiales y economía de metal, entre otros |
| Armas completamente migradas | 0 | Faltan críticos, equipos y mecánicas específicas/transversales |

El registro de ítems comprende 217 entradas del mod: 188 armas + 14 portadores
+ 12 municiones + los 3 metales previos. No se duplicó Scattergun. Los números
de combate incluyen entradas internas, como meleebase y munición de Sentry;
no son un recuento de 101 armas revisadas manualmente en supervivencia.

### Implementación

- WeaponCatalog carga el recurso común al inicializar los registros. Rechaza
  IDs repetidos o inválidos, herencias cíclicas, padres ausentes y estadísticas
  inválidas. Resuelve Based on independientemente del orden y conserva campos
  desconocidos. Las definiciones son inmutables; Attributes se combina por clave.
- Se mantienen IDs individuales y portadores originales. Type identifica la
  definición del stack, incluso si su ítem base es otro. Se admite el ID simple
  o prefijado por rafradek_tf2_weapons. Un Type inválido no dispara ni se sustituye
  silenciosamente por otra arma. Todos los ítems individuales están en la pestaña
  creativa, con nombres recuperados del archivo en_US.lang original.
- CatalogWeaponItem amplía TF2WeaponItem, que se conserva. El hitscan mantiene
  la dispersión, oclusión por bloques y acumulación de daño de todos los perdigones
  por objetivo. Usa estadísticas originales, daño decimal, tiempos convertidos
  de milisegundos a ticks y caída de daño por distancia. Conserva el límite
  hitscan original de 256 bloques y melee de 2.4, modificable por Range;
  Efficient range se conserva para la IA futura, no como límite del tiro.
  Las armas automáticas disparan durante el uso sostenido; rifles tienen disparo
  base sin scope/charge.
- Las armas melee se ejecutan en el servidor mediante clic izquierdo o uso;
  cancelan el segundo ataque vanilla y aplican alcance y cadencia propios.
  Backstab, reparación de edificios y efectos complejos de melee quedan pendientes.
- Ammo, ReserveAmmo, Reloading, ReloadTicks y deuda de consumo fraccionario se
  almacenan en el stack. La recarga por cartucho puede interrumpirse al disparar;
  la de cargador transfiere sólo la reserva disponible. Deseleccionar cancela la
  recarga. Agotar la reserva no produce munición gratuita. Sneak + usar inicia
  una recarga manual. Creativo dispara sin consumir; supervivencia tiene reservas
  finitas. Los suministros se usan con el arma en la otra mano y sólo consumen
  unidades aceptadas. Crafting/loot de los suministros sigue pendiente.
- La inicialización migra el Ammo previo de Scattergun o el Clip histórico y añade la reserva.
  Cambiar Type de un stack inicializado no concede munición nueva. Barras y
  tooltips leen NBT sin modificarlo. Los cambios se hacen en el servidor y usan
  la sincronización vanilla del ItemStack; no hay catálogos configurables por
  mundo ni cambios remotos de definiciones en esta entrega.
- Se recuperan modificadores de daño, cadencia, dispersión, alcance, perdigones,
  clip, recarga y consumo, además de daño condicionado por fuego y curación por
  golpe/muerte. Se admite Attributes con nombres o IDs numéricos originales,
  combinando atributos incorporados y mejoras según su tipo aditivo/multiplicativo.
  El índice de 249 IDs procede de las fuentes originales; no implica que los 249
  comportamientos estén portados. Atributos no implementados se indican [pending].
- ProjectileActions ofrece un punto de registro para emisores de entidades
  modernas. No hay emisores aún: los lanzadores pendientes no consumen munición
  ni producen hitscan falso. Widowmaker y Short Circuit conservan datos,
  pero el consumo de metal sigue pendiente. Los tooltips distinguen comportamiento
  parcial o pendiente en lugar de declarar estas armas completas.

### Recursos cliente

WeaponModels está restringido a Dist.CLIENT. El registro de modelos adicionales
y la resolución de ItemOverrides seleccionan Render desde Type. Se conserva el
modelo seleccionado con sus transforms para las perspectivas vanilla y sus
overrides originales. No se creó una tabla gigante de overrides duplicados ni
se añadieron nuevas exclusiones Gradle. Los JSON generados son sólo entradas de
inventario ausentes que apuntan al recurso original correspondiente.

Se añadieron el atlas de Monoculus y el padre normal del cuchillo, que tenía sólo
overrides y podía devolver geometría vacía. Se repararon slots de textura
indefinidos en Rocket Launcher, Liberty Launcher, Sharp Dresser y Splendid Screen
apuntándolos a la textura existente de la misma arma, sin cambiar geometría o UV.
Esos slots ya faltaban en las fuentes históricas; la fidelidad visual de sus UV
necesita revisión manual. Se conserva la paleta histórica compartida gunslinger
del modelo 3D knife_normal: su nombre no implica que la geometría sea un guante.

Los 184 mappings no significan 184 geometrías únicas ni una revisión visual de
las 188 armas en primera persona, tercera persona y suelo. Algunas armas usan
sprites o recursos compartidos originales. missing_models.txt sigue identificando
geometrías ausentes. Las animaciones/backstab dependientes de lógica antigua y
las variantes de equipo requieren más trabajo. Las cuatro entradas sin Render
heredable usan un icono provisional explícito y tooltip de recurso pendiente.

### Pruebas reales

- clean build con Java 17 y Forge 47.2.0: correcto, incluyendo 5 tests JUnit.
- 5 tests unitarios: recuento/IDs, herencia y atributos, rechazo de duplicados y
  padres/ciclos inválidos, conservación de cartuchos y cargadores, interrupción,
  creatividad y límites de estado.
- runGameTestServer: servidor iniciado correctamente, mundo aislado, 7 pruebas
  pasadas. Verifican los 188 registros activos, Type histórico/inválido, NBT y
  mejoras numéricas, daño y cooldown, oclusión, munición de lanzadores pendientes,
  acumulación de perdigones, melee, suministros y uso continuo. Esto sí comprueba
  comportamiento en un servidor Minecraft; no sustituye una partida multijugador.
- runClient: arrancó y conectó al servidor integrado. La validación de baking
  comprobó 188 rutas individuales + 188 rutas mediante portador Type, geometría
  no vacía y sprites de caras sin missingno. Resultado: 184 mappings originales,
  4 provisionales. No se revisaron manualmente todas las perspectivas/animaciones.
- python tools/audit_resources.py --check --output resource-audit.json: correcto.
  El informe contiene mapping y estado por arma; comprueba JSON, referencias
  locales y variables de textura, más eventos de sonido del catálogo. No valida
  todos los recursos vanilla ni convierte el contenido histórico a datapacks.
- Queda un aviso de audio histórico fuera del catálogo: no existe
  sounds/mob/sniper/sniper_laughhappy03.ogg. Se conserva su referencia para
  recuperarlo con los NPCs. Dragon's Fury no dispone de sample de recarga original;
  su evento se declara vacío de forma explícita, sin sustituirlo por otro sonido.

Comandos reproducibles en PowerShell:

```powershell
$env:JAVA_HOME = 'G:\Programas\zulu-17'
.\gradlew.bat clean build --gradle-user-home .gradle-local
.\gradlew.bat runGameTestServer --gradle-user-home .gradle-local
.\gradlew.bat runClient --gradle-user-home .gradle-local
python tools/generate_weapon_resources.py
python tools/audit_resources.py --check --output resource-audit.json
```

### Pendientes prioritarios

Proyectiles/entidades y explosiones reales; críticos/minicríticos, DamageSources
propios, resistencia/penetración y knockback; scope/charge y spin-up de Minigun;
backstab, sangrado y otros efectos; atributos de equipamiento y cambios por
salud/rage; reservas compartidas, recetas y loot; efectos de Medic y Spy; modelos
únicos/animaciones y perspectivas revisadas visualmente. Después mercenarios y
equipos, Engineer y bloques/interfaces, conservando las mejoras del fork.

Referencias de API utilizadas: [registros Forge](https://docs.minecraftforge.net/en/1.20.1/concepts/registries/),
[GameTest](https://docs.minecraftforge.net/en/1.20.x/misc/gametest/) y fuentes
oficiales Forge 47.2.0 de ModelEvent y ModelBakery en la caché de dependencias.

## Historial: primera entrega de estabilización (700cc11)

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
