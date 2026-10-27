/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Amenities.AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Hotels.HOTELS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.RoomAmenities.ROOM_AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Rooms.ROOMS;

import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.RoomAmenitiesRecord;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.RoomsRecord;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jooq.*;
import org.jooq.impl.DSL;

@ApplicationScoped
public class RoomRepositoryImpl implements RoomRepository {
  private final DSLContext dsl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "DSLContext is an intentionally shared injected database dependency.")
  public RoomRepositoryImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  private static RoomsRecord mkRoomsRecord(UUID hotelId, CreateRoomCommand request) {
    return mkRoomsRecord(
        hotelId,
        request.roomId(),
        request.roomNumber(),
        request.roomType(),
        request.pricePerNight(),
        request.roomStatus());
  }

  private static edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomType mkRoomType(RoomType roomType) {
    return edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomType.valueOf(roomType.name());
  }

  private static edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomStatus mkRoomStatus(
      RoomStatus roomStatus) {
    return edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomStatus.valueOf(roomStatus.name());
  }

  private static RoomsRecord mkRoomsRecord(
      UUID hotelId,
      UUID roomId,
      String roomNumber,
      RoomType roomType,
      org.javamoney.moneta.Money pricePerNight,
      RoomStatus roomStatus) {
    return new RoomsRecord(
        roomId,
        hotelId,
        roomNumber,
        mkRoomType(roomType),
        pricePerNight.getNumber().numberValueExact(BigDecimal.class),
        pricePerNight.getCurrency().getCurrencyCode(),
        mkRoomStatus(roomStatus));
  }

  private static SelectConditionStep<Record1<UUID>> selectHotelById(
      DSLContext context, UUID hotelId) {
    return context.select(HOTELS.ID).from(HOTELS).where(HOTELS.ID.eq(hotelId));
  }

  @Override
  public RoomMutationResult create(UUID hotelId, CreateRoomCommand request) {
    var record = mkRoomsRecord(hotelId, request);
    return dsl.transactionResult(
        configuration -> {
          DSLContext tx = DSL.using(configuration);
          var hotelExists = selectHotelById(tx, hotelId).forKeyShare().fetchOptional().isPresent();
          if (!hotelExists) {
            return new RoomMutationResult.NotFound();
          }
          var inserted =
              tx.insertInto(ROOMS)
                  .set(record)
                  .onConflict(ROOMS.HOTEL_ID, ROOMS.ROOM_NUMBER)
                  .doNothing()
                  .returning()
                  .fetchOptional();
          if (inserted.isEmpty()) {
            return new RoomMutationResult.RoomNumberConflict();
          }
          persistAmenities(tx, request.roomId(), hotelId, request.amenities());
          return new RoomMutationResult.Success(
              toRoom(inserted.orElseThrow(), request.amenities()));
        });
  }

  private void deleteRoomAmenities(DSLContext ctx, UUID roomId, UUID hotelId) {
    ctx.deleteFrom(ROOM_AMENITIES)
        .where(ROOM_AMENITIES.ROOM_ID.eq(roomId).and(ROOM_AMENITIES.HOTEL_ID.eq(hotelId)))
        .execute();
  }

  private RoomsRecord updateRoom(UpdateRoomCommand request, RoomsRecord old) {
    if (request.roomNumber() != null) {
      old.setRoomNumber(request.roomNumber());
    }
    if (request.roomType() != null) {
      old.setRoomType(mkRoomType(request.roomType()));
    }
    if (request.pricePerNight() != null) {
      old.setPricePerNight(request.pricePerNight().getNumber().numberValueExact(BigDecimal.class));
      old.setCurrency(request.pricePerNight().getCurrency().getCurrencyCode());
    }
    if (request.roomStatus() != null) {
      old.setRoomStatus(mkRoomStatus(request.roomStatus()));
    }
    return old;
  }

  @Override
  public RoomMutationResult update(UUID hotelId, UUID roomId, UpdateRoomCommand request) {
    return dsl.transactionResult(
        configuration -> {
          DSLContext tx = DSL.using(configuration);
          selectHotelById(tx, hotelId).forUpdate().fetchOptional();
          var existing = mkFindById(tx, hotelId, roomId).forUpdate().fetchOptional();
          if (existing.isEmpty()) {
            return new RoomMutationResult.NotFound();
          } else {
            var old = existing.orElseThrow();
            var recordToUpdate = updateRoom(request, old);
            var roomNumberInUse =
                tx.fetchExists(
                    tx.selectOne()
                        .from(ROOMS)
                        .where(
                            ROOMS
                                .HOTEL_ID
                                .eq(hotelId)
                                .and(ROOMS.ROOM_NUMBER.eq(recordToUpdate.getRoomNumber()))
                                .and(ROOMS.ID.ne(roomId))));
            if (roomNumberInUse) {
              return new RoomMutationResult.RoomNumberConflict();
            }
            var updated =
                tx.update(ROOMS)
                    .set(recordToUpdate)
                    .where(ROOMS.HOTEL_ID.eq(hotelId).and(ROOMS.ID.eq(roomId)))
                    .returning()
                    .fetchAny();
            assert updated != null;
            if (request.amenities() != null) {
              deleteRoomAmenities(tx, roomId, hotelId);
              persistAmenities(tx, roomId, hotelId, request.amenities());
              return new RoomMutationResult.Success(toRoom(updated, request.amenities()));
            } else {
              var amenities = fetchAmenities(tx, roomId);
              return new RoomMutationResult.Success(toRoom(updated, amenities));
            }
          }
        });
  }

  @Override
  public RoomPage findPage(UUID hotelId, RoomSearchCriteria criteria, int offset, int limit) {
    return dsl.transactionResult(
        configuration -> {
          DSLContext tx = DSL.using(configuration);

          long count = tx.fetchCount(selectMatchingRooms(tx, hotelId, criteria));
          List<RoomRecord> rooms =
              selectMatchingRooms(tx, hotelId, criteria)
                  .orderBy(sortField(criteria))
                  .offset(offset)
                  .limit(limit)
                  .fetch(record -> toRoom(record, fetchAmenities(tx, record.getId())));

          return new RoomPage(rooms, count);
        },
        Isolation.REPEATABLE_READ);
  }

  @Override
  public Optional<RoomRecord> findById(UUID hotelId, UUID roomId) {
    return findById(dsl, hotelId, roomId);
  }

  private SelectConditionStep<RoomsRecord> mkFindById(
      DSLContext context, UUID hotelId, UUID roomId) {
    return context.selectFrom(ROOMS).where(ROOMS.HOTEL_ID.eq(hotelId).and(ROOMS.ID.eq(roomId)));
  }

  private static SelectConditionStep<RoomsRecord> selectMatchingRooms(
      DSLContext context, UUID hotelId, RoomSearchCriteria criteria) {
    return context.selectFrom(ROOMS).where(matching(hotelId, criteria));
  }

  private Optional<RoomRecord> findById(DSLContext ctx, UUID hotelId, UUID roomId) {
    return mkFindById(ctx, hotelId, roomId)
        .fetchOptional(record -> toRoom(record, fetchAmenities(ctx, roomId)));
  }

  private Optional<RoomRecord> findByIdForUpdate(DSLContext ctx, UUID hotelId, UUID roomId) {
    return mkFindById(ctx, hotelId, roomId)
        .forUpdate()
        .fetchOptional(record -> toRoom(record, fetchAmenities(ctx, roomId)));
  }

  private Set<String> fetchAmenities(DSLContext context, UUID roomId) {
    return context
        .select(AMENITIES.NAME)
        .from(ROOM_AMENITIES)
        .join(AMENITIES)
        .on(ROOM_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
        .where(ROOM_AMENITIES.ROOM_ID.eq(roomId))
        .fetchSet(AMENITIES.NAME);
  }

  private static void persistAmenities(
      DSLContext context, UUID roomId, UUID hotelId, Set<String> names) {
    if (names.isEmpty()) {
      return;
    }
    var amenityRows = names.stream().map(DSL::row).toList();
    var amenities =
        context
            .insertInto(AMENITIES, AMENITIES.NAME)
            .valuesOfRows(amenityRows)
            .onConflict(AMENITIES.NAME)
            .doUpdate()
            .set(AMENITIES.NAME, DSL.excluded(AMENITIES.NAME))
            .returning(AMENITIES.ID)
            .fetch(AMENITIES.ID);
    var joins = amenities.stream().map(id -> new RoomAmenitiesRecord(roomId, hotelId, id)).toList();
    context
        .insertInto(ROOM_AMENITIES)
        .set(joins)
        .onConflict(ROOM_AMENITIES.ROOM_ID, ROOM_AMENITIES.AMENITY_ID)
        .doNothing()
        .execute();
  }

  private static RoomRecord toRoom(RoomsRecord record, Set<String> amenities) {
    return new RoomRecord(
        record.getId(),
        record.getHotelId(),
        record.getRoomNumber(),
        RoomType.valueOf(record.getRoomType().getLiteral()),
        org.javamoney.moneta.Money.of(record.getPricePerNight(), record.getCurrency()),
        RoomStatus.valueOf(record.getRoomStatus().getLiteral()),
        amenities);
  }

  private static Condition matching(UUID hotelId, RoomSearchCriteria criteria) {
    Condition condition = DSL.trueCondition();
    if (hotelId != null) {
      condition = condition.and(ROOMS.HOTEL_ID.eq(hotelId));
    }
    if (criteria.roomType() != null) {
      condition =
          condition.and(
              ROOMS.ROOM_TYPE.eq(
                  edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomType.valueOf(
                      criteria.roomType().name())));
    }
    if (criteria.roomStatus() != null) {
      condition =
          condition.and(
              ROOMS.ROOM_STATUS.eq(
                  edu.ukma.ssdm.teamspectrum.db.jooq.enums.RoomStatus.valueOf(
                      criteria.roomStatus().name())));
    }
    if (criteria.minPrice() != null) {
      condition = condition.and(ROOMS.PRICE_PER_NIGHT.ge(criteria.minPrice()));
    }
    if (criteria.maxPrice() != null) {
      condition = condition.and(ROOMS.PRICE_PER_NIGHT.le(criteria.maxPrice()));
    }
    if (!criteria.amenities().isEmpty()) {
      condition =
          condition.and(
              DSL.selectCount()
                  .from(ROOM_AMENITIES)
                  .join(AMENITIES)
                  .on(ROOM_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
                  .where(ROOM_AMENITIES.ROOM_ID.eq(ROOMS.ID))
                  .and(AMENITIES.NAME.in(criteria.amenities()))
                  .asField()
                  .eq(criteria.amenities().size()));
    }
    return condition;
  }

  private static SortField<?> sortField(RoomSearchCriteria criteria) {
    Field<?> field =
        switch (criteria.sortBy()) {
          case RoomNumber -> ROOMS.ROOM_NUMBER;
          case PricePerNight -> ROOMS.PRICE_PER_NIGHT;
          case null -> ROOMS.PRICE_PER_NIGHT;
        };
    return switch (criteria.order()) {
      case Asc -> field.asc();
      case Desc -> field.desc();
      case null -> field.asc();
    };
  }
}
