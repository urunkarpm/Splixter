# 🧪 Splixter Playwright + JS Rigorous Test Suite

Comprehensive automated end-to-end and component integration test suite in **Playwright + JavaScript** with deep positive and negative scenario testing across all components of the Splixter Android application.

---

## 📁 Test Architecture

```
tests/playwright/
├── package.json                   # Dependencies & npm run scripts
├── playwright.config.js           # Playwright configuration & reports
├── run_tests.js                   # CLI test runner with ADB device checks
├── src/
│   ├── driver/
│   │   ├── AndroidAppDriver.js    # Playwright Android ADB wrapper & UIAutomator inspector
│   │   └── Selectors.js           # Centralized Compose semantic selectors & strings
│   ├── helpers/
│   │   ├── TestData.js            # Input fixtures, XSS/SQL injections, bad numbers, malformed OCR
│   │   ├── AssertionHelper.js     # Exact float reconciliation, min-cashflow debt assertions
│   │   └── DeviceActions.js       # App reset, keyboard, orientation, process death helpers
│   └── specs/
│       ├── 01_onboarding_profile.spec.js   # User Profile Setup (Positive & Negative)
│       ├── 02_mode_selection.spec.js        # Mode Routing, Theme & Preferences
│       ├── 03_people_setup.spec.js          # Member Management, Duplicates, Groups
│       ├── 04_scan_and_bill_input.spec.js   # Manual Items, OCR Parsing, Discount/Tax limits
│       ├── 05_item_assignment.spec.js       # Item Splits, Unassigned Warning, Rounding
│       ├── 06_receipt_summary.spec.js       # Itemized Totals, Payer Assignment, Bill Reset
│       ├── 07_lobby_hub.spec.js             # Real-time Lobbies, Code Validation, Claims
│       ├── 08_trip_expenses.spec.js         # Multi-Category Expenses, Zero Amount Blocks
│       ├── 09_trip_summary_settle.spec.js   # Debt Optimization Math & Settlement Security
│       └── 10_edge_cases_resilience.spec.js # Backgrounding, Rotation, Backstack Traversal
```

---

## 🚀 Quick Start & Execution

### Prerequisites
1. **Node.js** (v18+) & **npm** installed.
2. **Android SDK / ADB** configured on `PATH`.
3. An Android device connected with **USB Debugging enabled** or an Android Emulator running.

### 1. Install Dependencies
```bash
cd tests/playwright
npm install
```

### 2. Run All Tests
```bash
npm test
```

### 3. Run Individual Component Test Suites
```bash
# User Profile Setup (Negative validation, UPI, XSS)
npm run test:onboarding

# Mode Selection & Dark Mode persistence
npm run test:mode

# People Setup (Duplicate names, self-protection, saved groups)
npm run test:people

# Scan Bill (OCR parsing, 0/negative prices, discount bounds)
npm run test:scan

# Item Assignment (Splits, unassigned validations)
npm run test:assign

# Receipt Summary (Payer selection, breakdown reconciliation)
npm run test:receipt

# Lobby Hub (Invalid codes, QR codes, profile claims)
npm run test:lobby

# Trip Expenses (Multi-category, 0-amount blocks, custom splits)
npm run test:expenses

# Trip Summary & Debt Settlements (Greedy algorithm, security)
npm run test:settle

# System Resilience (Orientation flip, backgrounding, backstack)
npm run test:resilience
```

---

## 🎯 Negative Testing Scenarios Covered

| Component | Negative Scenarios Tested |
| :--- | :--- |
| **Profile Setup** | Empty name submission, whitespace-only, 300+ chars overflow, malformed UPI IDs without `@`, non-numeric phone inputs, SQL & HTML injection inputs. |
| **People Setup** | Duplicate person name (case-insensitive), empty person name, self-profile deletion protection, 20+ member list stress test. |
| **Scan & Bill Input** | $0.00 price item, negative prices, NaN price inputs, malformed OCR text without prices, discount exceeding bill subtotal. |
| **Item Assignment** | Unassigned items warning, fraction rounding (3-way cent distribution), rapid tap race condition safety. |
| **Receipt Summary** | Zero-cost item divisions, missing payer selection, reset bill confirmation cancel/confirm. |
| **Lobby Hub** | Invalid 6-character code formats, non-existent lobby joining, offline network fallback. |
| **Trip Expenses** | $0.00 expense logging, negative amounts, non-payer delete permissions. |
| **Trip Summary** | Cyclic debt settlement resolution (A->B->C->A), unauthorized payment confirmation checks. |
| **Resilience** | Screen rotation portrait/landscape/portrait, backgrounding/resume state retention, backstack hierarchy navigation. |

---

## 📊 Reports & Artifacts
Playwright automatically outputs HTML and JSON test reports to:
- `test-results/html-report/index.html`
- `test-results/test-summary.json`
- `test-results/screenshots/` (on failure)
