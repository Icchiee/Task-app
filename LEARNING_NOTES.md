# task-app 学習ノート

> このファイルは task-app 開発を通じて学んだことを記録するためのノートです。
> 機能を実装したら、その都度ここに記録していきましょう。

---

## プロジェクト概要

| 項目 | 内容 |
|------|------|
| フレームワーク | Spring Boot 3.5.0 |
| Java | 21 |
| フロントエンド | Thymeleaf |
| DB | PostgreSQL (localhost:5434) |
| マイグレーション | Flyway |

---

## 習得目標スキル

- [ ] ページネーション（`Pageable`）
- [ ] 複合検索（`Specification`）
- [ ] バリデーション（`@Valid` / Bean Validation）
- [ ] 状態遷移（タスクのステータス管理）
- [ ] JWT組み込み（Thymeleafへの適用）

---

## 機能別 学習記録

### 機能1: ユーザー登録・ログイン

**実装日:** <!-- 例: 2026-06-15 -->

**学んだこと:**
<!-- ここに記録 -->

**つまずいたポイントと解決策:**
<!-- ここに記録 -->

**疑問メモ:**
<!-- 実装中に湧いた疑問を記録。解決したら → に答えを書く。
例:
- Q: パスワードのハッシュ化はどの層でやるべきか？ → A: Service層。Controller は生パスワードを受け取り、Service で BCrypt にかけて保存する。
-->

**重要なコード・設定:**
```java
// ここに印象に残ったコードを貼る
```

---

### 機能2: タスク作成

**実装日:**

**学んだこと:**

**つまずいたポイントと解決策:**

**疑問メモ:**
<!--
- Q:
-->

**重要なコード・設定:**
```java

```

---

### 機能3: タスク一覧（ページネーション・フィルタ）

**実装日:**

**学んだこと:**

#### Pageable について
<!--
例:
- `Pageable` は Spring Data JPA のインターフェース。ページ番号・件数・ソートを一括で扱える。
- Controller の引数に `@PageableDefault(size = 10) Pageable pageable` と書くだけで自動バインドされる。
- URL: /tasks?page=0&size=10&sort=createdAt,desc
-->

#### Specification について
<!--
例:
- 複数の検索条件を AND/OR で動的に組み合わせる仕組み。
- `JpaSpecificationExecutor<T>` を Repository に継承させることで使えるようになる。
- 条件が null のときはスキップする実装がポイント。
-->

**つまずいたポイントと解決策:**

**疑問メモ:**
<!--
- Q:
-->

**重要なコード・設定:**
```java

```

---

### 機能4: タスク詳細・編集

**実装日:**

**学んだこと:**

**つまずいたポイントと解決策:**

**疑問メモ:**
<!--
- Q:
-->

**重要なコード・設定:**
```java

```

---

### 機能5: タスクのステータス変更

**実装日:**

**学んだこと:**

#### 状態遷移について
<!--
例:
- ステータスを Enum で管理し、遷移可能な状態をメソッドで制御する。
- 不正な遷移（例: 完了→未着手）はサービス層で弾く。
-->

**つまずいたポイントと解決策:**

**疑問メモ:**
<!--
- Q:
-->

**重要なコード・設定:**
```java

```

---

### 機能6: タスク削除

**実装日:**

**学んだこと:**

**つまずいたポイントと解決策:**

**疑問メモ:**
<!--
- Q:
-->

**重要なコード・設定:**
```java

```

---

## 横断的なトピック

### Spring Security + Thymeleaf の連携

<!--
例:
- `thymeleaf-extras-springsecurity6` を使うと、テンプレート内で `sec:authorize` 属性が使える。
- ログイン中のユーザー名の表示: <span th:text="${#authentication.name}"></span>
-->

### Flyway マイグレーション

<!--
例:
- ファイル名規則: V{バージョン}__{説明}.sql  例: V1__create_users_table.sql
- 一度適用したファイルは変更してはいけない（チェックサムで管理されているため）。
-->

### バリデーション

<!--
例:
- `@Valid` を Controller の引数につけると Bean Validation が動く。
- `BindingResult` で検証エラーをキャッチして、画面に返す。
-->

### JWT + Thymeleaf

<!--
例:
- REST API（auth-api）で学んだJWTをどうThymeleafアプリに適用するか？
- セッションベース vs トークンベース の違いは？
-->

---

## 疑問ログ（全体）

> 機能に紐づかない疑問や、複数機能にまたがる疑問をここに集約する。
> ステータスを `[未解決]` / `[解決済]` で管理する。

| # | ステータス | 疑問 | 答え・メモ |
|---|-----------|------|-----------|
| 1 | [解決済] | JDBCとは何か？ | JavaとDBの橋渡しをする標準API。SQLの発行・結果取得を担う低レベルAPI。JPA/Hibernateはこの上に構築されており、Spring Boot + JPA を使う場合は直接書く機会はほぼない。`application.yml` の `datasource.url: jdbc:postgresql://...` がJDBC接続URL。 |

<!--
記入例:
| 2 | [解決済] | Thymeleaf で CSRF トークンはどう扱う？ | form タグに th:action を使えば Spring Security が自動で hidden input を埋め込んでくれる。 |
| 3 | [未解決] | Specification と @Query はどう使い分ける？ | |
-->

---

## chat-app・auth-api との比較

| 観点 | chat-app | auth-api | task-app |
|------|----------|----------|----------|
| スタイル | Thymeleaf MVC | REST API | Thymeleaf MVC |
| 認証 | Spring Security (session) | JWT | 未実装 |
| DB操作 | JPA (基本) | JPA | JPA + Specification |
| 新しく学ぶこと | - | JWT, REST設計 | Pageable, Specification, 状態遷移 |

---

## 気づき・感想

<!-- 開発全体を通じて感じたことを自由に記録 -->

---

## 参考にしたリソース

<!-- 役立ったドキュメント・記事などを記録 -->

- [Spring Data JPA - Pagination](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html)
- [Spring Data JPA - Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)
- [Thymeleaf + Spring Security](https://github.com/thymeleaf/thymeleaf-extras-springsecurity)
