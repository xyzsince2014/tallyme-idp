package tallyme.idp.domain.converters;

import java.sql.Date;

import java.time.LocalDate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// this converter is automatically applied to Entity's LocalDate fields
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

  // convert a LocalDate object to a java.sql.Data one
  @Override
  public Date convertToDatabaseColumn(LocalDate localDate) {
    return localDate == null ? null : Date.valueOf(localDate);
  }

  // convert a java.sql.Date object to a LocalDate one
  @Override
  public LocalDate convertToEntityAttribute(Date date) {
    return date == null ? null : date.toLocalDate();
  }
}
