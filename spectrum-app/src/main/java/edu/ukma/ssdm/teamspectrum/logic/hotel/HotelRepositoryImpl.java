/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Amenities.AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.HotelAmenities.HOTEL_AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Hotels.HOTELS;

import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.HotelsRecord;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;

@ApplicationScoped
public class HotelRepositoryImpl implements HotelRepository {
  private final DSLContext dsl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "DSLContext is an intentionally shared injected database dependency.")
  public HotelRepositoryImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<HotelRecord> search(HotelSearchCriteria criteria, int offset, int limit) {
    var rows =
        dsl.selectFrom(HOTELS)
            .where(matching(criteria))
            .orderBy(sortField(criteria))
            .offset(offset)
            .limit(limit)
            .fetch();
    return toHotels(rows);
  }

  @Override
  public long count(HotelSearchCriteria criteria) {
    return dsl.fetchCount(dsl.selectFrom(HOTELS).where(matching(criteria)));
  }

  @Override
  public Optional<HotelRecord> findById(UUID id) {
    return dsl.selectFrom(HOTELS)
        .where(HOTELS.ID.eq(id))
        .fetchOptional(record -> toHotel(record, fetchAmenities(id)));
  }

  @Override
  public Optional<HotelRecord> findByName(String name) {
    if (name == null) {
      return Optional.empty();
    }
    return dsl.selectFrom(HOTELS)
        .where(DSL.lower(HOTELS.NAME).eq(name.trim().toLowerCase()))
        .fetchOptional(record -> toHotel(record, fetchAmenities(record.getId())));
  }

  @Override
  public void add(HotelRecord hotel) {
    dsl.transaction(
        configuration -> {
          DSLContext tx = DSL.using(configuration);
          tx.insertInto(HOTELS)
              .set(HOTELS.ID, hotel.id())
              .set(HOTELS.NAME, hotel.name())
              .set(HOTELS.CITY, hotel.address().city())
              .set(HOTELS.DISTRICT, hotel.address().district())
              .set(HOTELS.STREET, hotel.address().street())
              .set(HOTELS.BUILDING_NUMBER, hotel.address().buildingNumber())
              .set(HOTELS.PICTURE_URL, hotel.pictureUrl())
              .set(HOTELS.PRICE_PER_NIGHT, hotel.pricePerNight())
              .set(HOTELS.RATING, hotel.rating())
              .execute();
          if (!hotel.amenities().isEmpty()) {
            var amenityRows = hotel.amenities().stream().map(DSL::row).toList();
            var amenities =
                tx.insertInto(AMENITIES, AMENITIES.NAME)
                    .valuesOfRows(amenityRows)
                    .onConflict(AMENITIES.NAME)
                    .doUpdate()
                    .set(AMENITIES.NAME, DSL.excluded(AMENITIES.NAME))
                    .returning(AMENITIES.ID, AMENITIES.NAME)
                    .fetch();
            var hotelAmenityRows =
                amenities.stream()
                    .map(amenity -> DSL.row(hotel.id(), amenity.get(AMENITIES.ID)))
                    .collect(Collectors.toList());
            tx.insertInto(HOTEL_AMENITIES, HOTEL_AMENITIES.HOTEL_ID, HOTEL_AMENITIES.AMENITY_ID)
                .valuesOfRows(hotelAmenityRows)
                .execute();
          }
        });
  }

  public Map<UUID, Set<String>> fetchAmenities(Set<UUID> hotelIds) {
    return dsl.select(HOTEL_AMENITIES.HOTEL_ID, AMENITIES.NAME)
        .from(HOTEL_AMENITIES)
        .join(AMENITIES)
        .on(HOTEL_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
        .where(HOTEL_AMENITIES.HOTEL_ID.in(hotelIds))
        .fetch()
        .collect(
            Collectors.groupingBy(
                row -> row.get(HOTEL_AMENITIES.HOTEL_ID),
                Collectors.mapping(row -> row.get(AMENITIES.NAME), Collectors.toSet())));
  }

  public Set<String> fetchAmenities(UUID hotelId) {
    return dsl
        .select(AMENITIES.NAME)
        .from(HOTEL_AMENITIES)
        .join(AMENITIES)
        .on(HOTEL_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
        .where(HOTEL_AMENITIES.HOTEL_ID.eq(hotelId))
        .fetch()
        .stream()
        .map(r -> r.get(AMENITIES.NAME))
        .collect(Collectors.toSet());
  }

  private static Condition matching(HotelSearchCriteria criteria) {
    Condition condition = DSL.trueCondition();
    if (criteria.location() != null) {
      String location = criteria.location().trim().toLowerCase();
      condition =
          condition.and(
              DSL.lower(HOTELS.CITY).eq(location).or(DSL.lower(HOTELS.DISTRICT).eq(location)));
    }
    if (criteria.minPrice() != null) {
      condition = condition.and(HOTELS.PRICE_PER_NIGHT.ge(criteria.minPrice()));
    }
    if (criteria.maxPrice() != null) {
      condition = condition.and(HOTELS.PRICE_PER_NIGHT.le(criteria.maxPrice()));
    }
    if (criteria.minRating() != null) {
      condition = condition.and(HOTELS.RATING.ge(criteria.minRating()));
    }
    if (criteria.maxRating() != null) {
      condition = condition.and(HOTELS.RATING.le(criteria.maxRating()));
    }
    if (!criteria.amenities().isEmpty()) {
      condition =
          condition.and(
              DSL.selectCount()
                  .from(HOTEL_AMENITIES)
                  .join(AMENITIES)
                  .on(HOTEL_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
                  .where(HOTEL_AMENITIES.HOTEL_ID.eq(HOTELS.ID))
                  .and(AMENITIES.NAME.in(criteria.amenities()))
                  .asField()
                  .eq(criteria.amenities().size()));
    }
    return condition;
  }

  private static Field<?> sortField(HotelSortBy sortBy) {
    return switch (sortBy) {
      case Rating -> HOTELS.PRICE_PER_NIGHT;
      case PricePerNight -> HOTELS.RATING;
      case null -> HOTELS.RATING;
    };
  }

  private static SortField<?> sortField(HotelSearchCriteria criteria) {
    Field<?> field = sortField(criteria.sortBy());
    return switch (criteria.order()) {
      case Desc -> field.desc();
      case Asc -> field.asc();
      case null -> field.desc();
    };
  }

  private HotelRecord toHotel(HotelsRecord hotel, Set<String> amenities) {
    return new HotelRecord(
        hotel.getId(),
        hotel.getName(),
        new HotelAddress(
            hotel.getCity(), hotel.getDistrict(), hotel.getStreet(), hotel.getBuildingNumber()),
        hotel.getPictureUrl(),
        hotel.getPricePerNight(),
        hotel.getRating(),
        amenities != null ? amenities : Set.of());
  }

  private List<HotelRecord> toHotels(List<HotelsRecord> records) {
    if (records.isEmpty()) {
      return List.of();
    }
    var ids = records.stream().map(record -> record.get(HOTELS.ID)).collect(Collectors.toSet());
    Map<UUID, Set<String>> amenitiesByHotel = fetchAmenities(ids);
    return records.stream()
        .map(hotel -> toHotel(hotel, amenitiesByHotel.get(hotel.getId())))
        .toList();
  }
}
