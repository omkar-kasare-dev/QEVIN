# QEVIN 💬

### Quiet. Every Voice. In. Network.

**A modern, privacy-focused personal messaging application built to make communication simple, meaningful and secure.**

<p align="center">   <img src="docs/images/qevin-banner.png" alt="QEVIN Banner" width="100%" /> </p>

<p align="center">   <strong>Connect privately. Communicate freely. Stay connected.\</strong> </p>

---

## 📱 About QEVIN

**QEVIN** is a next-generation personal messaging application designed around simplicity, privacy and a clean user experience.

The goal is to create a communication platform where users can connect with people, exchange messages, share moments and communicate without unnecessary complexity.

QEVIN focuses on a minimal interface, thoughtful interactions, modern Android development practices and privacy-conscious design.

Unlike traditional messaging interfaces, QEVIN aims to establish its own visual identity through a distinctive dark interface, midnight navy backgrounds, soft lavender accents and carefully designed UI components.

### ✨ Brand Identity

| Element           | Description                          |
| ----------------- | ------------------------------------ |
| App Name          | QEVIN                                |
| Full Form         | Quiet. Every Voice. In. Network.     |
| Category          | Personal Messaging                   |
| Platform          | Android                              |
| Primary Theme     | Dark / Midnight                      |
| Accent Color      | Lavender / Violet                    |
| Design Philosophy | Minimal, Elegant and Privacy-focused |

---

## 🎨 Design Philosophy

QEVIN follows a custom design language developed specifically for the application.

### Color Palette

| Color           | Hex Code  | Usage                  |
| --------------- | --------- | ---------------------- |
| Midnight Navy   | `#05051F` | Primary background     |
| Deep Navy       | `#0B0B2E` | Secondary background   |
| Soft Lavender   | `#B9A5FF` | Primary accent         |
| Electric Violet | `#713BFF` | Buttons and highlights |
| Soft White      | `#F5F3FF` | Primary text           |
| Muted Lavender  | `#A9A2C7` | Secondary text         |

### Design Principles

- Minimal and distraction-free interface.
- Consistent spacing and typography.
- Smooth transitions and meaningful animations.
- Custom-designed components.
- Dark-first visual experience.
- Clear visual hierarchy.
- Consistent UI across all application screens.

---

## 🚀 Features

QEVIN is being developed with the following feature roadmap.

### 1. Authentication & Account Management

- User registration.
- Secure login.
- Logout functionality.
- User profile management.
- Profile photo support.
- Account settings.

### 2. Personal Messaging

- One-to-one conversations.
- Real-time message delivery.
- Message timestamps.
- Message delivery and read indicators.
- Conversation history.
- Unread message indicators.
- Typing indicators.

### 3. Contacts & Connections

- Contact discovery.
- Search users.
- Start new conversations.
- Manage personal connections.
- Online and last-seen status.

### 4. Media Sharing

- Image sharing.
- Video sharing.
- Document attachments.
- Media preview.
- Shared media history.

### 5. Voice & Video Communication

- Voice calling.
- Video calling.
- Incoming call notifications.
- Call history.
- Call controls.

### 6. Groups & Community

- Create group conversations.
- Add and remove participants.
- Group profile management.
- Group messaging.
- Group notifications.

### 7. Privacy & Security

- Privacy-focused account settings.
- Secure authentication.
- Session management.
- Block and report users.
- Control profile visibility.
- Secure communication architecture.

**Note:** End-to-end encryption and other security mechanisms must be implemented and independently validated before QEVIN can claim end-to-end encrypted communication.

---

## 🛠️ Technology Stack

QEVIN follows a modern Android development approach.

| Technology         | Purpose                       |
| ------------------ | ----------------------------- |
| Kotlin             | Primary programming language  |
| Jetpack Compose    | Modern declarative UI         |
| Material 3         | UI components and foundations |
| Android Studio     | Development environment       |
| Kotlin Coroutines  | Asynchronous operations       |
| StateFlow          | Reactive state management     |
| MVVM               | Presentation architecture     |
| Clean Architecture | Separation of concerns        |
| Gradle Kotlin DSL  | Build configuration           |
| Git & GitHub       | Version control               |

*Backend, database, authentication and real-time messaging technologies will be documented here once their implementation is finalized.*

---

## 🏗️ Architecture

QEVIN is intended to follow MVVM with Clean Architecture principles to maintain a scalable and maintainable codebase.

### Architecture Overview

```text
┌───────────────────────────────┐
│        Presentation           │
│                               │
│  Jetpack Compose Screens      │
│  UI Components                │
│  ViewModels                   │
│  UI State / UI Events         │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│           Domain              │
│                               │
│  Models                       │
│  Use Cases                    │
│  Repository Interfaces        │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│            Data               │
│                               │
│  Repository Implementations   │
│  Data Sources                 │
│  DTOs / Mappers               │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│       External Services       │
│                               │
│  Backend / Database / APIs    │
└───────────────────────────────┘
```

### Architecture Benefits

- Separation of responsibilities.
- Better testability.
- Reusable business logic.
- Easier maintenance.
- Scalable feature development.
- Improved state management.

---

## 📂 Project Structure

The intended project organization follows a feature-oriented approach with clear architectural boundaries.

```text
QEVIN/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── <package-name>/
│           │       │
│           │       ├── core/
│           │       │   ├── navigation/
│           │       │   ├── theme/
│           │       │   ├── components/
│           │       │   └── utils/
│           │       │
│           │       ├── features/
│           │       │   ├── splash/
│           │       │   ├── onboarding/
│           │       │   ├── authentication/
│           │       │   ├── home/
│           │       │   ├── chat/
│           │       │   ├── contacts/
│           │       │   ├── profile/
│           │       │   └── settings/
│           │       │
│           │       ├── data/
│           │       ├── domain/
│           │       └── MainActivity.kt
│           │
│           └── res/
│               ├── drawable/
│               ├── mipmap/
│               └── values/
│
├── docs/
│   └── images/
│       ├── qevin-banner.png
│       ├── app-icon.png
│       └── screenshots/
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

*This represents the proposed organization. Update it to match the actual repository structure as development progresses.*

---

## ⚙️ Getting Started

Follow these instructions to run QEVIN locally.

### Prerequisites

Make sure you have:

- Android Studio.
- Android SDK.
- Kotlin support.
- JDK compatible with the project's Gradle configuration.
- Git installed.
- Android Emulator or a physical Android device.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/QEVIN.git
```

### 2. Open the Project

Open the cloned project in Android Studio.

Wait for Gradle synchronization to complete.

### 3. Configure the Project

- Verify the Android SDK configuration.
- Check the Gradle dependencies.
- Configure any required backend credentials when applicable.
- Ensure the project configuration is complete.

### 4. Build the Application

On Windows:

```bash
gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
./gradlew assembleDebug
```

### 5. Run the Application

Select an Android emulator or connect a physical Android device.

Click **Run** in Android Studio.

---

## 📸 Application Screenshots

The following section will showcase the actual application screens as development progresses.

| Screen        | Preview     |
| ------------- | ----------- |
| Splash Screen | Implemented |
| Onboarding    | Implemented |
| Login         | Implemented |
| Registration  | Implemented |
| Home / Chats  | Implemented/Coming Soon |
| Conversation  | Coming Soon |
| User Profile  | Implemented |
| Settings      | Coming Soon |

Add actual screenshots inside `docs/images/screenshots/` and update this section with their image paths.

---

## 🗺️ Development Roadmap

QEVIN is being developed incrementally, with a focus on building a stable foundation before introducing advanced communication features.

- [x] Initial Android project setup.
- [x] Initial application build and launch.
- [x] QEVIN brand identity.
- [x] Initial splash screen design.
- [x] Initial design system direction.
- [x] Complete reusable UI components.
- [x] Onboarding screens.
- [x] Login and registration.
- [x] Main messaging dashboard.
- [x] User profile and settings.
- [x] Backend integration.
- [x] User authentication.
- [ ] Real-time personal messaging.
- [ ] Media sharing.
- [ ] Push notifications.
- [ ] Voice and video calls.
- [ ] Privacy and security enhancements.
- [ ] Testing and performance optimization.
- [ ] Google Play Store release.

---

## 🔐 Privacy & Security

Privacy is one of the core principles behind QEVIN.

The application is being designed with an emphasis on:

- Responsible handling of user information.
- Secure account authentication.
- Appropriate access control.
- Safe handling of user-generated content.
- Privacy-conscious application design.

Security features will be documented as they are implemented and verified.

---

## 🤝 Contributing

Contributions, suggestions and feedback are welcome.

If you would like to contribute:

1. Fork the repository.
2. Create a feature branch.

```bash
git checkout -b feature/your-feature-name
```

3. Implement your changes.
4. Commit your changes.

```bash
git commit -m "feat: add your feature"
```

5. Push your branch.

```bash
git push origin feature/your-feature-name
```

6. Open a Pull Request.

Please keep code readable, follow the existing architecture and maintain consistency with the QEVIN design system.

---

## 📌 Project Status

**QEVIN is currently under active development.**

The initial Android foundation and visual brand direction have been established. Application screens and core messaging functionality are being developed incrementally.

Features listed in the roadmap should not be considered production-ready until their implementation and testing are complete.

---

## 👨‍💻 Developer

**QEVIN** is an independent Android application project.

Developed with a focus on modern Android technologies, clean architecture, thoughtful UI design and privacy-conscious communication.

---

## 📄 License

The licensing terms for QEVIN have not yet been finalized.

Until a license is added to this repository, all rights are reserved by the project owner. Please contact the owner before redistributing or commercially using the project.

---

<p align="center">   <strong>QEVIN</strong>   <br/>   <em>Quiet. Every Voice. In. Network.</em>   <br/><br/>   A quieter, more thoughtful way to stay connected. </p>
