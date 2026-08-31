# ⚡ Splixter v2.1.0 — Comprehensive Bug Fixes & Stability Release

Splixter v2.1.0 delivers a major overhaul addressing 20 functional and logical issues across the app. This update focuses on flawless financial ledger math, reliable cloud synchronization, atomic data persistence, and seamless navigation.

---

## 🚀 What's New & Fixed

### 💰 Financial & Settlement Accuracy
- **Decoupled Settlement Math**: Fixed balance calculations so completed settlements dynamically adjust debtor/creditor balances without corrupting the historical direct expense spend (`totalPaid`).
- **Instant Settlement Calculation Cache**: Integrated settlement records into dynamic cache invalidation keys so calculations update automatically upon recording transfers.

### 🛡️ Persistence & State Management
- **Current User Identity Preservation**: Fixed person deserialization on app restart so the active user identity (`isCurrentUser`) is reliably maintained across sessions.
- **Protected User Profile & Lobby State**: Ensured `user_profile_json` and `saved_lobbies_json` are safely preserved when resetting/clearing bills.
- **Race-Free State Snapshots**: Guaranteed consistent, atomic serialization of UI state snapshots to disk without asynchronous coroutine races.
- **Smart Mode Reset**: Fixed `clearAllData()` to navigate to the correct hub depending on whether Single Bill or Trip Ledger mode is active.

### 👥 Lobby & Cloud Sync Stability
- **Atomic Member Addition**: Unique ID assignment and atomic member synchronization to local state and Supabase cloud.
- **Collision-Resistant Lobby Codes**: Upgraded lobby code generation to 6-character collision-checked alphanumeric keys.
- **Lifecycle-Bound Polling**: Automatically stops lobby background polling upon mode changes or lobby deletion to conserve device resources and battery.
- **Resilient Network Handshake**: Safely reads HTTP error streams from Supabase backend without unhandled stream exceptions.

### 🧾 Smarter Bill Scanning & Parsing
- **Word-Boundary Matcher**: Upgraded receipt OCR filter to use whole-word boundary matching, preventing false-positive rejection of food items whose names contain partial keywords.

### 📱 Navigation Refinements
- **Clean App Exit**: Removed recursive back-press loops from Mode Selection screen to permit natural Android back-to-home behavior.
- **Receipt History Integrity**: Prevented duplicate timestamp rewrites when re-navigating to the summary screen for existing bills.

---

**Full Commit History & Source**: [https://github.com/urunkarpm/Splixter](https://github.com/urunkarpm/Splixter)
