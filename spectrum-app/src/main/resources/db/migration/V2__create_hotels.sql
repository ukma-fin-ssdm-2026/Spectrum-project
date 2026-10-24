CREATE TABLE hotels (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    city VARCHAR,
    district VARCHAR,
    street VARCHAR,
    building_number INTEGER,
    picture_url TEXT,
    price_per_night DECIMAL NOT NULL,
    rating DOUBLE PRECISION NOT NULL
);

CREATE TABLE amenities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE hotel_amenities (
    hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    amenity_id UUID NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (hotel_id, amenity_id)
);

CREATE INDEX hotel_amenities_amenity_id_idx ON hotel_amenities (amenity_id);
