<img src="https://orelia-mc.github.io/assets/logo_wide.jpg" />
<h1 align="center">Orelia Extra</h1>
<p align="center">MMORPG Feature Plugin of Orelia-MC</p>

## About

`orelia-extra` は Minecraft RPG プラグイン群 **Orelia** の後発 MMORPG 機能プラグイン(Paper 1.21.x / Java 21)です。Party・Friend・Guild・Chat・Trade・Mail・Auction・Housing・Pet・Mount・Ranking・Achievement を提供します。

Orelia は 3 プラグイン構成です。

- [orelia-core](https://github.com/orelia-mc/orelia-core) — 戦闘・プレイヤー・ステータスの基盤(必須依存)
- [orelia-world](https://github.com/orelia-mc/orelia-world) — クエスト・NPC・ストーリーのコンテンツ層(ソフト依存)
- **orelia-extra**(本リポジトリ) — 後発の MMORPG 系機能

全 12 モジュールが実装済みで、それぞれ `OreliaExtraPlugin#onEnable` に登録される `ExtraModule` として動作し、orelia-core / orelia-world とは公開 API(`rpg.api` / `rpg.world.api`)経由でのみ連携します(ゲームプレイモジュールの内部には触れません)。

- **Party**(`/ol party`) — インメモリのパーティ機能(create/invite/accept/decline/leave/kick/disband/transfer/chat。リーダーはleaveできずdisbandかtransferのみ)。招待を受け取ると「承認」「拒否」のクリック可能なチャットテキストが表示され、クリックだけで応答できる。参加/離脱/追放/解散はメンバー全員に通知され、リーダーがサーバーから切断した場合は自動でleave扱いになり(1人なら解散、2人以上なら残りメンバーへリーダー権限を自動譲渡)
- **Friend**(`/ol friend`) — DB 永続化された相互フレンドリスト(add/accept/decline/remove/list、招待はクリック承認/拒否)。オンラインのフレンドには`/ol friend list`から「メッセージ」(`/ol msg`の入力欄への差し込み)・「テレポート申請」ボタンが出る。テレポートはフレンド間のみ・双方合意制(申請→クリック承認)
- **Guild**(`/ol guild`) — DB 永続化されたギルド(leader/officer/member ロール、list/transfer/chat)。招待はクリックで`/guild accept`を実行できる。参加/離脱/追放/解散はメンバー全員に通知される
- **Chat**(`/ol chat`、トップレベル `/chat` エイリアス) — パブリック(デフォルト)/パーティー/ギルド/管理者の4チャンネルを切り替えるチャットチャンネル機能。`/oladmin chat <message>`・`/ol party chat <message>`・`/ol guild chat <message>` は現在の選択チャンネルを変えずに一度だけ送信する。`/ol msg <player> <message>`(トップレベル`/msg`エイリアス)は選択中チャンネルを変えずに1対1の個人メッセージを送る
- **Trade**(`/ol trade`) — confirm/confirm ハンドシェイクによる 2 人間アイテム取引
- **Mail**(`/ol mail`) — アイテム添付・GUI 受信箱付きの DB 永続化メールボックス
- **Auction**(`/ol auction`) — 期限付き出品のプレイヤー主導オークションハウス(決済は Vault)
- **Housing**(`/ol house`) — 設定駆動の購入可能な住居プロット(`/ol house home` でテレポート、`/ol house gui`でGUI一覧から購入・帰宅も可)
- **Pet**(`/ol pet`) — 設定駆動の追従ペット(unlock/summon/dismiss、`/ol pet gui`でGUI一覧から購入・召喚も可)
- **Mount**(`/ol mount`) — 設定駆動の騎乗マウント(unlock/summon/dismiss)
- **Ranking**(`/ol ranking`) — レベルランキング GUI(orelia-core の `StatusApi` を直接参照)
- **Achievement**(`/ol achievement [page]`はチャット表示、`/ol achievement gui`はGUI表示) — 設定駆動の実績(レベル/クエスト/所持金条件、報酬は `SkillApi` 経由)。各実績は`achievements.yml`の`category:`で任意のジャンルに分類でき、GUIはジャンル選択画面→各ジャンルの実績一覧(7件ごとにページ送り)という2段階構成です。`rpg.extra.api.AchievementApi`(`openGui(Player)`)を`ServicesManager`経由で公開しており、orelia-worldのプレイヤー情報メニュー(ネザースター)の「実績」アイコンはこのAPIでコマンドを経由せず直接GUIを開きます。

## Setup

```bash
./gradlew build
```

ビルドには `repo.papermc.io`(Paper API)と `jitpack.io`(orelia-core・orelia-world・Vault API を GitHub から直接解決)へのネットワークアクセスが必要です。

## config/messagesの自動移行・バージョン管理

`messages.yml`・`achievements.yml`・`housing.yml`・`mounts.yml`・`pets.yml`・`config.yml`はどちらも先頭の`config-version`で管理されており、新しいjarで起動すると新規追加されたキー(既存セクション内部のネストしたキーも含む)は既存ファイルの正しい位置へ自動で追記されます(orelia-coreの`ConfigMigrator`をjitpack経由で共有)。新しいトップレベルセクション・キーを追加したら、そのファイルの`config-version`を1つ上げてください。`main`へのpush(=PRマージ)ごとに`.github/workflows/version-bump.yml`が`build.gradle.kts`の`version`を自動でPATCHインクリメントし、タグを打ちます。互換性が壊れる変更は`bump:minor`、大規模な改修は`bump:major`ラベルをPRに付けてからマージしてください。
