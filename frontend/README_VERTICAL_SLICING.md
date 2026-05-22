Vertical Slicing Structure (frontend)
-----------------------------------

What I changed:

- Removed legacy top-level `src/components` and `src/css` duplicates — the app already uses feature-based slices under `src/features`.
- The canonical layout is now vertical/feature slices under `src/features/<feature>/components`, with local styles next to components (e.g., `features/auth/components/LoginPage.js` and `LoginPage.css`).

Why:

- Vertical slicing groups UI, styles, and feature-specific services together, making features self-contained and easier to maintain.

Notes / next steps:

- If you prefer imports from a single entrypoint per feature, add `index.js` files under each `features/<feature>` to re-export components and hooks (many already exist).
- Run the frontend build and run the app to verify there are no stale imports referencing the deleted folders.
