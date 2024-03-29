/*<license>
Copyright 2004 - $Date$ by PeopleWare n.v..

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

package org.ppwcode.vernacular.value_III;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.ppwcode.util.reflect_I.TypeHelpers.type;

import java.util.Map;

import org.ppwcode.util.test.contract.Contract;



@SuppressWarnings("unchecked")
public class _Contract_AbstractEnumEditor<_Enum_ extends Enum<_Enum_>> extends Contract<AbstractEnumEditor> {

  public _Contract_AbstractEnumEditor() {
    super(AbstractEnumEditor.class);
  }

  @Override
  public void assertInvariants(AbstractEnumEditor ee) {
    super.assertInvariants(ee);
  }

  public void assertGetLabel(AbstractEnumEditor<_Enum_> ee, String result) {
    _Contract_EnumEditor eeC = (_Contract_EnumEditor)getDirectSuperContracts().get(EnumEditor.class);
    eeC.assertGetLabel(ee, result);
  }




  public void assertGetEnumType(AbstractEnumEditor<_Enum_> ee, Class<?> result) {
    assertEquals(type(ee.getExpectedEnumTypeClassName()), result);
  }

  public void assertGetExpectedEnumTypeClassName(AbstractEnumEditor<_Enum_> ee, String result) {
    assertEquals(ee.getClass().getName().substring(0, ee.getClass().getName().lastIndexOf("Editor")), result);
  }

  public void assertGetTags(AbstractEnumEditor<_Enum_> ee, String[] result) {
    _Contract_EnumEditor eeC = (_Contract_EnumEditor)getDirectSuperContracts().get(EnumEditor.class);
    eeC.assertGetTags(ee, result);
    assertArrayEquals(ee.getValuesMap().keySet().toArray(), result);
  }

  public void assertGetValuesMap(AbstractEnumEditor<_Enum_> ee, Map<String, ?> result) {
    assertEquals(EnumHelpers.valuesMap(ee.getEnumType()), result);
  }

  public void assertGetAsText(AbstractEnumEditor<_Enum_> ee, String result) {
    assertEquals(ee.getValue() == null ? null : ee.getValue().name(), result);
  }

  public void assertSetAsText_String_Nominal(AbstractEnumEditor<_Enum_> ee, String tag) {
    _Contract_EnumEditor eeC = (_Contract_EnumEditor)getDirectSuperContracts().get(EnumEditor.class);
    eeC.assertSetAsText_String_Nominal(ee, tag);
    assertEquals(tag == null || tag.equals("") ? null : ee.getValuesMap().get(tag), ee.getValue());
  }

  public void assertSetAsText_String_Exception(AbstractEnumEditor<_Enum_> ee, String tag, IllegalArgumentException exc, Object oldValue) {
    _Contract_EnumEditor eeC = (_Contract_EnumEditor)getDirectSuperContracts().get(EnumEditor.class);
    eeC.assertSetAsText_String_Exception(ee, tag, exc);
    assertNotNull(tag);
    assertFalse("".equals(tag));
    assertNull(ee.getValuesMap().get(tag));
    assertEquals(oldValue, ee.getValue());
  }

}

