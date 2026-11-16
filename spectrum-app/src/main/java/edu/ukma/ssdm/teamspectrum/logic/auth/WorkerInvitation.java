/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.time.Instant;

public record WorkerInvitation(String registrationUrl, Instant expiresAt) {}
