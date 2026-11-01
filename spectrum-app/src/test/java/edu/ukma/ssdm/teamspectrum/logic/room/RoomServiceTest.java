/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RoomServiceTest {
  private static final UUID HOTEL_ID = UUID.randomUUID();
  private static final UUID ROOM_ID = UUID.randomUUID();

  private RoomRepository repository;
  private RoomServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(RoomRepository.class);
    service = new RoomServiceImpl(repository);
  }

  @Test
  void createDelegatesToRepositoryAndReturnsItsResult() {
    var request =
        new CreateRoomCommand(ROOM_ID, "101", RoomType.SINGLE, null, RoomStatus.ACTIVE, Set.of());
    var result = new RoomMutationResult.RoomNumberConflict();
    when(repository.create(HOTEL_ID, request)).thenReturn(result);

    assertSame(result, service.create(HOTEL_ID, request));

    verify(repository).create(HOTEL_ID, request);
  }

  @Test
  void updateDelegatesToRepositoryAndReturnsItsResult() {
    var request = new UpdateRoomCommand("102", null, null, null, null);
    var result = new RoomMutationResult.RoomNumberConflict();
    when(repository.update(HOTEL_ID, ROOM_ID, request)).thenReturn(result);

    assertSame(result, service.update(HOTEL_ID, ROOM_ID, request));

    verify(repository).update(HOTEL_ID, ROOM_ID, request);
  }

  @Test
  void findPageCalculatesOffsetAndPassesLimitToRepository() {
    var criteria = criteria(2, 5);
    var result = new RoomPage(List.of(), 0);
    when(repository.findPage(HOTEL_ID, criteria, 10, 5)).thenReturn(result);

    assertSame(result, service.findPage(HOTEL_ID, criteria));

    verify(repository).findPage(HOTEL_ID, criteria, 10, 5);
  }

  @Test
  void findPageThrowsOnOffsetOverflowWithoutCallingRepository() {
    var criteria = criteria(Integer.MAX_VALUE, 2);

    assertThrows(ArithmeticException.class, () -> service.findPage(HOTEL_ID, criteria));

    verify(repository, never())
        .findPage(Mockito.eq(HOTEL_ID), Mockito.eq(criteria), Mockito.anyInt(), Mockito.anyInt());
  }

  @Test
  void findByIdDelegatesToRepositoryAndReturnsItsResult() {
    var result = Optional.<RoomRecord>empty();
    when(repository.findById(HOTEL_ID, ROOM_ID)).thenReturn(result);

    assertSame(result, service.findById(HOTEL_ID, ROOM_ID));

    verify(repository).findById(HOTEL_ID, ROOM_ID);
  }

  private static RoomSearchCriteria criteria(int page, int limit) {
    return new RoomSearchCriteria(
        null, null, null, null, BigDecimal.ZERO, null, Set.of(), null, SortOrder.ASC, page, limit);
  }
}
