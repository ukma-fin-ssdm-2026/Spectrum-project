CREATE TYPE ROOM_TYPE AS ENUM ('SINGLE', 'DOUBLE', 'SUITE');
CREATE TYPE ROOM_STATUS AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_number VARCHAR NOT NULL,
    room_type ROOM_TYPE NOT NULL,
    price_per_night DECIMAL NOT NULL,
    currency VARCHAR(3) NOT NULL,
    room_status ROOM_STATUS NOT NULL,
    CONSTRAINT rooms_id_hotel_id_unique UNIQUE (id, hotel_id),
    CONSTRAINT rooms_hotel_room_number_unique UNIQUE (hotel_id, room_number)
);

CREATE TABLE room_amenities (
    room_id UUID NOT NULL,
    hotel_id UUID NOT NULL,
    amenity_id UUID NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (room_id, amenity_id),
    CONSTRAINT room_amenities_room_hotel_fk
        FOREIGN KEY (room_id, hotel_id) REFERENCES rooms(id, hotel_id) ON DELETE CASCADE
);

CREATE INDEX room_amenities_amenity_id_idx ON room_amenities (amenity_id);

DROP INDEX hotel_amenities_amenity_id_idx;
DROP TABLE hotel_amenities;
