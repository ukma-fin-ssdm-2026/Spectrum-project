/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

public interface UserFactory {
  String normalizeEmail(String email);

  User create(String email, String password);
}
