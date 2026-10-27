/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoomServiceImpl implements RoomService {

  private final RoomRepository repository;

  @Inject
  public RoomServiceImpl(RoomRepository repository) {
    this.repository = repository;
  }

  @Override
  public RoomMutationResult create(UUID hotelId, CreateRoomCommand request) {
    return repository.create(hotelId, request);
  }

  @Override
  public RoomMutationResult update(UUID hotelId, UUID roomId, UpdateRoomCommand request) {
    return repository.update(hotelId, roomId, request);
  }

  @Override
  public RoomPage findPage(UUID hotelId, RoomSearchCriteria criteria) {
    int offset = Math.multiplyExact(criteria.page(), criteria.limit());
    return repository.findPage(hotelId, criteria, offset, criteria.limit());
  }

  @Override
  public Optional<RoomRecord> findById(UUID hotelId, UUID roomId) {
    return repository.findById(hotelId, roomId);
  }
}
