/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.time.Instant;
import java.util.UUID;

public interface WorkerInvitationRepository {
  void create(String email, String tokenHash, Instant expiresAt);

  boolean isActive(String tokenHash);

  User consumeAndRegister(String tokenHash, UUID userId, String passwordHash);
}
