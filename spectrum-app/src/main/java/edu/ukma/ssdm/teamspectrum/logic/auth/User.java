/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.util.UUID;

public record User(UUID id, String email, String passwordHash, Role role) {}
