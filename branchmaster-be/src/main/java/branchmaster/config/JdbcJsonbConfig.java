package branchmaster.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.List;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

@Configuration
public class JdbcJsonbConfig extends AbstractJdbcConfiguration {

  private final ObjectMapper objectMapper;

  public JdbcJsonbConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected List<?> userConverters() {
    return List.of(
        new JsonNodeToJsonbPgObjectConverter(),
        new PgObjectToJsonNodeConverter(objectMapper),
        new StringToJsonNodeConverter(objectMapper));
  }

  @WritingConverter
  public static class JsonNodeToJsonbPgObjectConverter implements Converter<JsonNode, PGobject> {
    @Override
    public PGobject convert(JsonNode source) {
      if (source == null) return null;

      PGobject pg = new PGobject();
      pg.setType("jsonb");
      try {
        pg.setValue(source.toString());
      } catch (SQLException e) {
        throw new IllegalArgumentException("Failed to write jsonb", e);
      }
      return pg;
    }
  }

  @ReadingConverter
  public static class PgObjectToJsonNodeConverter implements Converter<PGobject, JsonNode> {
    private final ObjectMapper objectMapper;

    public PgObjectToJsonNodeConverter(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode convert(PGobject source) {
      if (source == null || source.getValue() == null) return null;
      try {
        return objectMapper.readTree(source.getValue());
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to read jsonb PGobject", e);
      }
    }
  }

  @ReadingConverter
  public static class StringToJsonNodeConverter implements Converter<String, JsonNode> {
    private final ObjectMapper objectMapper;

    public StringToJsonNodeConverter(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode convert(String source) {
      if (source == null || source.isBlank()) return null;
      try {
        return objectMapper.readTree(source);
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to read jsonb String", e);
      }
    }
  }
}
