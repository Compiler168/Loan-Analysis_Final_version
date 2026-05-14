# SmartLoan AI+ Design System

## 1. Vision & Principles
SmartLoan AI+ is a modern fintech application designed to provide users with intelligent financial insights and loan management tools. The design language is built on three core pillars:
- **Trust:** Professional, clean, and secure appearance using deep blues and high-quality typography.
- **Intelligence:** Subtle use of gradients and "sparkle" iconography to denote AI-driven features.
- **Clarity:** High contrast ratios and a strict 8pt grid system for optimal readability and accessibility.

---

## 2. Color Palette
The application uses a "Modern Fintech" palette designed for clarity and professional appeal.

### Core Brand Colors
| Category | Variable | Hex | Usage |
| :--- | :--- | :--- | :--- |
| **Primary** | `primary_600` | `#2563EB` | Main brand color, primary actions, and highlights. |
| **Secondary** | `secondary_600` | `#059669` | Success states, financial growth, and positive indicators. |
| **Accent** | `accent_500` | `#8B5CF6` | Innovation highlights and special AI features. |

### Semantic Colors
| Status | Hex (Main) | Hex (Light) | Usage |
| :--- | :--- | :--- | :--- |
| **Success** | `#10B981` | `#D1FAE5` | Approved loans, profitable trends. |
| **Warning** | `#F59E0B` | `#FEF3C7` | Medium risk, pending actions. |
| **Error** | `#EF4444` | `#FEE2E2` | High risk, rejected applications, debt. |

### Neutral Scale
- **Background:** `#F8FAFC` (Slate 50)
- **Surface/Card:** `#FFFFFF`
- **Text (High Emphasis):** `#0F172A` (Slate 900)
- **Text (Medium Emphasis):** `#475569` (Slate 600)
- **Text (Low/Muted):** `#94A3B8` (Slate 400)
- **Border/Divider:** `#E2E8F0`

---

## 3. Typography
We use a **Major Second (1.125)** scale for balanced hierarchy.

- **Display:** `32sp` (Bold) - Onboarding and major landing headlines.
- **Heading 1:** `24sp` (Bold) - Section headers.
- **Heading 2:** `20sp` (Medium/Bold) - Activity titles.
- **Body Large:** `16sp` - Key information and input text.
- **Body Medium:** `14sp` - Standard descriptive text.
- **Body Small:** `12sp` - Secondary labels and metadata.
- **Caption:** `11sp` - Overlines and fine print.

---

## 4. Spacing System
Built on a **8pt Grid** for consistency across all screens.

- **XS:** `4dp`
- **SM:** `8dp`
- **MD:** `16dp`
- **LG:** `24dp`
- **XL:** `32dp`
- **XXL:** `48dp`

---

## 5. Component Standards

### Cards
- **Radius:** `16dp` (Standard), `24dp` (Dashboard Hero)
- **Elevation:** `2dp` (Subtle) to `8dp` (Floating)
- **Border:** `1dp` solid `#E2E8F0` for low-elevation cards.

### Buttons
- **Height:** `54dp` (Primary), `40dp` (Small)
- **Radius:** `14dp`
- **Style:** Filled for primary actions, Outlined for secondary.

### Input Fields
- **Height:** `56dp`
- **Radius:** `14dp`
- **Background:** `#F8FAFC` (Subtle gray for contrast against white surfaces).

---

## 6. Iconography
- **System Icons:** 24x24dp, Line-style (2px stroke).
- **AI Features:** Multi-colored sparkles and gradient-filled icons to differentiate from standard UI.

---

## 7. Theme Architecture
The app follows **Material 3 (M3)** standards with a custom bridge for legacy Material Components:
- **Light Theme:** Primary focus, optimized for professional use.
- **Dark Theme:** High-contrast slate-based theme for accessibility (mapped in `values-night`).
- **Dynamic Color:** Ready for Android 12+ wallpaper-based tinting.
