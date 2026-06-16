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

**実装日:** 2026-06-14

**学んだこと:**

- Flyway のマイグレーションファイルは `V{番号}__{説明}.sql` の命名規則。一度適用したら変更禁止（チェックサム管理）
- `ddl-auto: validate` により Entity と DB スキーマの不一致をアプリ起動時に検出できる
- JPA Entity の設計原則：`@NoArgsConstructor(access = PROTECTED)` でJPA用コンストラクタを保護、`@Builder` + `@AllArgsConstructor` で外部からの生成を Builder 経由に統一
- `id` はプリミティブ `long` ではなくラッパー型 `Long` を使う（未保存は null で表現できる）
- Entity に `@Setter` を付けない（フィールドの変更は専用メソッドで制御する）
- Spring Data JPA Repository はメソッド名からSQLを自動生成する。`existsByUsername` のように `exists` prefix も使える
- `@Repository` アノテーションは不要。`JpaRepository` を継承するだけで Spring が Bean として認識する
- DTO には `@Getter` + `@Setter` が必要（`@ModelAttribute` でフォームをバインドするため）
- `UserDetailsService` を implements した Service を Bean 登録するだけで Spring Security がフォームログインに自動で使う
- `PasswordEncoder` の Bean を `SecurityConfig` に定義しておかないと `UserService` が起動時に注入失敗する
- `SecurityFilterChain` の書き方（新スタイル）：`WebSecurityConfigurerAdapter` は非推奨、Bean 定義スタイルで書く
- PRG パターン（Post/Redirect/Get）：POST 成功後は `redirect:` を返してブラウザの二重送信を防ぐ
- `@Valid` + `BindingResult` をセットで使う。`BindingResult` は `@Valid` の直後の引数に置く必要がある
- `bindingResult.hasErrors()` チェックを先に行い、エラーがあればフォームに戻す
- `@WebMvcTest`：Controller レイヤーだけ起動する高速テスト。`@MockBean` で Service を差し替える
- `@WebMvcTest` での POST テストには `.with(csrf())` が必要

**つまずいたポイントと解決策:**

- `extends UserDetailsService` と書いてしまった → インターフェースなので `implements` が正しい
- テストで `User` のインポートが Spring Security の `User` と Entity の `User` で衝突した → Entity をインポートし、Spring Security の `User` は完全修飾名で使う
- `@RequiredArgsConstructor` があっても `final` を付け忘れると注入されない（NullPointerException）
- `TaskAppApplicationTests.contextLoads` が失敗した → `SecurityConfig` が未作成で `PasswordEncoder` Bean がなかったため

**重要なコード・設定:**
```java
// UserDetailsService の正しい実装パターン
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        .roles("USER")
        .build();
}

// SecurityFilterChain の新スタイル定義
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/tasks", true)
        );
    return http.build();
}
```

---

### 機能2: タスク作成

**実装日:** 2026-06-16（実装中）

**学んだこと:**

- テーブル設計の基本：`user_id`（FK）は「誰のデータか」を示す最重要カラム。これがないとログインユーザーとデータを紐づけられない
- FK 制約は SQL の `CREATE TABLE` 内でカラム定義の**最後**に書く
- `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "user_id")` で Entity 間のリレーションを定義
- `FetchType.LAZY`（遅延取得）が商用の基本。`EAGER` は毎回 JOIN が走るためパフォーマンスが悪化する
- `@PreUpdate` で更新時に `updatedAt` を自動セット（`@PrePersist` は作成時のみ）
- `@AuthenticationPrincipal UserDetails currentUser` でコントローラーからログインユーザーを取得する
- Service の `create()` 戻り値は `void`：呼び出し元（Controller）がリダイレクトするだけで戻り値を使わないため
- `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` を DTO の `LocalDate` フィールドに付けると HTML の `<input type="date">` からの文字列を自動変換できる
- 実装前に「フロー・責務・DTO の入出力・エラーケース」を言語化してから書き始める習慣が商用の基本

**つまずいたポイントと解決策:**

- `@Generated`（jakarta.annotation）を `@GeneratedValue` と間違えた → JPA の `GeneratedValue` は `jakarta.persistence` パッケージ
- FK 制約をカラム定義の途中に書いてしまい SQL 構文エラー → 末尾に移動
- DTO の `dueDate` を `String` で定義してしまった → `LocalDate` + `@DateTimeFormat` が正しい

**重要なコード・設定:**
```java
// ログインユーザーをコントローラーで取得するパターン
@PostMapping("/tasks")
public String create(
        @AuthenticationPrincipal UserDetails currentUser,
        @Valid @ModelAttribute("form") TaskCreateRequest request,
        BindingResult bindingResult) {
    if (bindingResult.hasErrors()) return "task/new";
    taskService.create(request, currentUser.getUsername());
    return "redirect:/tasks";
}

// Entity のリレーション定義
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

// 更新日時の自動セット
@PreUpdate
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
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
| 2 | [解決済] | `Model` はViewで使うオブジェクトか？ | はい。ControllerからThymeleafテンプレートへデータを渡すための「荷物袋」。`model.addAttribute("名前", 値)` で詰め、テンプレート側で `${名前}` で受け取る。 |
| 3 | [解決済] | `addAttribute` は何をしているか？ | Modelに「名前→値」のペアでデータを登録するメソッド。例: `model.addAttribute("tasks", taskList)` → Thymeleafで `${tasks}` として参照できる。 |
| 4 | [解決済] | `@ModelAttribute("request")` とは何か？ | フォームの入力値をJavaオブジェクトに自動バインドするアノテーション。`"request"` という名前はThymeleafの `th:object="${request}"` と対応する。名前を省略するとクラス名の先頭小文字が自動で使われる。 |
| 5 | [解決済] | Mockito とは何か？ | テスト時に本物の代わりに動く「偽物（モック）」を作るライブラリ。`when(...).thenReturn(...)` で偽物の振る舞いを定義できる。DBに接続せずにServiceのロジックだけをテストしたい場面で使う。 |
| 6 | [解決済] | `@MockBean` とは何か？ | MockitoをSpringのテストで使いやすくしたアノテーション。SpringのDIコンテナに偽物のBeanを登録し、本物と差し替える。`@SpringBootTest` と組み合わせて使う。 |
| 7 | [解決済] | `@Autowired` とは何か？ | SpringのDIコンテナが管理するBeanを自動で注入するアノテーション。本番コードではコンストラクタ注入（`final` + `@RequiredArgsConstructor`）が推奨。テストでは `@SpringBootTest` 起動後にコンテナからBeanを取り出す用途で使う。 |
| 8 | [解決済] | `th:if="${param.error}"` 等はどういう仕組みか？ | `param.xxx` はURLのクエリパラメータ `?xxx` を読むThymeleafの機能。`?error` と `?logout` は Spring Security が認証失敗・ログアウト時に自動で付けてリダイレクトする。`?registered` は自分のControllerが登録成功後に `redirect:/login?registered` と付ける。@Valid のバリデーションとは別の仕組みで、メッセージ表示専用のURL通知パターン。 |
| 9 | [解決済] | Controllerに `?logout` 等のルートがないのに動くのはなぜか？ | クエリパラメータはルーティングに影響しない。`/login?logout` も `/login?error` も同じ `@GetMapping("/login")` が受け取る。`/logout` 自体は Spring Security のフィルターが横取りして処理するためControllerに届かない。Controllerには `@GetMapping("/login")` の1つだけあれば全パターン対応できる。リダイレクト先は SecurityConfig の `logoutSuccessUrl("/login?logout")` で設定する。 |
| 10 | [解決済] | POST /logout がなぜ GET /login?logout として届くのか？ | Spring Security が POST /logout を処理した後 HTTP 302 リダイレクトを返す。ブラウザは 302 を受け取ると自動的に GET に切り替えてリダイレクト先へアクセスする（HTTP仕様）。POST→GET の変換は Spring Security ではなくブラウザの動作。 |
| 11 | [解決済] | Spring Security のフィルタリングは自動か？ | 自動。`spring-boot-starter-security` を追加するだけでフィルターチェーン（LogoutFilter・UsernamePasswordAuthenticationFilter等）が有効になる。SecurityConfig はデフォルト設定を上書きするためのもの。フィルターチェーンはControllerより手前で動作し、条件に合うリクエストはControllerに届く前に処理が完了する。 |
| 12 | [解決済] | HTTPステータス302とは何か？ | 「一時的に別のURLへ移動してください」を意味するリダイレクト。サーバーが302とLocationヘッダーを返すと、ブラウザが自動でそのURLにGETリクエストを送る。Springの `return "redirect:/xxx"` は302を返す。301（永久移動・キャッシュされる）との違いに注意。主要コード: 200=成功, 201=作成成功, 302=一時リダイレクト, 400=リクエスト不正, 401=未認証, 403=権限なし, 404=見つからない, 500=サーバーエラー。 |
| 13 | [解決済] | `GET /login` だけ用意して `?logout` がなくても `/login?logout` に到達できるか？ | できる。クエリパラメータはルーティングに影響しない。`/login?logout` も `/login?error` も `/login` も全て `@GetMapping("/login")` が受け取る。`?logout` はThymeleafがメッセージ表示の判断に使う付加情報であり、ルーティングの条件ではない。 |
| 14 | [解決済] | `@ManyToOne` / `FetchType.LAZY` / `@JoinColumn` とは何か？ | `@ManyToOne` は多対1のリレーション定義（タスク多:ユーザー1）。`FetchType.LAZY` は遅延取得で `getUser()` を呼んだ瞬間にSELECTされる（商用現場の基本。EAGERは毎回JOIN されパフォーマンス悪化）。`@JoinColumn(name = "user_id")` はDBの外部キーカラム名を指定。`nullable = false` はNULL禁止。 |
| 15 | [解決済] | `@RequiredArgsConstructor` がServiceで宣言される理由は？ | `final` フィールド全てを引数に持つコンストラクタをLombokが自動生成するアノテーション。Springはコンストラクタが1つなら自動でDI注入する（Spring 4.3以降）。コンストラクタ注入が推奨される理由は「finalで変更不可・テストで直接生成できる・依存関係が明確・@Autowired不要」の4点。フィールド注入（`@Autowired` 直付け）は商用現場では非推奨。 |
| 16 | [解決済] | JPA `save()` の戻り値は何か？戻り値を受け取らない場合は？ | 戻り値は保存後のエンティティ（T）。新規作成（IDなし）は `persist()` が呼ばれ元オブジェクト自体にIDが書き込まれるため戻り値を受け取らなくてもOK。更新（IDあり）は `merge()` が呼ばれ元オブジェクトとは別の新しいオブジェクトが返るため必ず戻り値を使う必要がある。どちらか不明な場合は常に受け取るのが安全。 |
| 17 | [解決済] | Spring が `@Repository` なしで Repository を認識する仕組みは？ | `JpaRepository` を継承しているインターフェースは起動時のスキャンで自動検出され、実装クラスと Bean 登録が行われる。`Repository` が継承ツリーの頂点にある Spring Data のマーカーインターフェースであるため。`@Repository` は JdbcTemplate など Spring Data を使わない実装でのみ必要。 |
| 18 | [解決済] | `@Mock` と `@InjectMocks` の違いは？ | `@Mock` は依存オブジェクトの偽物を作る。`@InjectMocks` はテスト対象の本物クラスを生成し、`@Mock` で作った偽物を自動注入する。単体テストの目的は「対象クラスのロジックのみ」を検証することで、DB 等の外部依存を偽物で切り離せる。 |
| 19 | [解決済] | `@Builder` + `@AllArgsConstructor` + `@NoArgsConstructor` を同時に使う必要があるのはなぜか？ | `@Builder` は内部で全フィールドの引数コンストラクタを自動生成するが、`@NoArgsConstructor` があると生成を止める。そのため `@AllArgsConstructor` を明示して Builder が使えるコンストラクタを提供し、`@NoArgsConstructor` は JPA の要件（引数なしコンストラクタ必須）を満たすために残す。3つセットが JPA Entity + Builder の正しい組み合わせ。 |
| 20 | [解決済] | `loadUserByUsername` の戻り値を自前クラスにできるか？ | インターフェースの契約上、戻り値は `UserDetails`（またはそれを implements したクラス）でなければならない。自前クラスに `implements UserDetails` すれば可能だが、`password` の公開やセキュリティと DTO の責務混在という設計上の問題が生じる。商用では Spring Security の `User` クラスか専用の `UserDetailsImpl` クラスを使い、レスポンス DTO とは分ける。 |
| 21 | [解決済] | `UserDetailsService` を使わず自前認証にする方法は？ | `AuthenticationProvider` を implements して Bean 登録すると Spring Security が使う。ただし BCrypt 照合・セッション管理・CSRF などを自前実装する必要があり、フォームログインでは過剰。`AuthenticationProvider` が活きるのは多段階認証・LDAP 連携・複数認証源が必要な場面。 |
| 22 | [解決済] | `@WebMvcTest` と `@SpringBootTest` の違いは？ | `@SpringBootTest` はアプリ全体（DB・Service・Security 含む）を起動する。`@WebMvcTest` は Controller レイヤーのみ起動し高速。Controller テストには `@WebMvcTest`、全体の結合テストには `@SpringBootTest` を使うのが商用標準。`@WebMvcTest` では Service を `@MockBean` で差し替える（`@Mock` ではなく Spring コンテキストへの登録が必要なため）。 |
| 23 | [解決済] | `@Configuration` はいつ呼び出されるか？ | アプリ起動時（Spring の ApplicationContext 初期化フェーズ）に1回だけ処理される。Spring がクラスパスをスキャンして `@Configuration` クラスを見つけ、`@Bean` メソッドを実行して Bean を登録する。「呼び出される」ではなく「起動時に読まれて Bean を登録する設定ファイル」が正確なイメージ。 |
| 24 | [解決済] | Service の create() 戻り値は `void` か `Response` か？ | 呼び出し元（Controller）が戻り値を使うかどうかで決める。作成後にリダイレクトするだけなら `void`。作成後に詳細ページ（`/tasks/{id}`）へ飛ぶなら `id` または `Response` が必要。REST API では `201 Created + 作成リソース` を返すのが標準。「今必要な最小限を返す」が原則。 |

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
