# Mobile UI theme tokens

The app uses Material 3 `ColorScheme` roles as its shared semantic color tokens. Components should use these roles instead of declaring screen-specific colors.

| Kora MVP CSS token | Compose role | Purpose |
| --- | --- | --- |
| `--background` | `background` / `onBackground` | Page canvas and its text |
| `--card` | `surface` / `onSurface` | Cards, sheets, and primary content |
| `--muted` | `surfaceVariant` / `secondary` | Quiet controls and grouped content |
| `--muted-foreground` | `onSurfaceVariant` | Secondary labels and supporting text |
| `--border`, `--input` | `outline`, `outlineVariant` | Dividers, outlines, and field boundaries |
| tenant `primary_color` | `primary` / `onPrimary` | Primary actions and selected states |
| tenant `accent_color` | `tertiary` / `onTertiary` | Secondary emphasis and charts |
| `--destructive` | `error` / `onError` | Errors and destructive actions |

The web tenant contract comes from `GET /api/v1/system/branding`. The mobile app caches this response with its other workspace metadata, so the last known customer branding remains available offline. It reads `primary_color`, `accent_color`, and `default_mode`; an absent accent is generated with the same hue shift, saturation range, and lightness range as the web palette.

The configured colors remain the tenant brand seeds. The rendered semantic action/accent role is adjusted only as far as needed to reach 4.5:1 contrast against its theme surface. `onPrimary` and `onTertiary` are chosen from black or white using the larger measured contrast ratio. Bright brand colors therefore retain their hue while remaining readable in text, icons, and controls.

Light and dark neutral surfaces follow Kora MVP's neutral theme. Tenant colors are accents and do not tint page backgrounds or every card. Secondary labels use `onSurfaceVariant` instead of translucent `onSurface` values, which keeps contrast predictable in both appearances.

Shared spacing and radii live in `KoraTokens`; layout and components should use Material theme roles, `KoraTokens` dimensions, and Material 3 shapes instead of local hex values.
