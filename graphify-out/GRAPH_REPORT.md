# Graph Report - DeeplinkManager  (2026-08-06)

## Corpus Check
- 29 files · ~11,492 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 199 nodes · 267 edges · 19 communities (13 shown, 6 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5f617f89`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AGENTS.md (project conventions & invariants)
- TagEntity
- DeeplinkDao
- CsvExporter.kt
- DeeplinkDatabase
- MainScreen.kt
- DeeplinkWithTag
- ShareUrlExtractorTest
- MainViewModelTest
- MainActivity
- ShareUrlExtractor
- gradlew
- DeeplinkApp
- App Launcher Icon (debug source set)
- graphify.js

## God Nodes (most connected - your core abstractions)
1. `TagEntity` - 19 edges
2. `AGENTS.md (project conventions & invariants)` - 18 edges
3. `DeeplinkDao` - 11 edges
4. `README.md (Deeplink Manager)` - 11 edges
5. `TagDao` - 10 edges
6. `MainScreen()` - 10 edges
7. `ShareUrlExtractorTest` - 10 edges
8. `DeeplinkWithTag` - 9 edges
9. `DeeplinkRepository` - 9 edges
10. `DeeplinkRepositoryImpl` - 9 edges

## Surprising Connections (you probably didn't know these)
- `100% offline, data never leaves device` --semantically_similar_to--> `Built entirely offline; no network layer`  [INFERRED] [semantically similar]
  README.md → AGENTS.md
- `Tech stack table (Kotlin, Compose, MVVM, Hilt, Room)` --semantically_similar_to--> `Tech stack (Kotlin, Compose, MVVM/Clean, Hilt, Room)`  [INFERRED] [semantically similar]
  README.md → AGENTS.md
- `Tag & Organize feature (multi-tag, filter)` --conceptually_related_to--> `Ungrouped system tag (fixed id 1)`  [INFERRED]
  README.md → AGENTS.md
- `AGENTS.md (project conventions & invariants)` --references--> `README.md (Deeplink Manager)`  [EXTRACTED]
  AGENTS.md → README.md
- `CSV import/export (CsvExporter, CsvImporter, ExportStorage)` --references--> `Export feature (JSON file)`  [EXTRACTED]
  AGENTS.md → README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Domain invariants easy to violate** — agents_ungrouped_tag, agents_tag_delete_reassign, agents_duplicate_url_guard, agents_case_insensitive_tag_names, agents_upsert_id_zero [INFERRED 0.85]
- **Import/export subsystem with JSON-vs-CSV discrepancy** — agents_csv_import_export, readme_import, readme_export [INFERRED 0.85]
- **Core user features of Deeplink Manager** — readme_tag_organize, readme_add_deeplinks, readme_edit_anytime, readme_delete_confidence [INFERRED 0.85]

## Communities (19 total, 6 thin omitted)

### Community 0 - "AGENTS.md (project conventions & invariants)"
Cohesion: 0.09
Nodes (29): AGENTS.md (project conventions & invariants), Build & test (assembleDebug / testDebugUnitTest), Case-insensitive unique tag names, CSV import/export (CsvExporter, CsvImporter, ExportStorage), gradle/libs.versions.toml version catalog (libs.* aliases), Destructive Room migration wipes user data, Client-side duplicate URL guard, Hilt dependency injection setup (+21 more)

### Community 1 - "TagEntity"
Cohesion: 0.15
Nodes (4): TagDao, TagEntity, TagRepository, TagRepositoryImpl

### Community 2 - "DeeplinkDao"
Cohesion: 0.11
Nodes (5): DeeplinkDao, DeeplinkEntity, DeeplinkRepository, DeeplinkRepositoryImpl, RepositoryModule

### Community 3 - "CsvExporter.kt"
Cohesion: 0.17
Nodes (10): CsvExporter, CsvImporter, Error, ExportResult, ExportStorage, ImportResult, Context, ParsedDeeplink (+2 more)

### Community 4 - "DeeplinkDatabase"
Cohesion: 0.13
Nodes (10): build(), Callback, DeeplinkDatabase, Context, DatabaseModule, DispatcherModule, Context, CoroutineDispatcher (+2 more)

### Community 5 - "MainScreen.kt"
Cohesion: 0.13
Nodes (21): androidx, AddEditDeeplinkSheet(), ConfirmDialog(), DeeplinkList(), DeeplinkRow(), DrawerContent(), EmptyState(), MainViewModel (+13 more)

### Community 6 - "DeeplinkWithTag"
Cohesion: 0.21
Nodes (3): Flow, DeeplinkWithTag, Flow

### Community 9 - "MainActivity"
Cohesion: 0.16
Nodes (8): MainViewModel, MainActivity, DeeplinkManagerTheme(), DeeplinkLauncher, Context, Bundle, ComponentActivity, Intent

### Community 11 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **21 isolated node(s):** `All`, `Tag`, `Hidden`, `Add`, `Edit` (+16 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DeeplinkWithTag` connect `DeeplinkWithTag` to `CsvExporter.kt`, `MainScreen.kt`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `TagEntity` connect `TagEntity` to `MainScreen.kt`, `DeeplinkWithTag`?**
  _High betweenness centrality (0.140) - this node is a cross-community bridge._
- **Why does `MainScreen()` connect `MainScreen.kt` to `MainActivity`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **What connects `All`, `Tag`, `Hidden` to the rest of the system?**
  _21 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AGENTS.md (project conventions & invariants)` be split into smaller, more focused modules?**
  _Cohesion score 0.08620689655172414 - nodes in this community are weakly interconnected._
- **Should `DeeplinkDao` be split into smaller, more focused modules?**
  _Cohesion score 0.10507246376811594 - nodes in this community are weakly interconnected._
- **Should `DeeplinkDatabase` be split into smaller, more focused modules?**
  _Cohesion score 0.1286549707602339 - nodes in this community are weakly interconnected._