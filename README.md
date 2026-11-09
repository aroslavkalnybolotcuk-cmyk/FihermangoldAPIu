# Golden Fisherman (Fabric)

## Как собрать
1. Зайди на https://fabricmc.net/develop , выбери версию 26.3, скачай шаблон.
2. Скопируй из этого проекта `src/` в шаблон и впиши версии в gradle.properties / build.gradle.
3. `./gradlew build` -> jar в build/libs.

## Что нужно сделать руками
- Нарисовать текстуру `assets/goldenfisher/textures/entity/villager/profession/golden_fisherman.png`
  (64x64, как ванильные в textures/entity/villager/profession/ - золотая шляпа). Без неё житель будет без шляпы/«битая» текстура.
- (Опционально) свои текстуры для золотой бочки: сейчас используются ванильные.

## Если не компилируется (API менялся между версиями)
- `Identifier` <-> `ResourceLocation`
- конструктор `VillagerProfession` / `PoiType` (ModProfessions.java)
- `villager.getVillagerData().profession()` (может быть без .value())
- `BlockEntityType.addSupportedBlock` - есть в Fabric API; иначе миксин.

## Структура для расширения
- ModBlocks / ModProfessions - регистрация
- FishermanAI.lootTable() - добавить улов
- FishermanAI.tick() - поведение (автомат состояний)
- RodInteraction - работа с инвентарём жителя (слот 0 удочка, слот 1 улов)

## Известные ограничения v1
- Ванильный мозг жителя может иногда перебивать маршрут (сон ночью, паника) - это нормально.
- Состояние цикла не сохраняется при перезапуске сервера.
- Удочка не изнашивается; удочку обратно достать нельзя (идея на v2).
