/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

public enum Role {
  CLIENT(Values.CLIENT),
  WORKER(Values.WORKER),
  ADMIN(Values.ADMIN);

  private final String roleName;

  Role(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public String toString() {
    return roleName;
  }

  public static class Values {
    public static final String CLIENT = "CLIENT";
    public static final String WORKER = "WORKER";
    public static final String ADMIN = "ADMIN";
  }
}
