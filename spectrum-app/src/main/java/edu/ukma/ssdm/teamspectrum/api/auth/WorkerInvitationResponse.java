/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import java.time.Instant;

public record WorkerInvitationResponse(String registrationUrl, Instant expiresAt) {}
