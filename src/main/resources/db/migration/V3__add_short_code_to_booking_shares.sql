-- Add short_code for share URLs (cleaner than token in URL)
ALTER TABLE md_booking_shares ADD COLUMN short_code VARCHAR(16) NULL;
CREATE UNIQUE INDEX idx_booking_shares_short_code ON md_booking_shares(short_code) WHERE short_code IS NOT NULL;
