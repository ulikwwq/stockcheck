-- Product photo feature: images now live in Supabase Storage (a private
-- bucket, accessed only via backend-generated signed URLs) instead of as
-- an arbitrary client-supplied external URL.
--
-- The existing image_url column previously held whatever URL string a user
-- typed into a free-text field - it was never wired to real uploads. Those
-- values are not valid internal storage paths, so they are cleared here
-- rather than silently reinterpreted. Renaming (not dropping+adding) keeps
-- this a single additive-safe migration with no data-shape change beyond
-- the rename itself.
UPDATE products SET image_url = NULL WHERE image_url IS NOT NULL;

ALTER TABLE products RENAME COLUMN image_url TO image_path;
