<img src="https://orelia-mc.github.io/assets/logo_wide.jpg" />
<h1 align="center">Orelia Extra</h1>
<p align="center">MMORPG Feature Plugin of Orelia-MC</p>

## About

`orelia-extra` is the later MMORPG-feature plugin (Paper 1.21.x / Java 21) of the Minecraft RPG plugin suite **Orelia** — Party, Guild, Trade, Mail, Auction, Housing, Pet, Mount, Ranking, Achievement.

Orelia is split into 3 plugins:

- [orelia-core](https://github.com/orelia-mc/orelia-core) — combat/player/status foundation (required dependency)
- [orelia-world](https://github.com/orelia-mc/orelia-world) — quest/NPC/story content layer (soft dependency)
- **orelia-extra** (this repo) — later MMORPG features

All 10 modules are implemented, each as an `ExtraModule` registered in `OreliaExtraPlugin#onEnable`, talking to orelia-core/orelia-world only through their published `rpg.api`/`rpg.world.api` interfaces (never gameplay-module internals):

- **Party** (`/ol party`) — in-memory party grouping (create/invite/accept/leave/kick/disband)
- **Guild** (`/ol guild`) — DB-persisted guilds with leader/officer/member roles
- **Trade** (`/ol trade`) — two-player item trading with a confirm/confirm handshake
- **Mail** (`/ol mail`) — DB-persisted mailbox with item attachments, GUI inbox
- **Auction** (`/ol auction`) — player-run auction house with timed listings, settles via Vault
- **Housing** (`/ol house`) — config-driven purchasable house plots with `/ol house home` teleport
- **Pet** (`/ol pet`) — config-driven follower pets (unlock/summon/dismiss)
- **Mount** (`/ol mount`) — config-driven rideable mounts (unlock/summon/dismiss)
- **Ranking** (`/ol ranking`) — level leaderboard GUI, reads orelia-core's `StatusApi` directly
- **Achievement** (`/ol achievement`) — config-driven achievements (level/quest/money conditions), rewards via `SkillApi`

## Setup

```bash
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves orelia-core, orelia-world, and Vault API straight from GitHub).
