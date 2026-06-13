# Partial Seed Recovery Design

## Goal

Make demo data initialization recover safely when the database already contains only some categories or products.

## Category Recovery

Treat the six built-in category names as independent seed records. Reuse any existing category with the same name and create only missing categories. Preserve unrelated and existing categories without changing their fields or IDs.

## Product Recovery

Treat every configured seed product name as an independent seed record. Load existing product names once, create only missing seed products, and preserve all fields of existing products. Continue applying the separate image-only synchronization to configured products after missing products are inserted.

## Behavior Recovery

Build category product groups using the actual IDs returned by category recovery rather than assuming IDs `1` through `6`. Generate demo behaviors only when no behavior records exist, preserving the existing behavior idempotency rule.

## Testing

Use the existing partial-category and partial-product integration tests as regression coverage. Strengthen coverage by verifying an existing product is preserved and by using shifted category IDs so behavior generation proves it uses actual IDs. Run the full backend test suite afterward.
