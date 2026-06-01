# 🔗 Deeplink Manager

> **Your offline-first toolkit for taming Android deeplinks.**  
> Organize, test, and manage every deeplink your app supports — all on-device, no internet required.

---

## 📖 Overview

**Deeplink Manager** is an Android utility app for developers and QA engineers who deal with deeplinks daily. Whether you're testing flows, onboarding teammates, or debugging routing issues — stop digging through Notion, Slack, or Confluence. Your deeplinks live here.

Built entirely offline. Your data never leaves your device.

---

## ✨ Features

### 🏷️ Tag & Organize
Group deeplinks by feature, team, environment, or anything you want. Add multiple tags per deeplink and filter by them instantly.

### ➕ Add Deeplinks
Save any deeplink with a name, description, URL scheme, and tags. Support for all URI formats — `scheme://host/path?params`.

### ✏️ Edit Anytime
Update deeplinks in place. Change URLs, rename tags, fix typos — no history lost.

### 🗑️ Delete with Confidence
Delete individual deeplinks or bulk-delete by tag. Confirmation dialogs keep you safe from accidents.

### 📥 Import
Bring in deeplinks from a JSON file. Useful for bootstrapping a team setup or restoring from a backup.

### 📤 Export
Export your full library (or filtered sets) to JSON. Share with teammates, commit to your repo, or back up locally.

### 🔒 100% Offline
No accounts. No sync. No analytics. No network calls — ever. Runs entirely on local storage.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Storage | Room (local SQLite) |

---


## 🤝 Contributing

Contributions are welcome! Please open an issue before submitting a PR so we can discuss the change.

1. Fork the repo
2. Create your branch: `git checkout -b feature/my-feature`
3. Commit: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

```
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<p align="center">
  Built with ❤️ for Android developers who deserve better tooling.
</p>
