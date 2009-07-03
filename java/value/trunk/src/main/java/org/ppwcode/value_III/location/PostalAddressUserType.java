package org.ppwcode.value_III.location;

import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.ppwcode.util.reflect_I.InstanceHelpers;
import org.ppwcode.vernacular.value_III.ImmutableValue;
import org.ppwcode.vernacular.value_III.hibernate3.AbstractImmutableValueCompositeUserType;

/**
 * <p>
 * Hibernate 3 user type to store and retrieve {@link PostalAddress} instances.
 * </p>
 * <p>
 * {@link PostalAddress} instances are stored in xxx columns of type VARCHAR. The first contains the FQCN of the actual
 * identifier class to which the instance belongs. The second column contains the identifier. Both are limited to 255
 * characters.
 * </p>
 * 
 * @author David Van Keer
 * @author PeopleWare NV
 */
@Copyright("2008 - $Date: 2009-06-25 09:44:06 +0200 (Thu, 25 Jun 2009) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 4400 $", date = "$Date: 2009-06-25 09:44:06 +0200 (Thu, 25 Jun 2009) $")
public class PostalAddressUserType extends AbstractImmutableValueCompositeUserType {

  private static PostalCodeUserType POSTALCODE_USER_TYPE = new PostalCodeUserType();

  @Override
  public Class<? extends ImmutableValue> returnedClass() {
    return PostalAddress.class;
  }

  public Type[] getPropertyTypes() {
    Type[] result = new Type[] {};
    Type[] postalCodeTypes = POSTALCODE_USER_TYPE.getPropertyTypes();
    Type[] otherTypes = new Type[] {
      Hibernate.LOCALE,
      Hibernate.STRING,
      Hibernate.STRING
    };
    System.arraycopy(postalCodeTypes, 0, result, 0, postalCodeTypes.length);
    System.arraycopy(otherTypes, 0, result, postalCodeTypes.length, otherTypes.length);
    return result;
  }

  public final String[] getPropertyNames() {
    String[] result = new String[] {};
    String[] postalCodeNames = POSTALCODE_USER_TYPE.getPropertyNames();
    String[] otherNames = new String[] {
      "$locale",
      "$city",
      "$streetAddress"
    };
    System.arraycopy(postalCodeNames, 0, result, 0, postalCodeNames.length);
    System.arraycopy(otherNames, 0, result, postalCodeNames.length, otherNames.length);
    return result;

  }

  public Object getPropertyValue(Object obj, int prop) {
    try {
      PostalAddress id = (PostalAddress) obj;
      if (prop == 0) {
        return id.getPostalCode().getClass();
      } else if (prop == 1) {
        return id.getPostalCode().getIdentifier();
      } else if (prop == 2) {
        return id.getPostalCode().getCountry().getValue();
      } else if (prop == 3) {
        return id.getLocale();
      } else if (prop == 4) {
        return id.getCity();
      } else if (prop == 5) {
        return id.getStreetAddress();
      } else {
        throw new HibernateException("getPropertyValue with wrong index for " + returnedClass());
      }
    } catch (ClassCastException exc) {
      throw new HibernateException("cannot cast component into " + returnedClass(), exc);
    }
  }

  @SuppressWarnings("unchecked")
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
      throws HibernateException, SQLException {
    PostalAddress result = null;
    PostalCode postalCode = null;
    Locale locale = null;
    String city = null;
    String streetAddress = null;
    try {
      postalCode = (PostalCode) POSTALCODE_USER_TYPE.nullSafeGet(rs, new String[] {
        names[0],
        names[1],
        names[2]
      }, session, owner);
      locale = (Locale) Hibernate.LOCALE.nullSafeGet(rs, names[3], session, owner);
      city = (String) Hibernate.STRING.nullSafeGet(rs, names[4], session, owner);
      streetAddress = (String) Hibernate.STRING.nullSafeGet(rs, names[5], session, owner);
      if (postalCode == null && locale == null && city == null && streetAddress == null) {
        result = null;
      } else {
        result = InstanceHelpers.robustNewInstance(PostalAddress.class, postalCode, locale, city, streetAddress);
      }
      return result;
    } catch (ArrayIndexOutOfBoundsException exc) {
      throw new HibernateException("data received from database is not as expected: expected array of 4 values", exc);
    } catch (ClassCastException exc) {
      throw new HibernateException("data received from database is not as expected: expected array of 4 values", exc);
    } catch (InvocationTargetException exc) {
      throw new HibernateException("Creation of identifier of type " + PostalAddress.class
          + " failed with an application exception", exc);
    }
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
      throws HibernateException, SQLException {
    if (value != null && !returnedClass().isInstance(value)) {
      throw new HibernateException("this user type can only handle values of type "
          + returnedClass().getCanonicalName() + "; " + value.getClass().getCanonicalName() + " is not supported");
    } else if (value == null) {
      POSTALCODE_USER_TYPE.nullSafeSet(st, null, index, session);
      Hibernate.LOCALE.nullSafeSet(st, null, index + 1, session);
      Hibernate.STRING.nullSafeSet(st, null, index + 2, session);
      Hibernate.STRING.nullSafeSet(st, null, index + 3, session);
    } else {
      PostalAddress id = (PostalAddress) value;
      POSTALCODE_USER_TYPE.nullSafeSet(st, id.getPostalCode(), index, session);
      Hibernate.LOCALE.nullSafeSet(st, id.getLocale(), index + 1, session);
      Hibernate.STRING.nullSafeSet(st, id.getCity(), index + 2, session);
      Hibernate.STRING.nullSafeSet(st, id.getStreetAddress(), index + 3, session);
    }
  }
}
