# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`orelia-extra` is a Paper 1.21.x (Java 21) Minecraft plugin jar — the later MMORPG-feature layer of a 3-plugin split:

- `orelia-core` (separate repo) — combat/player/status foundation. **Required dependency**, must be installed and enabled before this plugin loads.
- `orelia-world` (separate repo) — quest/NPC/story content layer. **Soft dependency**, only used by `AchievementModule`'s optional `COMPLETE_QUEST` condition via `rpg.world.api.QuestApi`.
- **orelia-extra** (this repo) — Party, Guild, Trade, Mail, Auction, Housing, Pet, Mount, Ranking, Achievement.

Hard rule: orelia-extra only ever calls into orelia-core/orelia-world through their published `rpg.api.*`/`rpg.world.api.*` interfaces (via Bukkit's `ServicesManager`) or generic `rpg.core.*`/`rpg.database.*` infrastructure (`ConfigManager`, `SchedulerService`, `PlayerDataManager`, `DatabaseManager`, `SchemaOwner`). **Never** reach into either plugin's internal gameplay-module classes directly. Money (Auction settlement) goes straight through Vault's `Economy`, the same way orelia-world does — there is no custom EconomyApi.

## Commands

```
./gradlew build
```

Produces a shadowed jar (`orelia-extra-<version>.jar`) via the Shadow plugin; `build` depends on `shadowJar`. Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves `orelia-core`, `orelia-world`, and VaultAPI straight from their GitHub repos).

```
./gradlew test
```

JUnit 5 (`useJUnitPlatform()`).

In-game: `/extrareload` (registered via `ExtraAdminCommand`) reloads every module's config file without a server restart.

## Architecture

### Module system

`OreliaExtraPlugin` (`rpg/extra/core/OreliaExtraPlugin.java`) is the single entry point. On `onEnable()` it looks up orelia-core's `PlayerDataManager` and command registries from `ServicesManager` — hard-fails (disables the plugin) if either is missing, since that means orelia-core isn't installed/enabled first. It then builds its own `ConfigManager` and `SchedulerService`, and registers every top-level feature as an `ExtraModule` (`rpg/extra/core/module/ExtraModule.java`) via `ExtraModuleManager`.

- Modules with no dependency on each other register in roughly alphabetical order: Party → Guild → Trade → Mail → Auction → Housing → Pet → Mount → Ranking → Achievement. Ranking/Achievement register last since they read state produced by the others (or by orelia-core/orelia-world directly) rather than owning anything themselves.
- Modules are enabled in registration order, **disabled in reverse order** (mirrors orelia-core's `ModuleManager` and orelia-world's `WorldModuleManager`).
- `onReload()` is optional (default no-op); implement it to re-read config in place — see `AchievementModule.reloadAchievements()` for the pattern.
- Do not let one module reach into another module's internal classes — go through the other module's public getters on its `ExtraModule` (e.g. `GuildModule#getGuildService`), or through `rpg.api`/`rpg.world.api` if the consumer is orelia-core/orelia-world.

### Per-module package shape

Most feature packages (`party`, `guild`, `trade`, `mail`, `auction`, `housing`, `pet`, `mount`, `ranking`, `achievement`) follow the same internal layering:

- `repository/` — either config-driven (parses a `*.yml` into in-memory templates, e.g. `AchievementConfigRepository`, `MountConfigRepository`, `PetConfigRepository`) or DB-backed (implements `rpg.database.repository.SchemaOwner`, e.g. `GuildRepository`, `AuctionRepository`, `MailRepository`). Never touches Bukkit events or game logic.
- `model/` — plain data holders (`Guild`, `TradeOffer`, `HousePlot`, `MailMessage`, ...).
- `manager/` or `service/` — business logic. Modules with no persistence (Party, in-memory only) keep state directly in a `manager/`; DB-backed and config-driven modules layer a `service/` on top of their repository.
- `listener/` — Bukkit event handlers wired in `onEnable` (mostly quit/join cleanup, e.g. `PartyQuitListener`, `TradeQuitListener`).
- `command/` — registered into the shared `/ol` dispatcher via `plugin.getPlayerCommandRegistry().register(name, ...)`, not as a new top-level Bukkit command.
- `gui/` — present where the module has a GUI screen (Auction, Mail, Ranking).

### Config

Config-driven modules each read their own file under `src/main/resources/`: `config.yml` (Party's `party.max-size` and other cross-module settings), `achievements.yml`, `housing.yml`, `mounts.yml`, `pets.yml`. `ConfigManager.register(name)` copies the bundled default out of the jar on first use. Reload all of them via `/extrareload`, which calls `configManager.reloadAll()` then `moduleManager.reloadAll()`.

### Database

DB-backed modules (Guild, Mail, Auction, Housing ownership, Pet/Mount ownership, Achievement progress) each get orelia-core's shared `DatabaseManager` via `plugin.getServer().getServicesManager().load(DatabaseManager.class)` in their own `onEnable`, then create/migrate their own tables through a `SchemaOwner` repository — orelia-extra owns no schema centrally, same convention as orelia-core/orelia-world. Trade is a partial exception: an in-progress `TradeSession` itself is still in-memory only (a restart drops any open trade, returning items via the normal player-data save since they never left inventories on disk) — only the append-only audit log of *completed* trades (`trade_log`, `TradeLogRepository`) is DB-backed.

### Cross-module and cross-plugin dependencies

- `AchievementModule` is the widest-reaching module: it loads orelia-core's `StatusApi`/`SkillApi` (hard dependencies, throws `IllegalStateException` if missing), plus Vault's `Economy` and orelia-world's `QuestApi` (soft — may be `null` if orelia-world isn't installed, guard accordingly).
- `RankingModule` reads orelia-core's `StatusApi` directly for level data.
- When a module needs another orelia-extra module's service, fetch it via `plugin.getModuleManager().get(OtherModule.class)` (`Optional`), following the same fail-fast convention as orelia-core/orelia-world where the dependency is hard-required.

## Committing changes

When committing, also update README.md and README_EN.md accordingly.
