/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
  RoomMutationResult create(UUID hotelId, CreateRoomCommand request);

  RoomMutationResult update(UUID hotelId, UUID roomId, UpdateRoomCommand request);

  RoomPage findPage(UUID hotelId, RoomSearchCriteria criteria, int offset, int limit);

  Optional<RoomRecord> findById(UUID hotelId, UUID roomId);
}
