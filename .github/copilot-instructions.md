<!--
Brief, actionable instructions for AI coding agents working on this repo.
Focus: what to read first, build/test commands, project-specific patterns and examples.
-->

# Copilot / AI agent instructions

This repository is a Maven plugin that computes differences between two OpenAPI (Swagger v2) specifications and generates a changelog (HTML and/or Markdown).

Key entry points
- `pom.xml` — build configuration, Java source/target (1.7) and important plugin profiles (see `run-its`).
- `src/main/java/fr/laposte/disbr/maven/plugins/swaggerdiff/DiffMojo.java` — main Mojo (Maven goal) implementation. Read this first to understand plugin parameters and outputs.
- `src/main/java/fr/laposte/disbr/maven/plugins/swaggerdiff/out/HtmlRender.java` — example of HTML rendering (uses `j2html` style builders and inlined `swaggerdiff.css`).
- `src/main/java/fr/laposte/disbr/maven/plugins/swaggerdiff/out/OutputStyle.java` — supported output formats (`HTML`, `MARKDOWN`).
- `src/test/java/.../MyMojoTest.java` and `src/test/resources/project-to-test/` — unit/integration test pattern using the Maven Plugin Testing Harness and a small test project.

Why the structure is the way it is
- This is a `maven-plugin` packaging project; the code implements Mojos (extending `AbstractMojo`) that are executed by Maven.
- The plugin delegates API diff work to the external library `swagger-diff` and provides two renderers (Markdown via `MarkdownRender` and HTML via `HtmlRender`).
- `maven-plugin-plugin` is configured to produce mojo descriptors and the help goal. The `invoker` profile (`run-its`) runs integration tests against an example project.

Important developer workflows
- Build and run unit tests:
  - `mvn clean test` or `mvn -DskipTests=false clean install` to run tests and build the plugin artifact.
- Run the plugin help (useful to inspect parameters):
  - `mvn fr.laposte.dsibr.maven.plugins:swagger-diff-maven-plugin:help -Ddetail=true -Dgoal=diff`
- Run integration tests (invoker profile):
  - `mvn -Prun-its verify` — this invokes the `maven-invoker-plugin` configured in `pom.xml`.

Project-specific conventions & patterns for agents
- Mojo configuration: parameters are annotated with `@Parameter(property = "...", ...)`. To change behavior, search and update these fields in `DiffMojo.java`.
- Artifact attachment: the plugin uses `projectHelper.attachArtifact(project, extension, "swagger-diff", outputFile)` — attached artifacts use the classifier `swagger-diff`.
- Output defaults: `outputDirectory` default is `${project.build.directory}/swagger/` and `outputFilePrefix` default is `swagger-diff`.
- Rendering: `HtmlRender` loads `swaggerdiff.css` via the classpath (`ThreadContextClasspathResourceLoader`) — when changing CSS, update `src/main/resources/swaggerdiff.css`.
- Tests: unit tests use `AbstractMojoTestCase` (from `maven-plugin-testing-harness`). The sample test project lives in `src/test/resources/project-to-test/` and includes `oldApi.json`.

Integration points & external dependencies
- Relies on `com.deepoove:swagger-diff` for computing diffs.
- Uses Maven internals: `maven-plugin-api`, `maven-core`, `MavenProjectHelper`, and Plexus `BuildContext` for IDE integration.
- `HtmlRender` uses `j2html`-style static imports (check pom or transitive deps if you change rendering).

Code style & compatibility notes
- The project targets Java 1.7 (`maven.compiler.source`/`target`). Avoid Java 8+ language features unless you also update `pom.xml` and CI. Keep method-style, no lambdas.
- The codebase uses explicit IO handling and commons-io (`IOUtils`). Follow existing patterns for resource management (try/catch/finally and quiet close).

Where to look for follow-ups
- If changing Mojo parameters or attachments: update `DiffMojo.java` and test(s) in `src/test`.
- If changing render output: update `HtmlRender.java`, `OutputStyle.java` and `src/main/resources/swaggerdiff.css`.
- If adding integration tests: add example projects under `src/test/resources` and run with `-Prun-its`.

If parts are unclear, ask for which target (unit tests, integration tests, or publishing the plugin) you want help with and I'll expand with example patches and commands.
