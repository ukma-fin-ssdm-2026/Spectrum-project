/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.param;

import edu.ukma.ssdm.teamspectrum.api.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.api.room.RoomSortBy;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;

@Provider
public class EnumParamConverterProvider implements ParamConverterProvider {
  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, java.lang.reflect.Type genericType, Annotation[] annotations) {
    if (rawType == RoomSortBy.class) {
      return (ParamConverter<T>) new EnumParamConverter<>(RoomSortBy.class);
    } else if (rawType == SortOrder.class) {
      return (ParamConverter<T>) new EnumParamConverter<>(SortOrder.class);
    } else return null;
  }

  private static final class EnumParamConverter<E extends Enum<E>> implements ParamConverter<E> {
    private final Class<E> enumType;
    private final E[] values;

    private EnumParamConverter(Class<E> enumType) {
      this.enumType = enumType;
      this.values = enumType.getEnumConstants();
    }

    @Override
    public E fromString(String value) {
      if (value == null) {
        return null;
      }

      return Arrays.stream(values)
          .filter(candidate -> candidate.toString().equals(value))
          .findFirst()
          .orElseThrow(
              () ->
                  new BadRequestException(
                      "Invalid value '%s' for %s. Allowed values: %s"
                          .formatted(value, enumType.getSimpleName(), allowedValues())));
    }

    @Override
    public String toString(E value) {
      return value == null ? null : value.toString();
    }

    private String allowedValues() {
      return Arrays.stream(values).map(Object::toString).collect(Collectors.joining(", "));
    }
  }
}
