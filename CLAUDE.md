# CLAUDE.md

Guidance for Claude Code in this repository.

## Project overview

Acro Tesseract is a wiki of acroyoga poses (graph nodes) and transitions (directed edges; parallel edges and
self-loops allowed). This branch rebuilds the legacy Play app (on `master`) as a serverless app. The design,
data model and roadmap are in `docs/acrotesseract-modernization.md`; read it before changing the data model,
auth or infrastructure.

The workspace follows the conventions of the Olympos repo (`~/git/Olympos`): Nx monorepo, top-level projects,
TypeScript CDK with a stages file, and Google sign-in exchanged for an HttpOnly session cookie.

## Structure

```
acrotesseract-frontend/   React 19 + Vite + Vitest. Nx targets: dev, build, test, typecheck, preview
  src/app/app.tsx         route table (paths mirror the legacy Play app; see docs/frontend-pages-plan.md)
  src/api/                typed fetch client + TanStack Query hooks for /api/poses and /api/transitions
  src/pages/              Home (client-side search), pose/transition list + detail, Graph (lazy-loaded Cytoscape)
  src/components/         AppShell (header/nav), shared UI (Markdown, YouTubeEmbed, lists), PoseGraph
  src/test/               renderApp() + stubApi() helpers and fixtures for page tests
acrotesseract-backend/    sbt build, Scala 3.9. Nx targets wrap sbt: build (lambda/assembly), test, serve
  modules/domain/         Pose, Transition (no AWS deps)
  modules/api/            Router (reads + POST/PUT writes), Validation, jsoniter-scala codecs, AcroRepository
                          trait + InMemoryRepository, StaticData (loads data/data.json, bundled as classpath data.json)
  modules/store/          DynamoDbRepository (two tables, optimistic locking, pose-existence transactions),
                          DynamoDbTables (schema for DynamoDB Local; keep in sync with StorageStack), Wiring (env → router)
  modules/lambda/         acrotesseract.Handler (APIGatewayV2HTTPEvent) -> acrotesseract-lambda.jar
  modules/local/          JDK HttpServer on :8080 (`nx serve`) and the `seed` main (`nx seed`)
acrotesseract-cdk/        CDK app: cdk/AcroTesseractCdkApp.ts, cdk/AcroTesseractStages.ts, cdk/stacks/*
data/                     data.json (placeholder poses + transitions, RDS column names) + data.schema.json
docs/                     design docs
```

## Commands

- Test everything: `npx nx run-many -t test`
- Backend: `npx nx test acrotesseract-backend`, `npx nx build acrotesseract-backend`, `npx nx serve acrotesseract-backend`
- Frontend: `npx nx dev acrotesseract-frontend` (port 4200, proxies `/api` to 8080), `npx nx test acrotesseract-frontend`
- CDK: `npx nx test acrotesseract-cdk`, `npx nx package acrotesseract-cdk` (synth; builds backend and frontend first),
  `npx nx deploy acrotesseract-cdk` (`-c quick` for hotswap)

## Conventions

- **AWS**: account `640110193230` (`acro-tesseract-prod`), region `us-west-2`, CLI profile `acrotesseract-prod`,
  SSO session `ferrocene`. CDK targets pass `--profile acrotesseract-prod`.
- **Stack names**: `acrotesseract-<purpose>-stack-<stage>`. Stages are listed explicitly in `AcroTesseractStages.ts`
  with a `stageName`; don't derive stage names from array positions.
- **IAM**: grant with CDK helpers (`table.grantReadWriteData(fn)`); never `table/*` or table-management actions,
  and don't hard-code role names.
- **Secrets**: SSM SecureStrings under `/acrotesseract/<stage>/`, created with `aws ssm put-parameter`.
  Google client IDs are public config, not secrets.
- **Data**: two DynamoDB tables per stage, `acrotesseract-poses-<stage>` and `acrotesseract-transitions-<stage>`. The
  schema lives in both `StorageStack.ts` and `DynamoDbTables.scala`; change them together. Every repository must pass
  `RepositoryContract` (in-memory in `api` tests, DynamoDB Local in `store` tests with `DYNAMODB_ENDPOINT` set).
- **Writes** are off on deployed stages (`writesEnabled` in `AcroTesseractStages.ts` → `WRITES_ENABLED`) until Google
  sign-in exists. Don't turn them on in prod without auth.
- **Backend**: keep the Lambda cold-start path lean: no web framework, no reflection-based JSON. Build SDK clients
  and the router during init so SnapStart snapshots them. The Lambda asset path in `AcroTesseractCdkApp.ts` and
  the `build` output in `acrotesseract-backend/project.json` both include the Scala version; update both together.
- **CDK tests**: every stack has a `*.spec.ts` using `Template.fromStack`. Java Lambdas can't use inline code, so
  tests use a stub asset directory.
- **Build output is never committed** (`dist/`, `target/`, `cdk.out/`, `*.tsbuildinfo`).
- If sbt isn't installed locally, the backend can be built in a container:
  `podman run --rm -v "$PWD/acrotesseract-backend:/work" -w /work eclipse-temurin:21-jdk ...` with the sbt launcher.
