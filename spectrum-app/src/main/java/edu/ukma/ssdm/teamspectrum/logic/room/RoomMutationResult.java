/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

public sealed interface RoomMutationResult
    permits RoomMutationResult.Success,
        RoomMutationResult.NotFound,
        RoomMutationResult.RoomNumberConflict {
  record Success(RoomRecord room) implements RoomMutationResult {}

  record NotFound() implements RoomMutationResult {}

  record RoomNumberConflict() implements RoomMutationResult {}
}
