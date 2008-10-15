/*<license>
Copyright 2004 - $Date: 2008-10-05 20:33:16 +0200 (Sun, 05 Oct 2008) $ by PeopleWare n.v..

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

package org.ppwcode.util.reflect_I.serialization.teststubs;

import org.ppwcode.util.reflect_I.serialization.DoNotSerialize;


public class ReplacementSerializableSubSubStub extends ReplacementSerializableSubStub {

  public final String getProperty1TT() {
    return $property1;
  }

  public final void setProperty1TT(String property) {
    $property1 = property;
  }

  @DoNotSerialize
  private String $property1 = DEFAULT_PROPERTY_1_TT_VALUE;




  public final Delegate getDelegate() {
    return $delegate;
  }

  public final void setDelegate(Delegate delegate) {
    $delegate = delegate;
  }

  private Delegate $delegate;


  public final static String DEFAULT_PROPERTY_1_TT_VALUE = "DEFAULT_PROPERTY_1_TT_VALUE";



}

