/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

public sealed interface HotelMutationResult
    permits HotelMutationResult.Success, HotelMutationResult.NameConflict {
  record Success(HotelRecord hotel) implements HotelMutationResult {}

  record NameConflict() implements HotelMutationResult {}
}
