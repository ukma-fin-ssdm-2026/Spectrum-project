/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.time.Instant;
import java.util.UUID;

public interface WorkerInvitationRepository {
  void create(String email, String tokenHash, Instant createdAt, Instant expiresAt);

  boolean isActive(String tokenHash, Instant now);

  User consumeAndRegister(String tokenHash, UUID userId, String passwordHash, Instant now);
}
