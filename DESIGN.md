---
version: alpha
name: PharmaCare Clinical Commerce
description: "A light, trustworthy pharmacy storefront and admin system built around calm clinical surfaces, green health actions, compact data, and clear status communication."
colors:
  primary: "#15803D"
  on-primary: "#FFFFFF"
  primary-accent: "#16A34A"
  primary-hover: "#166534"
  primary-active: "#14532D"
  primary-soft: "#F0FDF4"
  primary-container: "#DCFCE7"
  primary-container-strong: "#BBF7D0"
  on-primary-container: "#166534"
  focus-ring: "#22C55E"
  secondary: "#2563EB"
  on-secondary: "#FFFFFF"
  secondary-soft: "#EFF6FF"
  secondary-container: "#DBEAFE"
  on-secondary-container: "#1E40AF"
  tertiary: "#D97706"
  on-tertiary: "#FFFFFF"
  tertiary-soft: "#FFFBEB"
  tertiary-container: "#FEF3C7"
  on-tertiary-container: "#92400E"
  success: "#16A34A"
  success-soft: "#F0FDF4"
  success-container: "#DCFCE7"
  on-success-container: "#166534"
  warning: "#D97706"
  warning-soft: "#FFFBEB"
  warning-container: "#FEF3C7"
  on-warning-container: "#92400E"
  alert: "#CA8A04"
  alert-soft: "#FEFCE8"
  alert-container: "#FEF9C3"
  on-alert-container: "#854D0E"
  destructive: "#DC2626"
  on-destructive: "#FFFFFF"
  destructive-soft: "#FEF2F2"
  destructive-container: "#FEE2E2"
  on-destructive-container: "#991B1B"
  info: "#2563EB"
  info-soft: "#EFF6FF"
  info-container: "#DBEAFE"
  on-info-container: "#1E40AF"
  purple-status: "#7E22CE"
  purple-status-container: "#F3E8FF"
  indigo-status: "#4338CA"
  indigo-status-container: "#E0E7FF"
  teal-status: "#115E59"
  teal-status-container: "#CCFBF1"
  rose-status: "#9F1239"
  rose-status-container: "#FFE4E6"
  orange-status: "#C2410C"
  orange-status-soft: "#FFF7ED"
  orange-status-container: "#FFEDD5"
  background: "#F8FAFC"
  surface: "#FFFFFF"
  surface-subtle: "#F8FAFC"
  surface-muted: "#F1F5F9"
  surface-container: "#FFFFFF"
  surface-container-hover: "#F8FAFC"
  surface-disabled: "#E2E8F0"
  border: "#E2E8F0"
  border-strong: "#CBD5E1"
  input-border: "#E2E8F0"
  text: "#1E293B"
  text-secondary: "#334155"
  text-muted: "#64748B"
  text-subtle: "#94A3B8"
  text-disabled: "#94A3B8"
  text-inverse: "#FFFFFF"
  link: "#2563EB"
  link-hover: "#1D4ED8"
  black: "#000000"
  white: "#FFFFFF"
typography:
  display:
    fontFamily: "Geist Variable"
    fontSize: 36px
    fontWeight: 700
    lineHeight: 44px
    letterSpacing: "-0.02em"
  headline-lg:
    fontFamily: "Geist Variable"
    fontSize: 30px
    fontWeight: 700
    lineHeight: 36px
    letterSpacing: "-0.01em"
  headline-md:
    fontFamily: "Geist Variable"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 32px
    letterSpacing: "-0.01em"
  headline-sm:
    fontFamily: "Geist Variable"
    fontSize: 20px
    fontWeight: 700
    lineHeight: 28px
    letterSpacing: "-0.005em"
  title-lg:
    fontFamily: "Geist Variable"
    fontSize: 18px
    fontWeight: 700
    lineHeight: 28px
  title-md:
    fontFamily: "Geist Variable"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 24px
  body-lg:
    fontFamily: "Geist Variable"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 26px
  body-md:
    fontFamily: "Geist Variable"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 22px
  body-sm:
    fontFamily: "Geist Variable"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 18px
  label-md:
    fontFamily: "Geist Variable"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 20px
  label-sm:
    fontFamily: "Geist Variable"
    fontSize: 12px
    fontWeight: 600
    lineHeight: 16px
  label-xs:
    fontFamily: "Geist Variable"
    fontSize: 10px
    fontWeight: 700
    lineHeight: 12px
  label-caps:
    fontFamily: "Geist Variable"
    fontSize: 12px
    fontWeight: 600
    lineHeight: 16px
    letterSpacing: "0.08em"
spacing:
  base: 4px
  xxs: 2px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  xxl: 32px
  xxxl: 40px
  jumbo: 64px
  page-padding-mobile: 16px
  page-padding-desktop: 24px
  card-padding: 16px
  panel-padding: 20px
  modal-padding: 24px
  grid-gap-sm: 12px
  grid-gap-md: 16px
  grid-gap-lg: 24px
  nav-height: 56px
  mobile-nav-height: 48px
  sidebar-width: 224px
  max-width-narrow: 448px
  max-width-content: 768px
  max-width-detail: 896px
  max-width-wide: 1280px
rounded:
  none: 0px
  sm: 6px
  DEFAULT: 8px
  md: 8px
  lg: 10px
  xl: 14px
  "2xl": 18px
  full: 9999px
radii:
  none: 0px
  control: 8px
  card: 14px
  modal: 18px
  pill: 9999px
shadows:
  none: "none"
  sm: "0 1px 2px 0 #0000000D"
  md: "0 4px 6px -1px #0000001A, 0 2px 4px -2px #0000001A"
  lg: "0 10px 15px -3px #0000001A, 0 4px 6px -4px #0000001A"
  xl: "0 20px 25px -5px #0000001A, 0 8px 10px -6px #0000001A"
elevation:
  flat:
    surface: "{colors.surface}"
    shadow: "{shadows.none}"
  nav:
    surface: "{colors.surface}"
    shadow: "{shadows.sm}"
  raised-card:
    surface: "{colors.surface}"
    shadow: "{shadows.md}"
  floating-card:
    surface: "{colors.surface}"
    shadow: "{shadows.lg}"
  modal:
    surface: "{colors.surface}"
    shadow: "{shadows.xl}"
motion:
  duration-instant: "75ms"
  duration-fast: "150ms"
  duration-standard: "200ms"
  duration-slow: "300ms"
  easing-standard: "cubic-bezier(0.4, 0, 0.2, 1)"
  easing-out: "cubic-bezier(0, 0, 0.2, 1)"
  lift-subtle: "-2px"
  press-depth: "1px"
components:
  app-shell:
    backgroundColor: "{colors.background}"
    textColor: "{colors.text}"
  navbar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text-secondary}"
    height: "{spacing.nav-height}"
  sidebar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text-secondary}"
    width: "{spacing.sidebar-width}"
  brand-mark:
    textColor: "{colors.primary-hover}"
    typography: "{typography.title-lg}"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.lg}"
    height: 40px
    padding: "0 16px"
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.on-primary}"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text-secondary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.lg}"
    height: 40px
    padding: "0 16px"
  button-secondary-hover:
    backgroundColor: "{colors.surface-container-hover}"
    textColor: "{colors.text}"
  button-destructive:
    backgroundColor: "{colors.destructive}"
    textColor: "{colors.on-destructive}"
    typography: "{typography.label-md}"
    rounded: "{rounded.lg}"
    height: 40px
    padding: "0 16px"
  button-soft-destructive:
    backgroundColor: "{colors.destructive-soft}"
    textColor: "{colors.on-destructive-container}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.lg}"
    height: 32px
    padding: "0 10px"
  input-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    typography: "{typography.body-md}"
    rounded: "{rounded.lg}"
    height: 40px
    padding: "8px 12px"
  input-field-focus:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
  label:
    textColor: "{colors.text-secondary}"
    typography: "{typography.label-sm}"
  card-standard:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.xl}"
    padding: "{spacing.card-padding}"
  card-panel:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.xl}"
    padding: "{spacing.panel-padding}"
  product-card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.xl}"
    padding: "{spacing.card-padding}"
  modal-surface:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.2xl}"
    padding: "{spacing.modal-padding}"
  table-header:
    backgroundColor: "{colors.surface-muted}"
    textColor: "{colors.text-secondary}"
    typography: "{typography.label-sm}"
  table-row-hover:
    backgroundColor: "{colors.surface-container-hover}"
    textColor: "{colors.text}"
  nav-link-active:
    textColor: "{colors.primary-hover}"
    typography: "{typography.label-md}"
  sidebar-item-active:
    backgroundColor: "{colors.primary-soft}"
    textColor: "{colors.primary-hover}"
    typography: "{typography.label-md}"
    rounded: "{rounded.lg}"
    padding: "8px 12px"
  badge-default:
    backgroundColor: "{colors.surface-muted}"
    textColor: "{colors.text-secondary}"
    typography: "{typography.label-xs}"
    rounded: "{rounded.full}"
    padding: "2px 8px"
  badge-success:
    backgroundColor: "{colors.success-container}"
    textColor: "{colors.on-success-container}"
    typography: "{typography.label-xs}"
    rounded: "{rounded.full}"
    padding: "2px 8px"
  badge-warning:
    backgroundColor: "{colors.warning-container}"
    textColor: "{colors.on-warning-container}"
    typography: "{typography.label-xs}"
    rounded: "{rounded.full}"
    padding: "2px 8px"
  badge-danger:
    backgroundColor: "{colors.destructive-container}"
    textColor: "{colors.on-destructive-container}"
    typography: "{typography.label-xs}"
    rounded: "{rounded.full}"
    padding: "2px 8px"
  badge-info:
    backgroundColor: "{colors.info-container}"
    textColor: "{colors.on-info-container}"
    typography: "{typography.label-xs}"
    rounded: "{rounded.full}"
    padding: "2px 8px"
  prescription-banner:
    backgroundColor: "{colors.tertiary-soft}"
    textColor: "{colors.on-tertiary-container}"
    rounded: "{rounded.lg}"
    padding: "12px"
  pagination-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.lg}"
    height: 36px
    width: 36px
  skeleton-block:
    backgroundColor: "{colors.surface-disabled}"
    rounded: "{rounded.xl}"
  customer-hero:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.2xl}"
    padding: "{spacing.modal-padding}"
---

## Overview

PharmaCare uses a clinical commerce visual identity: calm, efficient, trustworthy, and task-focused. The product needs to support two modes at once: a friendly customer storefront for browsing and checkout, and a compact operational admin panel for prescription review, inventory, orders, and reporting.

The interface should feel more like a reliable healthcare service than a lifestyle marketplace. Visual interest comes from crisp spacing, green action accents, soft status chips, and lightweight card elevation rather than illustration-heavy decoration.

## Colors

The palette is anchored by **health green** as the primary interaction color. Green is used for primary actions, active navigation states, successful statuses, cart affordances, price emphasis, and approved prescription states.

Neutral slate creates most of the interface. Pages sit on a pale slate background, while content is grouped in pure white cards with subtle borders. This produces a clean clinical tone and keeps dense admin tables readable.

Blue is reserved for links, informational actions, and operational secondary actions such as stock updates. Amber and yellow communicate prescription review, pending payment, expiry warnings, and other attention states. Red is strictly destructive or failed. Purple, indigo, teal, rose, and orange appear as low-emphasis operational status chips.

Do not overuse saturated backgrounds. Most colored treatments should be soft containers with darker text, especially for badges, alerts, and status rows. Reserve solid green for the main action on a screen.

## Typography

The type system is compact and utilitarian. Use **Geist Variable** as the product typeface, with system sans-serif fallbacks when unavailable. Text should be highly legible at small sizes because the UI contains many forms, tables, filters, badges, and order details.

Headings are bold and direct. Page titles generally use a 24px bold style. Section titles are usually 16px to 18px with semibold or bold weight. Body text is usually 14px, with 12px captions for metadata, helper copy, labels, timestamps, and empty states.

Uppercase tracking is appropriate for dashboard statistic labels and small category headings. Avoid decorative typography, script fonts, or overly large marketing type; the product voice should remain practical and professional.

## Layout

The layout follows a responsive centered-container model for customer flows and a full-height sidebar workspace model for admin flows. Customer pages use narrow to medium max widths for cart, checkout, orders, and details, while the catalog can expand to a wide grid.

Spacing follows a 4px base scale with an 8px rhythm. Common page padding is 16px on mobile and 24px on larger screens. Cards generally use 16px or 20px of internal padding; modals and hero panels use 24px. Grid gaps usually sit between 12px and 24px depending on density.

Group related controls tightly, but give major sections enough vertical separation to make tasks scan quickly. In admin screens, prioritize density and alignment. In storefront screens, allow slightly more breathing room around product cards and checkout choices.

## Elevation & Depth

Depth is intentionally restrained. Most hierarchy comes from white surfaces, pale page backgrounds, borders, and tonal hover states. Shadows are light and functional: navigation uses a small shadow, cards may lift subtly on hover, and modals use the strongest shadow in the system.

Hover elevation should be subtle: a small upward movement paired with a medium or large soft shadow. Avoid dramatic drop shadows, glass effects, heavy gradients, or dark panels. The clinical identity depends on cleanliness and confidence.

Modal overlays use black at partial opacity over the light app shell. The modal surface should remain white, rounded, and clearly elevated with a large soft shadow.

## Shapes

The shape language is soft but not playful. Controls use medium rounded corners, cards use larger rounded corners, and badges use full pills. This keeps the pharmacy UI approachable while preserving an operational feel.

Use rounded rectangles consistently. Product cards, cart rows, admin panels, and status banners should all share the same family of rounded corners. Reserve fully circular shapes for counters, icon buttons, quantity controls, and badges.

## Components

Primary buttons are solid green with white text and a darker green hover state. They should be used for actions like add to cart, continue, save, approve, place order, and log in. Secondary buttons are white or very pale slate with borders and slate text.

Inputs are white fields with subtle borders, compact padding, and green focus rings. Labels sit above inputs in small semibold slate text. Error text is red and should be concise.

Cards are white, bordered, rounded containers. Product cards can lift slightly on hover. Data panels and stat cards should stay flatter to preserve dashboard density. Tables use pale slate headers, thin dividers, and a pale hover row state.

Status chips use soft colored backgrounds and dark text. Keep chips small, rounded, and semibold. Prescription-required markers use amber. Success states use green. Payment and delivery lifecycle states can use blue, purple, indigo, teal, orange, or slate as needed, but all should remain low-saturation containers.

Skeleton loading states use slate-200 blocks with the same radius as the final component. This keeps layout shift minimal and matches the system's quiet visual language.

## Do's and Don'ts

- Do use green as the primary health and action signal.
- Do keep most surfaces white on pale slate.
- Do use borders before shadows for everyday hierarchy.
- Do make admin screens compact, aligned, and scannable.
- Do use soft status containers instead of saturated status blocks.
- Do keep form fields and controls at practical heights around 32px to 40px.
- Don't use dark mode styling unless the full screen is designed for it.
- Don't introduce glassmorphism, neon colors, or decorative gradients outside the green hero treatment.
- Don't use red for anything except destructive, rejected, failed, or blocked states.
- Don't mix pill badges with sharp-cornered cards; keep the rounded shape language consistent.
