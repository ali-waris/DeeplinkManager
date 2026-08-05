# Graph Report - .  (2026-08-05)

## Corpus Check
- Corpus is ~10,421 words - fits in a single context window. You may not need a graph.

## Summary
- 168 nodes · 232 edges · 18 communities (14 shown, 4 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 5 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Project Conventions
- Tag Data Layer
- Deeplink Repo & DI
- CSV Import/Export
- Room Database Setup
- Compose UI Screens
- Flow Query Layer
- Deeplink DAO & Entity
- UI State & Sheet
- App Entry & Theme
- Deep Link Launching
- Gradle Wrapper
- Application Class
- App Launcher Icons

## God Nodes (most connected - your core abstractions)
1. `TagEntity` - 19 edges
2. `AGENTS.md (project conventions & invariants)` - 18 edges
3. `DeeplinkDao` - 11 edges
4. `README.md (Deeplink Manager)` - 11 edges
5. `TagDao` - 10 edges
6. `MainScreen()` - 10 edges
7. `DeeplinkWithTag` - 9 edges
8. `DeeplinkRepository` - 9 edges
9. `DeeplinkRepositoryImpl` - 9 edges
10. `DeeplinkDatabase` - 8 edges

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

## Communities (18 total, 4 thin omitted)

### Community 0 - "Project Conventions"
Cohesion: 0.09
Nodes (29): AGENTS.md (project conventions & invariants), Build & test (assembleDebug / testDebugUnitTest), Case-insensitive unique tag names, CSV import/export (CsvExporter, CsvImporter, ExportStorage), gradle/libs.versions.toml version catalog (libs.* aliases), Destructive Room migration wipes user data, Client-side duplicate URL guard, Hilt dependency injection setup (+21 more)

### Community 1 - "Tag Data Layer"
Cohesion: 0.15
Nodes (4): TagDao, TagEntity, TagRepository, TagRepositoryImpl

### Community 2 - "Deeplink Repo & DI"
Cohesion: 0.13
Nodes (5): DeeplinkRepository, DeeplinkRepositoryImpl, DispatcherModule, RepositoryModule, CoroutineDispatcher

### Community 3 - "CSV Import/Export"
Cohesion: 0.17
Nodes (10): CsvExporter, CsvImporter, Error, ExportResult, ExportStorage, ImportResult, Context, ParsedDeeplink (+2 more)

### Community 4 - "Room Database Setup"
Cohesion: 0.17
Nodes (8): build(), Callback, DeeplinkDatabase, Context, DatabaseModule, Context, RoomDatabase, SupportSQLiteDatabase

### Community 5 - "Compose UI Screens"
Cohesion: 0.24
Nodes (13): androidx, AddEditDeeplinkSheet(), ConfirmDialog(), DeeplinkList(), DeeplinkRow(), DrawerContent(), EmptyState(), MainScreen() (+5 more)

### Community 6 - "Flow Query Layer"
Cohesion: 0.21
Nodes (3): Flow, DeeplinkWithTag, Flow

### Community 8 - "UI State & Sheet"
Cohesion: 0.22
Nodes (8): Add, All, Edit, Hidden, MainUiState, SheetState, Tag, TagFilter

### Community 9 - "App Entry & Theme"
Cohesion: 0.29
Nodes (4): MainActivity, DeeplinkManagerTheme(), Bundle, ComponentActivity

### Community 11 - "Gradle Wrapper"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **21 isolated node(s):** `All`, `Tag`, `Hidden`, `Add`, `Edit` (+16 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DeeplinkWithTag` connect `Flow Query Layer` to `CSV Import/Export`, `Compose UI Screens`?**
  _High betweenness centrality (0.192) - this node is a cross-community bridge._
- **Why does `TagEntity` connect `Tag Data Layer` to `Compose UI Screens`, `Flow Query Layer`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **What connects `All`, `Tag`, `Hidden` to the rest of the system?**
  _21 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Project Conventions` be split into smaller, more focused modules?**
  _Cohesion score 0.08620689655172414 - nodes in this community are weakly interconnected._
- **Should `Deeplink Repo & DI` be split into smaller, more focused modules?**
  _Cohesion score 0.1323529411764706 - nodes in this community are weakly interconnected._