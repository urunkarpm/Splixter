---
name: Luminous Ledger
colors:
  surface: '#fcf8ff'
  surface-dim: '#ddd8e0'
  surface-bright: '#fcf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f2f9'
  surface-container: '#f1ecf3'
  surface-container-high: '#ebe7ee'
  surface-container-highest: '#e5e1e8'
  on-surface: '#1c1b20'
  on-surface-variant: '#464555'
  inverse-surface: '#313035'
  inverse-on-surface: '#f4eff6'
  outline: '#777587'
  outline-variant: '#c7c4d8'
  surface-tint: '#4c42e9'
  primary: '#493ee5'
  on-primary: '#ffffff'
  primary-container: '#635bff'
  on-primary-container: '#fefaff'
  inverse-primary: '#c3c0ff'
  secondary: '#5d5d6b'
  on-secondary: '#ffffff'
  secondary-container: '#e2e1f1'
  on-secondary-container: '#636371'
  tertiary: '#00637b'
  on-tertiary: '#ffffff'
  tertiary-container: '#007e9b'
  on-tertiary-container: '#f6fcff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c3c0ff'
  on-primary-fixed: '#0f0069'
  on-primary-fixed-variant: '#321ed2'
  secondary-fixed: '#e2e1f1'
  secondary-fixed-dim: '#c6c5d4'
  on-secondary-fixed: '#191b26'
  on-secondary-fixed-variant: '#454652'
  tertiary-fixed: '#b7eaff'
  tertiary-fixed-dim: '#4cd6ff'
  on-tertiary-fixed: '#001f28'
  on-tertiary-fixed-variant: '#004e60'
  background: '#fcf8ff'
  on-background: '#1c1b20'
  surface-variant: '#e5e1e8'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  container-padding: 20px
  stack-gap: 16px
  card-inner-padding: 24px
  gutter: 12px
---

## Brand & Style

The design system is centered on **Modern Minimalism with Tactile Warmth**. It aims to reduce the social friction of splitting bills by providing an interface that feels helpful, transparent, and approachable. 

The aesthetic leverages high-quality whitespace and a "Soft Corporate" vibe—professional enough to handle financial transactions, yet playful enough for a dinner out with friends. It avoids the clinical coldness of traditional banking apps, opting instead for a friendly, supportive environment. Visual hierarchy is established through clear typographic scaling and subtle depth, moving away from the flat surfaces of the reference into a more layered, dimensional experience.

## Colors

The palette is anchored by a vibrant **Electric Indigo** primary, which provides a modern and energetic focal point for actions. This is supported by a range of **Soft Lavender** and **Cool Grey** neutrals that maintain a clean, airy feel.

To handle the "playful item assignment" mentioned in the request, the system utilizes a secondary "Avatar Palette" of desaturated pastels (Mint, Peach, Sky, and Lemon). These are used exclusively for user tags and status indicators to ensure clarity without overwhelming the interface. The background is a crisp, slightly off-white to reduce eye strain, while text uses a deep charcoal rather than pure black for a softer, more premium reading experience.

## Typography

The design system utilizes **Plus Jakarta Sans** for its entire typographic scale. This font was chosen for its soft, rounded terminals and modern geometric construction, which perfectly aligns with the friendly yet functional brand narrative.

Headlines use a bold weight with slightly tighter letter-spacing to create a strong visual anchor. Body text remains spacious and legible, while labels and tags utilize a semi-bold weight at smaller sizes to maintain readability during complex tasks like item assignment. Numerical data (prices and percentages) should always use the `600` weight or higher to ensure they stand out as the primary information of the app.

## Layout & Spacing

The layout follows a **Fluid Content Model** within a 12-column grid for desktop and a single-column stack for mobile. A strict 8px spacing system ensures consistency across all components.

Vertical rhythm is prioritized, with generous margins between card sections (24px - 32px) to prevent the UI from feeling cluttered during heavy data entry. Elements within cards, such as user tags, use a 12px gutter to allow for touch-friendly interaction. The mobile experience features a fixed bottom action bar for the "Final Bill" calculation, ensuring the primary goal is always within thumb-reach.

## Elevation & Depth

Visual hierarchy is established through **Tonal Layering** and **Ambient Shadows**. Instead of the heavy borders seen in the reference, this design system uses soft, diffused shadows (`0px 4px 20px rgba(0,0,0,0.05)`) to lift cards off the background.

- **Level 0 (Background):** Soft off-white (#FAFAFC).
- **Level 1 (Cards):** Pure white with a subtle 1px border in a light lavender-grey.
- **Level 2 (Active Elements):** Enhanced shadows to indicate interactivity or current focus.
- **Overlays:** Modals and bottom sheets use a subtle backdrop blur (12px) to maintain context while focusing the user on the task at hand.

## Shapes

The shape language is **Rounded and Friendly**. A base radius of 16px (`rounded-lg`) is applied to primary cards to create a modern, approachable container. Interactive elements like buttons and input fields utilize a 12px radius, striking a balance between soft aesthetics and structural clarity. Small UI elements like "Item Tags" use a pill-shaped (100px) radius to distinguish them as draggable or clickable objects.

## Components

### Buttons & Inputs
Primary buttons use a vibrant gradient from the Primary color to a slightly more teal-tinted secondary to add depth. Input fields are redesigned from the reference to have floating labels and a clearer "active" state using a 2px primary border.

### Item Assignment Tags
Tags are the core functional element. They feature a soft pastel background with a high-contrast icon (emoji or avatar). On "Item Cards," these tags are displayed in a flexible wrap-grid. Active tags have a subtle "glow" or thicker border to show they are currently contributing to the split.

### Cards
Cards are the primary organizational unit. Each card represents a bill item and features a clear title, price, and a toggle for "Taxable" or "Service Charge" inclusion. The internal spacing is increased compared to the reference to allow for larger touch targets.

### Progress Indicators
The step-wizard at the top is simplified into a clean, horizontal track with soft-lavender inactive states and high-contrast indigo active states, using typography rather than just icons to indicate the current stage (e.g., "1. Group", "2. Items").