# orelia-extra

Later MMORPG-feature plugin for Orelia (Paper 1.21.x / Java 21) - Party, Guild, Trade,
Mail, Auction, Housing, Pet, Mount, Ranking, Achievement.

Part of the Orelia 3-plugin split:

- [orelia-core](https://github.com/rasp1220/orelia-core) - combat/player/status foundation (required dependency)
- [orelia-world](https://github.com/rasp1220/orelia-world) - quest/NPC/story content layer (soft dependency)
- **orelia-extra** (this repo) - later MMORPG features

All 10 modules are implemented, each as an `ExtraModule` registered in
`OreliaExtraPlugin#onEnable`, talking to orelia-core/orelia-world only through their
published `rpg.api`/`rpg.world.api` interfaces (never gameplay-module internals):

- **Party** (`/party`) - in-memory party grouping (create/invite/accept/leave/kick/disband)
- **Guild** (`/guild`) - DB-persisted guilds with leader/officer/member roles
- **Trade** (`/trade`) - two-player item trading with a confirm/confirm handshake
- **Mail** (`/mail`) - DB-persisted mailbox with item attachments, GUI inbox
- **Auction** (`/auction`) - player-run auction house with timed listings, settles via Vault
- **Housing** (`/house`) - config-driven purchasable house plots with `/house home` teleport
- **Pet** (`/pet`) - config-driven follower pets (unlock/summon/dismiss)
- **Mount** (`/mount`) - config-driven rideable mounts (unlock/summon/dismiss)
- **Ranking** (`/ranking`) - level leaderboard GUI, reads orelia-core's `StatusApi` directly
- **Achievement** (`/achievement`) - config-driven achievements (level/quest/money conditions), rewards via `SkillApi`

## Building

```
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves
orelia-core, orelia-world, and Vault API straight from GitHub).
