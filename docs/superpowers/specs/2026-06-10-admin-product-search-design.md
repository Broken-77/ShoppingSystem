# Admin Product Search Design

## Goal

Add fast, local product search to the admin product management table without changing the existing backend API.

## Search Behavior

- Place a search field directly above the product table.
- Match against product name, brand, and the resolved category name.
- Ignore leading and trailing whitespace and letter case.
- Update results immediately as the administrator types.
- Reset pagination to page 1 whenever the search query changes.
- Calculate page count, range text, and total count from filtered results.
- Show a clear empty state when no products match.

## Data Flow

`products` remains the source list loaded by `listAdminProducts()`. A computed `filteredProducts` list applies the normalized query, and the existing pagination helpers paginate that computed list. Product creation, editing, status changes, and backend endpoints remain unchanged.

## UI

The search control uses the existing input and panel styling. It includes a visible label, a concise placeholder, and a result count so administrators can understand the active filter without adding a separate submit button.

## Testing

Extract the matching logic into a small frontend utility and test name, brand, category, case-insensitive matching, whitespace normalization, and empty-query behavior. Run the existing pagination tests and a production build, then verify the page in the in-app browser.

