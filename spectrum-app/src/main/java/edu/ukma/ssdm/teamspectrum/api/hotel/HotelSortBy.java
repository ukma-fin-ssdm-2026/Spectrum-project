/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

public enum HotelSortBy {
  PricePerNight(Values.PricePerNight),
  Rating(Values.Rating);

  private final String sortByName;

  HotelSortBy(String sortByName) {
    this.sortByName = sortByName;
  }

  @Override
  public String toString() {
    return sortByName;
  }

  public static class Values {
    public static final String PricePerNight = "PRICE_PER_NIGHT";
    public static final String Rating = "RATING";
  }
}
