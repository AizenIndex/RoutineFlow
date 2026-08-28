<h1 align="center" style="font-size:32px; line-height:1"><b>RoutineFlow</b></h1>
<p align="center"><i>A modernized, adaptive habit tracker and daily routine planner with intelligent streaks, advanced consistency analytics, and celebration micro-interactions.</i></p>

<div align="center">
  <img alt="RoutineFlow logo" src="images/app_logo.svg" height="150px">
</div>

<br />

<div align="center">

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20Modular-orange.svg)](docs/ModuleStructure.md)

</div>

<br />

## 🌟 What is Routine Tracker Modern?

**Routine Tracker Modern** is an offline-first, privacy-respecting daily planner and habit tracker. It seamlessly combines a calendar agenda with an intelligent habit scheduling engine that adapts to your actual pace.

Unlike typical habit trackers that penalize you harshly for missed days, Routine Tracker includes non-due days in streaks, lets you balance backlogs flexibly, and gives you deep visibility into your long-term consistency.

---

## ✨ Key Features & Modern Additions

### 🎯 1. Habit Insights & Consistency Analytics *(New)*
* **Rolling Consistency Score**: Real-time calculated consistency rate percentage and dynamic progress indicator for every routine.
* **Streak Milestone Badges**: Unlock milestones (7-Day Champion, 30-Day Master, 100-Day Legend) as your streaks grow.
* **Comprehensive Stats**: Track longest streak, current streak, and total completed sessions.

### 🎆 2. Habit Completion Celebrations *(New)*
* **Micro-Interaction Burst Effects**: Fluid, particle celebration animations when checking off tasks in your agenda.
* **Satisfying Feedback**: Visual delight designed to build positive dopamine reinforcement loops for daily habits.

### 🔄 3. Adaptive & Flexible Schedules
* **Auto-adjusting backlog**: Miss a routine? The app intelligently suggests clearing backlogs on the next non-due day. Over-complete? The next planned session automatically balances out.
* **Flexible frequencies**: Daily, weekly (e.g. 3x/week on any day or specific days), monthly, and alternate-day cycles.

### 📅 4. Agenda & Calendar Integration
* **Visual Agenda**: Clean, distraction-free agenda for any date.
* **Long-term Planning**: Routine calendar displaying completions, streaks, and upcoming schedules to easily plan ahead.

### 🎨 5. Modern UI & Privacy First
* **Material You & Dark Mode**: Dynamic theme colors on Android 12+ and high-contrast dark theme.
* **100% Offline & Private**: Zero tracking, zero telemetry, zero external accounts. Your data never leaves your device.
* **Completely Free**: No ads, no paywalls, and open-source under GPL v3.

---

## 🏗️ Architecture & Tech Stack

This project is built using modern Android best practices following CLEAN Architecture and modularization principles (inspired by Google's *Now In Android*):

* **UI Layer**: 100% Jetpack Compose with Material 3, Single Activity, zero legacy Fragments.
* **State Management**: StateFlow, ViewModel, and unidirectional data flow (UDF).
* **Dependency Injection**: [Koin](https://insert-koin.io/) (configured for seamless future Kotlin Multiplatform expansion).
* **Database**: [SQLDelight](https://cashapp.github.io/sqldelight/) for local SQLite storage.
* **Date & Time**: `kotlinx-datetime` and `kizitonwose/Calendar`.
* **Modularization**: Multi-module Gradle build with custom convention plugins in `build-logic`.

---

## 📜 Original Creator & Attribution

> [!IMPORTANT]  
> This project is a modernized fork and continuation based on the excellent work of **[Daniel Rendox](https://github.com/DanielRendox)** on [RoutineTracker](https://github.com/DanielRendox/RoutineTracker).  
> Original repository: [https://github.com/DanielRendox/RoutineTracker](https://github.com/DanielRendox/RoutineTracker)  
> All modifications, improvements, and new features remain free and open-source under the **GNU General Public License v3 (GPL v3)**.

---

## 🚀 Building from Source

1. Clone this repository:
   ```bash
   git clone <YOUR_REPOSITORY_URL>
   ```
2. Open the project in [Android Studio](https://developer.android.com/studio) (Koala Feature Drop or Ladybug recommended).
3. Build and run the `app` module on an emulator or connected physical Android device (API 21+).

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.
