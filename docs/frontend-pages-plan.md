# Frontend Pages Plan

*Plan. Status: draft, as of 2026-09-24.*

This plan rebuilds the legacy Play app's pages (Twirl views on `master`) as routes in the React app
(`acrotesseract-frontend`). The URLs and page structure stay the same. The data comes from the new JSON API, and ids
are UUIDs. Each page also fixes the problems the old version had.

Phase 1 needs only the read endpoints that exist today (`/api/poses`, `/api/poses/{id}`, `/api/transitions`,
`/api/transitions/{id}`). Phase 2 needs the write API and Google sign-in from the modernization design doc.

## How the old site was organized

Every page except the graph used `main_with_navbar.scala.html`:

- **Top bar:** a dark Bootstrap bar with the **Acro Tesseract** brand, which links to `/`, and a hamburger button.
- **Hamburger panel:** it expanded a panel with an **About** blurb and links to **Login**, **Poses**, **Transitions**
  and **Graph**. There were no always-visible nav links.
- **Content:** each page rendered into a centered `container`.

The graph page used the bare `main` layout, so it had no navbar.

| URL | View | What it showed |
|---|---|---|
| `/` | `index` | A live search box (250 ms throttle, `GET /search`). Results were grouped into a **Poses** section and a **Transitions** section, each with a **New** button |
| `/poses` | `poses_list` | "List Poses" heading, a **New** button, and a one-column table of pose names linking to each pose |
| `/poses/:id` | `poses_pose` | A sub-bar "Pose: *name*" with **Edit**, **Graph** (`/graph?focusPose=id`) and **Delete** (browser `confirm`). Below it: the pose image, the description (raw text), then two tables, **Transitions From This Pose** and **Transitions To This Pose**, each with a **New** button that pre-fills the pose as `from` or `to` |
| `/poses/new`, `/poses/:id/edit` | `poses_pose_new`, `poses_pose_add_edit` | A form with Name, Description (single-line input) and Image Url |
| `/transitions` | `transitions_list` | "List Transitions" heading, a **New** button, and a table of names |
| `/transitions/:id` | `transitions_transitions` | A sub-bar "Transition: *name*" with **Edit** and **Delete**. Below it: a YouTube iframe (URL converted by `YoutubeUrlParser`), the description, and **From:** / **To:** links to the two poses |
| `/transitions/new?from=&to=`, `/transitions/:id/edit` | `transitions_transition_new`, `…_edit` | A form with Name, Description and Youtube Url, plus **From Pose** and **To Pose** as *raw id text inputs*. Each had a **Search** button that opened a modal to search poses by name and fill in the id |
| `/graph` | `graph` + `graph.js` | A full-screen Cytoscape + Cola graph. Clicking a node opened the pose |
| `/login`, `/admin` | `login`, `admin` | A Google SSO button and logout link; an editor-only admin page with counts, username and the JDBC string |

**Problems to fix in the port:**

- **Descriptions** were single-line inputs, and were shown as raw text even though they're Markdown.
- **Transitions were listed by name only,** so a pose page never showed where each transition goes.
- **Pose pickers were raw id text boxes.** That doesn't work with UUIDs, which nobody types by hand.
- **Edit and Delete buttons were shown to everyone,** even though the server didn't check who was editing.
- **The graph page** had no navbar, and it ignored `focusPose`.
- **There was no 404 page,** no per-page `<title>`, and no loading or error states.
- **The legacy `/search` endpoint doesn't exist in the new API.**

## Routes in the React app

The paths are the same as before, with UUIDs in place of numbers. CloudFront already rewrites extension-less paths
to `index.html`, so a deep link like `/poses/<uuid>` loads the SPA.

| Route | Page component | Data | Phase |
|---|---|---|---|
| `/` | `HomePage` | `usePoses()` + `useTransitions()`, filtered client-side | 1 |
| `/poses` | `PoseListPage` | `usePoses()` | 1 |
| `/poses/:poseId` | `PoseDetailPage` | `usePose(id)`, plus `usePoses()` to name the other end of each transition | 1 |
| `/transitions` | `TransitionListPage` | `useTransitions()` + `usePoses()` (to show *From → To*) | 1 |
| `/transitions/:transitionId` | `TransitionDetailPage` | `useTransition(id)` | 1 |
| `/graph?focusPose=` | `GraphPage` | `usePoses()` + `useTransitions()` (later `/api/graph`) | 1 |
| `*` | `NotFoundPage` | – | 1 |
| `/poses/new`, `/poses/:poseId/edit` | `PoseFormPage` | `usePose(id)` + mutation | 2 |
| `/transitions/new?from=&to=`, `/transitions/:transitionId/edit` | `TransitionFormPage` | `useTransition(id)`, `usePoses()` + mutation | 2 |
| `/login` | `LoginModal` (a dialog opened from the header, not a page) | `POST /api/auth/google` | 2 |
| `/admin` | `AdminPage` | `GET /api/admin/stats` | 2 |

## Layout

`AppShell` wraps every route, the graph included:

- **Header:** the brand links to `/`, followed by nav links (**Poses**, **Transitions**, **Graph**) that are always
  visible on wide screens. On narrow screens they collapse into a menu button, like the old hamburger. On the right
  is **Sign in**, or the user's name and **Sign out** (phase 2).
- **About text:** the old blurb ("explore the world of acroyoga, transition by transition…") moves to the home page
  as its intro, instead of hiding behind the hamburger.
- **Main:** a centered content column, max width around 960px with 16px side padding. The graph page uses the full
  width and height below the header.
- **Page title:** a small `usePageTitle("Front Bird")` hook sets `document.title` to `Front Bird · Acro Tesseract`.

## Page details

**Home (`/`)**
- Show the intro text and a search box, with Ground highlighted as the starting pose.
- Results render as two sections, **Poses** and **Transitions**, the same as before. Search is client-side: a
  case-insensitive substring match over the already-loaded lists, updated on each keystroke (no server round trip,
  so no throttling). With an empty query, show all poses and transitions.
- The **New** buttons appear only for editors (phase 2).

**Pose list (`/poses`)**
- A name-sorted list of links, as before. Add a transition count per pose (`N out · M in`) so the list is more
  useful than a bare table.
- **New** for editors.

**Pose detail (`/poses/:poseId`)**
- The header row holds the pose name and actions: **View in graph** for everyone, and **Edit** / **Delete** for
  editors.
- If there's an image, show it, capped at 300px high like the old `.primary-pose-img`.
- Render the description as sanitized Markdown.
- Two sections, **Transitions from this pose** and **Transitions to this pose**. Each row shows the transition name
  and the pose at the other end, for example `Front Bird to Throne → Throne`. Both parts are links. Editors also get a
  **New** button that links to `/transitions/new?from=<id>` or `?to=<id>`.
- For an unknown id, show the not-found state. A malformed id (the API returns 400) is also treated as not found.

**Transition list (`/transitions`)**
- A name-sorted list. Each row shows *name* and *From → To*.

**Transition detail (`/transitions/:transitionId`)**
- The header shows the name, with **Edit** / **Delete** for editors.
- A responsive 16:9 YouTube embed. `youtubeEmbedUrl()` is a TypeScript port of `YoutubeUrlParser`, handling
  `youtu.be`, `watch?v=`, `/embed/` and the `t` / `start` parameters, with unit tests using the old parser's example
  URLs.
- Markdown description.
- A **From → To** pair of pose links, shown as a small visual (`[Front Bird] → [Throne]`) rather than two lines of
  text.

**Graph (`/graph`)**
- Implemented as described in the modernization doc: Cytoscape + Cola in a `PoseGraph` component.
- Clicking a node opens its pose, and clicking an edge opens its transition.
- `?focusPose=<id>` centers and zooms on that pose and highlights its neighborhood.
- The layout re-runs on drag end instead of on every drag event.

**Forms (phase 2)**
- **`PoseFormPage`:** Name, Description (a multi-line Markdown editor with a preview tab) and Image URL (with a live
  image preview).
- **`TransitionFormPage`:** Name, Description, YouTube URL (with a live embed preview), and **From** / **To** pose
  pickers. Each picker is a searchable combobox of pose names that stores the UUID. It replaces both the raw id
  input and the old search modal. `?from=` and `?to=` pre-select the pickers.
- **Saving:** calls the write API, sending `version` for optimistic locking, and redirects to the detail page. A 409
  shows "this was changed by someone else — reload?".
- **Delete:** a confirm dialog (not `window.confirm`). A 409 on pose delete shows the API's "delete or re-point its
  N transitions first" message.

## Building blocks

| Piece | Purpose |
|---|---|
| `react-router` | Routing, `useParams`, `useSearchParams`, `<Link>` |
| `@tanstack/react-query` | Fetch and cache hooks (`usePoses`, `usePose`, `useTransitions`, `useTransition`). Lists are shared across pages, so moving between pages doesn't refetch |
| `api.ts` | A typed fetch client. Types mirror the API (`Pose`, `Transition`, `PoseDetail`, `TransitionDetail`), and later are generated from `api/openapi.yaml` |
| `react-markdown` + `remark-gfm` + `rehype-sanitize` | `<Markdown>` for descriptions (the same libraries Olympos uses, plus sanitizing) |
| `cytoscape` + `cytoscape-cola` | The graph page |
| Shared components | `AppShell`, `PageHeader` (title + action slot), `EntityLink`, `PoseLink`, `TransitionRow`, `SearchBox`, `YouTubeEmbed`, `Markdown`, `LoadingState` / `ErrorState` / `NotFound`, and in phase 2 `PosePicker` and `ConfirmDialog` |
| `useAuth()` | Phase 2. Wraps `/api/me` and exposes `isEditor`, which gates every Edit/New/Delete control. The server check stays authoritative |

**File layout:**

```
acrotesseract-frontend/src/
  main.tsx                 QueryClientProvider + BrowserRouter
  app/app.tsx              route table
  api/                     client.ts, types.ts, hooks.ts
  components/              AppShell, PageHeader, Markdown, YouTubeEmbed, PoseLink, ...
  pages/                   HomePage, PoseListPage, PoseDetailPage, TransitionListPage,
                           TransitionDetailPage, GraphPage, NotFoundPage (+ phase 2 forms, AdminPage)
  lib/                     youtube.ts, search.ts (+ *.spec.ts)
```

## Testing

- **Pages:** each page gets a Vitest + React Testing Library spec. It renders the page inside the router and query
  client, with `fetch` stubbed to return fixtures. It checks the loading state, the loaded content (names, links,
  from/to), and the not-found state.
- **Utilities:** `youtubeEmbedUrl` and search filtering get unit tests.
- **End to end:** a Playwright smoke test later, against the deployed site.

## Order of work

1. Add the dependencies, `AppShell`, the router, the API client and hooks, and `NotFoundPage`.
2. Build `PoseListPage`, `PoseDetailPage`, `TransitionListPage` and `TransitionDetailPage`, with `Markdown` and
   `YouTubeEmbed`.
3. Build `HomePage` with client-side search.
4. Build `GraphPage`.
5. Phase 2, after the write API and auth exist: sign-in, `useAuth`, the forms, delete, and `AdminPage`.
