-- Idempotent seed for pharmacy_admin_db.
-- Mirrors categories and medicines from pharmacy_catalog_db into the admin DB schema.
-- Safe for repeated runs: INSERT IGNORE skips existing rows.

USE pharmacy_admin_db;

START TRANSACTION;

-- ── Categories ─────────────────────────────────────────────────────────────
INSERT IGNORE INTO categories (id, name, description, is_active, created_at, updated_at)
SELECT id, name, description, is_active, created_at, NOW()
FROM pharmacy_catalog_db.categories;

-- ── Medicines ─────────────────────────────────────────────────────────────
-- Maps catalog fields to admin schema (admin has mrp, generic_name, strength, dosage_form, sku)
INSERT IGNORE INTO medicines (
    id, name, manufacturer, description, image_url,
    price, mrp, stock, requires_prescription, is_active,
    expiry_date, category_id, created_at, updated_at
)
SELECT
    m.id,
    m.name,
    m.manufacturer,
    m.description,
    m.image_url,
    m.price,
    COALESCE(m.discounted_price, m.price),
    COALESCE(m.stock, 0),
    COALESCE(m.requires_prescription, b'0'),
    m.is_active,
    m.expiry_date,
    m.category_id,
    m.created_at,
    NOW()
FROM pharmacy_catalog_db.medicines m;

COMMIT;
