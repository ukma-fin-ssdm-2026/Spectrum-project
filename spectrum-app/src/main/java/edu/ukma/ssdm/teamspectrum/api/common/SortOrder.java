/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common;

public enum SortOrder {
  Asc(Values.Asc),
  Desc(Values.Desc);

  private final String sortOrderName;

  SortOrder(String sortOrderName) {
    this.sortOrderName = sortOrderName;
  }

  @Override
  public String toString() {
    return sortOrderName;
  }

  public static class Values {
    public static final String Asc = "ASC";
    public static final String Desc = "DESC";
  }
}
