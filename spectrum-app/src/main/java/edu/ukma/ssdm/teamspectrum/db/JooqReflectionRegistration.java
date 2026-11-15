/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.db;

import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.AmenitiesRecord;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.HotelsRecord;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.RoomAmenitiesRecord;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.RoomsRecord;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.UsersRecord;
import io.quarkus.runtime.annotations.RegisterForReflection;

/** Registers generated jOOQ records for reflective construction in native images. */
@RegisterForReflection(
    targets = {
      AmenitiesRecord.class,
      HotelsRecord.class,
      RoomAmenitiesRecord.class,
      RoomsRecord.class,
      UsersRecord.class
    })
public final class JooqReflectionRegistration {
  private JooqReflectionRegistration() {}
}
