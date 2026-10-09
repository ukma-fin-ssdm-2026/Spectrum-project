/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

public interface UserFactory {
  User create(String email, String password);
}
