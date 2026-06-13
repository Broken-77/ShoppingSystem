# Seed Product Image Sync Design

## Goal

Keep existing product data intact while applying image URL changes from `DataInitializer.IMG` to products already stored in the demo database.

## Behavior

- Match configured images to existing products by exact product name.
- Update only `Product.imageUrl` when the configured URL is non-empty and differs from the stored URL.
- Preserve category, name, description, brand, price, stock, status, sales count, creation time, users, behaviors, carts, and orders.
- Leave products without an `IMG` entry unchanged.
- Continue using the existing seed path to create missing products.

## Implementation

Add a focused `updateImageUrl` method to `Product`. After `seedProducts` has created any missing products and loaded all products, use a targeted repository update that changes only the image column, then synchronize the in-memory product before returning the product map. This avoids triggering `@PreUpdate`, so `updatedAt` remains unchanged.

## Testing

Create an existing product whose name has an `IMG` entry but whose image and business fields differ from the seed defaults. Run the initializer and verify that only its image URL changes while all other business and timestamp fields remain unchanged.
