---
name: GitPulse Expressive M3
colors:
  surface: '#0c1322'
  surface-dim: '#0c1322'
  surface-bright: '#323949'
  surface-container-lowest: '#070e1d'
  surface-container-low: '#141b2b'
  surface-container: '#191f2f'
  surface-container-high: '#232a3a'
  surface-container-highest: '#2e3545'
  on-surface: '#dce2f7'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dce2f7'
  inverse-on-surface: '#293040'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#68dba9'
  on-secondary: '#003825'
  secondary-container: '#25a475'
  on-secondary-container: '#00311f'
  tertiary: '#ffb95f'
  on-tertiary: '#472a00'
  tertiary-container: '#e29100'
  on-tertiary-container: '#523200'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#85f8c4'
  secondary-fixed-dim: '#68dba9'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#0c1322'
  on-background: '#dce2f7'
  surface-variant: '#2e3545'
typography:
  display-lg:
    fontFamily: Geist
    fontSize: 44px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.03em
  display-lg-mobile:
    fontFamily: Geist
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Geist
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Geist
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Geist
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: 0em
  body-md:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0.01em
  body-sm:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.015em
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.02em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.03em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1.25rem
  gutter-mobile: 1rem
  margin: 1.5rem
  margin-mobile: 1.25rem
  space-xs: 0.375rem
  space-sm: 0.625rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.25rem
---

## Brand & Style

The design system establishes an engineering-grade, high-clarity developer environment designed around Google’s Material 3 Expressive guidelines for Android. It prioritizes focused code intelligence, commit dynamics, CI/CD telemetry, and team activity without visual noise.

### Style Archetype & Aesthetics
- **Material 3 Expressive Tonal Architecture**: Strictly utilizes flat tonal surfaces (`surface-container-lowest` through `surface-container-highest`) to signify layering, containment, and focus.
- **Zero Gradient Policy**: All UI fills, buttons, indicators, and scrims are 100% solid, opaque, flat tonal hexes. Gradients (linear, radial, or angular) are strictly forbidden across all components, badges, charts, and navigation elements.
- **Iconographic Discipline**: Emojis are completely banned. All metaphorical, analytical, and status representations are handled via crisp, geometric Material Symbols / Lucide-style SVG icons (`1.5px` to `2px` stroke or solid glyph fills).
- **Spacious & Expressive**: Unconstrained, breathable layouts with expansive gutters, deliberate inner card padding, and dynamic corner radiuses (`rounded-3xl` for high-level surfaces; expressive full pills for floating system bars).

## Colors

The palette operates in an immersive dark system environment built with solid slate and obsidian tones, highlighted by vivid emerald for active git states and warm solid amber for alerts and pull request updates.

### Solid Surface Architecture
- **Surface Container Lowest (Canvas / Root)**: `#0a0f1d` — Base system background behind all scrolling content.
- **Surface Container Low**: `#111827` — Recessed panels, inset code viewports, and search bars.
- **Surface Container**: `#1f293d` — Primary card container, metric boards, and list groupings.
- **Surface Container High**: `#28354d` — Hover states, modal sheets, and active chip toggles.
- **Surface Container Highest**: `#374663` — Floating navigation containers, tooltips, and elevated segmented controls.

### Accent & Semantic Tokens
- **Primary (Accent / Merged / Success)**: `#10b981` (Solid Emerald 500). Contrast on primary: `#042f1a`.
- **Primary Container**: `#064e3b` (Deep Solid Emerald). On primary container: `#6ee7b7`.
- **Secondary (Darker Accent / Branch)**: `#059669` (Solid Emerald 600).
- **Tertiary (Attention / Review / Stale)**: `#f59e0b` (Solid Amber 500).
- **Tertiary Container**: `#78350f`. On tertiary container: `#fde68a`.
- **Error (Failed Builds / Conflicts)**: `#ef4444`. On error: `#450a0a`.
- **Outline / Border**: `#334155` (Crisp low-contrast boundary, 1px solid).
- **On Surface (Headings & Values)**: `#f8fafc`.
- **On Surface Variant (Muted Text & Metadata)**: `#94a3b8`.

## Typography

Typography pairs `Geist` for balanced, rationalized sans-serif interface hierarchy and `JetBrains Mono` for precise developer data, commit hashes, branch targets, status tags, and statistical indicators.

- **Scale Rationale**: Mobile viewports use `display-lg-mobile` capped at `34px` with relaxed `40px` line-heights to eliminate line crunch in commit summaries and repo names.
- **Reading Comfort**: Body text features spacious line-height ratios (`16px/26px` on `body-lg`) to preserve clarity when reading pull request descriptions or issue threads.
- **Technical Readouts**: All numeric stats (loc additions, deletions, run times, commit SHAs) use the mono scale (`label-lg`, `label-md`, `label-sm`) with uppercase tabular figures.

## Layout & Spacing

GitPulse enforces generous spatial discipline to prevent information density fatigue. 

- **Outer Margins**: Mobile screens run a strict `1.25rem` (`20px`) safe margin, scaling to `1.5rem` (`24px`) on tablets and foldable split-panes.
- **Component Padding**: Standard card internal padding is locked to `1.5rem` (`24px`). Dense or nested items use `1rem` (`16px`). Micro badges use `0.375rem` (`6px`) vertical by `0.625rem` (`10px`) horizontal.
- **Vertical Rhythm**: Feed items, repository groups, and commit cards maintain a minimum `1rem` (`16px`) to `1.25rem` (`20px`) stack gap.
- **Bottom Clearance**: All primary scroll areas enforce `6rem` (`96px`) bottom content padding to clear the elevated floating pill navigation bar.

## Elevation & Depth

Visual hierarchy uses pure flat tonal containment and crisp 1px structural outlines. Box-shadows and drop-shadows are excluded to avoid fuzzy edges and keep the UI sharp.

- **Level 0 (Base Canvas)**: Surface `#0a0f1d`. No borders.
- **Level 1 (Structural Cards)**: Surface `#1f293d`, outlined by `1px solid #334155`. Distinguishes list items and cards effortlessly against `#0a0f1d`.
- **Level 2 (Active/Pressed Cards & Inset Controls)**: Surface `#28354d`, outline `1px solid #475569`.
- **Level 3 (Floating Pill Bottom Bar & Modals)**: Solid surface `#374663` with a crisp boundary outline `1px solid #64748b`. The floating pill navigation sits detached from the bottom screen edge with no blur filters or shadow diffusions.

## Shapes

The design follows Material 3 Expressive curvature geometry, utilizing prominent soft corners paired with precise full pill enclosures.

- **Repository & Analytics Cards**: `rounded-3xl` (`1.5rem` to `1.75rem` / `24px` to `28px`) for all hero cards, stats boxes, and commit wrappers.
- **Floating Navigation Bar**: Full pill (`rounded-full` / `9999px`) floating horizontally centered above the system home indicator.
- **Buttons, Filters & Chips**: Pill radius (`9999px`) for action chips, filter selectors, and primary trigger buttons.
- **Code Snippets & Diff Blocks**: `rounded-2xl` (`1rem` / `16px`) with inset paddings.

## Components

### Buttons
- **Filled Primary**: Solid background `#10b981`, text `#042f1a`, font `Geist SemiBold 14px`, corner `rounded-full`, height `48px`, horizontal padding `24px`. No shadow, no gradient.
- **Tonal (Secondary)**: Solid background `#28354d`, text `#f8fafc`, border `1px solid #334155`, corner `rounded-full`, height `48px`.
- **Icon Buttons**: Size `44x44px`, solid background `#1f293d`, corner `rounded-full`, containing a 20px solid-colored SVG glyph.

### Cards & Repositories
- **Surface**: Solid `#1f293d`, corner `rounded-3xl`, border `1px solid #334155`, padding `24px`.
- **Header**: Includes title in `headline-sm`, status icon (e.g. Git Branch or Check SVG), and metadata badge.
- **Gaps**: Sub-elements (branch name, commit message, pipeline stats) spaced using `space-md` (`16px`).

### Filter Chips
- **Unselected**: Solid background `#111827`, border `1px solid #334155`, text `#94a3b8`, padding `8px 16px`, corner `rounded-full`.
- **Selected**: Solid background `#10b981`, border `1px solid #10b981`, text `#042f1a`, corner `rounded-full`, font `JetBrains Mono Medium`.

### Floating Bottom Navigation Bar
- **Form Factor**: Fully detached pill suspended `16px` above the bottom edge, centered with `20px` margins.
- **Surface**: Solid `#28354d`, border `1px solid #475569`, corner `rounded-full`, height `64px`, horizontal padding `12px`.
- **Active Tab**: Active indicator is a solid pill container `#10b981` with `#042f1a` icon. Inactive tabs display `#94a3b8` SVG icons over the flat background.

### Input Fields & Search
- **Surface**: Solid `#111827`, border `1px solid #334155`, corner `rounded-full`, height `52px`, padding `0 20px`.
- **Active State**: Border shifts to solid `#10b981` (1.5px width). No glow or outer halo.
- **Prefix**: Clean search SVG icon in `#94a3b8`.

### Lists & Activity Rows
- **Container**: Solid `#1f293d`, internal divider `1px solid #28354d`, outer corner `rounded-3xl`.
- **Item Rhythm**: Height `64px`, vertical padding `16px`, horizontal padding `20px`.
- **Commit Indicator**: Monospace SHA pills in solid `#111827` with `#10b981` or `#94a3b8` text.