# 🧾 Splixter — Smart & Fair Bill Splitting App

**Splixter** is a modern, feature-rich Android app built with Kotlin and Jetpack Compose designed to make group bill splitting effortless, accurate, and visually delightful. Whether you're dining out with friends, sharing apartment expenses, or organizing a group trip, Splixter calculates exact individual shares with proportional tax, tip, and discount distribution.

---

## ✨ Features Overview

### 🍕 1. Itemized & Smart Cost Splitting
- **Item-by-Item Assignment**: Easily assign individual food or drink items to specific people.
- **Shared Items**: Split shared starters, appetizers, or drinks among multiple selected participants.
- **Smart Fallback**: Any unassigned items automatically default to an equal split among all group members.
- **Fair Proportional Distribution**: Taxes, tips, and discounts are distributed proportionally based on each person's exact consumption share—ensuring no one pays more than their fair share.
- **Category-Specific Tax Slicing**: Differentiates between Food and Liquor items. GST is levied strictly on food items, and VAT is levied strictly on liquor items. People are only charged GST/VAT based on their specific food/liquor consumption shares. If there are only liquor items and no food on the bill, no GST is charged.

---

### 👥 2. People & Group Management
- **Custom Participant Badges**: Assign fun emojis and distinct color palettes to each person.
- **Payer Selection**: Designate who paid the total bill upfront with one tap.
- **Saved Groups**: Create and save recurring groups (e.g., *"Roommates"*, *"Office Team"*, *"Weekend Gang"*) for instant loading on future bills.

---

### 🔍 3. Item Entry & Smart Suggestions
- **Smart Dish Autocomplete**: Integrated dish dictionary featuring popular Indian food delicacies, street food, continental dishes, and beverage suggestions.
- **Intelligent Category Guessing**: Automatically guesses whether an item is Food or Liquor in real-time as you type or tap suggestions, with an interactive override toggle.
- **Instant Price Entry**: Fast, streamlined manual item addition designed for quick input.

---

### 📊 4. Bill History & Spending Insights
- **Bill History**: Automatically saves completed bills with detailed timestamps and itemized breakdowns. Easily reload past bills anytime.
- **Spending Analytics & Insights**: Integrated modal to visualize spending distributions and breakdown trends across group outings.

---

### 📤 5. High-Res Receipt Exporting & Sharing
- **Thermal Receipt Renderer**: Programmatically generates a clean, styled receipt using custom Android Canvas rendering.
- **Share Image (PNG)**: One-tap PNG receipt generation ready to share directly via WhatsApp, Telegram, Instagram, or Email.
- **Export PDF**: Generate and share structured PDF reports for formal expense tracking and reimbursements.

---

### 🎨 6. Premium UI & Performance Optimization
- **iOS-style Liquid Glass (Glassmorphism)**: Displays soft, colorful glowing background blobs (liquid gradients) behind frosted-glass cards with thin, glossy reflective borders.
- **High-Contrast Segmented Toggles**: Includes glassmorphic segmented toggles with physical sliding capsules and elevated active indicators for clear visibility in both Light and Dark modes.
- **Material 3 Design System**: Beautiful, modern aesthetic with fluid layouts and high-contrast visuals.
- **Light & Dark Mode**: Full support for system and manual dark theme toggling with smooth transitions.
- **Lag-Free Performance**: State changes and storage disk writes are debounced on background IO coroutines to keep typing and interactions 100% smooth.

---

## 🛠️ Built With
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Graphics Engine**: Android Canvas API (Receipt Generation)
- **Local Storage**: Asynchronous JSON serialization with SharedPreferences

---

## 🚀 Getting Started
1. Clone or download the repository.
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Build and run the project using `./gradlew installDebug` or by clicking **Run** in Android Studio.
