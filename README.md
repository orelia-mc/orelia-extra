# orelia-extra

Later MMORPG-feature plugin for Orelia (Paper 1.21.x / Java 21) - Party, Guild, Trade,
Mail, Auction, Housing, Pet, Mount, Ranking, Achievement.

Part of the Orelia 3-plugin split:

- [orelia-core](https://github.com/rasp1220/orelia-core) - combat/player/status foundation (required dependency)
- [orelia-world](https://github.com/rasp1220/orelia-world) - quest/NPC/story content layer (soft dependency)
- **orelia-extra** (this repo) - later MMORPG features

**No modules are implemented yet** - this repo is the bootstrap scaffold (build config,
`OreliaExtraPlugin` main class, `ExtraModule`/`ExtraModuleManager` lifecycle plumbing)
ready to receive Party/Guild/Trade/... modules as they're built, following the same
pattern as orelia-core/orelia-world: one `ExtraModule` per feature, registered in
`OreliaExtraPlugin#onEnable`, talking to orelia-core/orelia-world only through their
published `rpg.api` interfaces.

## Building

```
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves
orelia-core, orelia-world, and Vault API straight from GitHub).
