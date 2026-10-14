/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;

public interface TokenFactory {
  IssuedToken create(User user);
}
