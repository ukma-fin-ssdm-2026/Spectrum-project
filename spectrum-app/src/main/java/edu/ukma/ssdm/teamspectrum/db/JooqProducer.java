/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import javax.sql.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

@ApplicationScoped
public class JooqProducer {
  @Produces
  @ApplicationScoped
  DSLContext dslContext(
      DataSource dataSource,
      @ConfigProperty(name = "app.jooq.schema", defaultValue = "public") String schema) {
    Settings settings =
        new Settings()
            .withRenderMapping(
                new RenderMapping()
                    .withSchemata(new MappedSchema().withInput("public").withOutput(schema)));
    return DSL.using(dataSource, SQLDialect.POSTGRES, settings);
  }
}
