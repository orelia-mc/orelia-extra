<img src="https://orelia-mc.github.io/assets/logo_wide.jpg" />
<h1 align="center">Orelia Extra</h1>
<p align="center">MMORPG Feature Plugin of Orelia-MC</p>

## About

`orelia-extra` は Minecraft RPG プラグイン群 **Orelia** の後発 MMORPG 機能プラグイン(Paper 1.21.x / Java 21)です。Party・Guild・Trade・Mail・Auction・Housing・Pet・Mount・Ranking・Achievement を提供します。

Orelia は 3 プラグイン構成です。

- [orelia-core](https://github.com/orelia-mc/orelia-core) — 戦闘・プレイヤー・ステータスの基盤(必須依存)
- [orelia-world](https://github.com/orelia-mc/orelia-world) — クエスト・NPC・ストーリーのコンテンツ層(ソフト依存)
- **orelia-extra**(本リポジトリ) — 後発の MMORPG 系機能

全 10 モジュールが実装済みで、それぞれ `OreliaExtraPlugin#onEnable` に登録される `ExtraModule` として動作し、orelia-core / orelia-world とは公開 API(`rpg.api` / `rpg.world.api`)経由でのみ連携します(ゲームプレイモジュールの内部には触れません)。

- **Party**(`/ol party`) — インメモリのパーティ機能(create/invite/accept/leave/kick/disband)
- **Guild**(`/ol guild`) — DB 永続化されたギルド(leader/officer/member ロール)
- **Trade**(`/ol trade`) — confirm/confirm ハンドシェイクによる 2 人間アイテム取引
- **Mail**(`/ol mail`) — アイテム添付・GUI 受信箱付きの DB 永続化メールボックス
- **Auction**(`/ol auction`) — 期限付き出品のプレイヤー主導オークションハウス(決済は Vault)
- **Housing**(`/ol house`) — 設定駆動の購入可能な住居プロット(`/ol house home` でテレポート)
- **Pet**(`/ol pet`) — 設定駆動の追従ペット(unlock/summon/dismiss)
- **Mount**(`/ol mount`) — 設定駆動の騎乗マウント(unlock/summon/dismiss)
- **Ranking**(`/ol ranking`) — レベルランキング GUI(orelia-core の `StatusApi` を直接参照)
- **Achievement**(`/ol achievement`) — 設定駆動の実績(レベル/クエスト/所持金条件、報酬は `SkillApi` 経由)

## Setup

```bash
./gradlew build
```

ビルドには `repo.papermc.io`(Paper API)と `jitpack.io`(orelia-core・orelia-world・Vault API を GitHub から直接解決)へのネットワークアクセスが必要です。
