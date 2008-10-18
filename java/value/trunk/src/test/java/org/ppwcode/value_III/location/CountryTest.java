/*<license>
Copyright 2004 - $Date: 2008-10-17 16:01:32 +0200 (Fri, 17 Oct 2008) $ by PeopleWare n.v..

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</license>*/

package org.ppwcode.value_III.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ppwcode.value_III.location.Country.NO_COUNTRY;
import static org.ppwcode.value_III.location.Country.VALUES;

import java.beans.PropertyEditorManager;
import java.util.Locale;

import org.junit.Test;


public class CountryTest {

  @Test
  public void testCountry() {
    Country subject = new Country();
    assertEquals("BE", subject.getValue());
  }

  @Test
  public void testVALUES() {
    assertNotNull(VALUES);
    assertFalse(VALUES.keySet().contains(null));
    assertFalse(VALUES.values().contains(null));
    for (String cc : VALUES.keySet()) {
      assertEquals(cc, VALUES.get(cc).getValue());
    }
    for (Country c : VALUES.values()) {
      assertTrue("not an ISO country code: \"" + c + "\"", (! c.equals(NO_COUNTRY) ? contains(Locale.getISOCountries(), c.getValue()) : true));
    }
    for (String cc : Locale.getISOCountries()) {
      assertNotNull(VALUES.get(cc));
    }
  }

  private <_T_> boolean contains(_T_[] countries, _T_ value) {
    for (_T_ _t_ : countries) {
      if (eqn(_t_, value)) {
        return true;
      }
    }
    return false;
  }

  private <_T_> boolean eqn(_T_ one, _T_ other) {
    return (one == null) ? (other == null) : one.equals(other);
  }

  @Test
  public void testNO_COUNTRY() {
    assertEquals(VALUES.get(" "), NO_COUNTRY);
  }

  @Test
  public void demo() {
    // there are no dutch translations in Java SE?!
    CountryEditor ce = (CountryEditor)PropertyEditorManager.findEditor(Country.class);
    ce.setDisplayLocale(new Locale("nl"));
    System.out.println("nr of countries: " + VALUES.size());
    for (Country c : VALUES.values()) {
      System.out.print(c.toString() + ": ");
      ce.setValue(c);
      System.out.println(ce.getLabel());
    }
  }

}

