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

package org.ppwcode.vernacular.exception_II;


import junit.framework.TestCase;


public class TestProgrammingError extends TestCase {

  public final static String TEST_MESSAGE = "This is a test message";

  public final static String EMPTY = "";

  public final static Throwable TEST_THROWABLE = new NullPointerException();



  private void testInvariants(ProgrammingError subject) {
    assertNotNull(subject.getMessage());
  }


  /*<method signature="ProgrammingError(String, Throwable)">*/
  //-----------------------------------------------------------------------

  public void testProgrammingErrorStringThrowable1() {
    testProgrammingErrorStringThrowable(TEST_MESSAGE, TEST_THROWABLE);
  }

  public void testProgrammingErrorStringThrowable2() {
    testProgrammingErrorStringThrowable(TEST_MESSAGE, null);
  }

  public void testProgrammingErrorStringThrowable3() {
    testProgrammingErrorStringThrowable(EMPTY, TEST_THROWABLE);
  }

  public void testProgrammingErrorStringThrowable4() {
    testProgrammingErrorStringThrowable(EMPTY, null);
  }

  public void testProgrammingErrorStringThrowable5() {
    testProgrammingErrorStringThrowable(null, TEST_THROWABLE);
  }

  public void testProgrammingErrorStringThrowable6() {
    testProgrammingErrorStringThrowable(null, null);
  }

  public void testProgrammingErrorStringThrowable(String message, Throwable t) {
    ProgrammingError subject = new ProgrammingError(message, t);
    testInvariants(subject);
    assertEquals(ProgrammingError.defaultMessage(message, t), subject.getMessage());
    assertEquals(t, subject.getCause());
  }

  /*</method>*/



  /*<method signature="ProgrammingError(Throwable)">*/
  //-----------------------------------------------------------------------

  public void testProgrammingErrorThrowable1() {
    testProgrammingErrorThrowable(TEST_THROWABLE);
  }

  public void testProgrammingErrorThrowable2() {
    testProgrammingErrorThrowable(null);
  }

  private void testProgrammingErrorThrowable(Throwable t) {
    ProgrammingError subject = new ProgrammingError(t);
    testInvariants(subject);
    assertEquals(ProgrammingError.defaultMessage(null, t), subject.getMessage());
    assertEquals(t, subject.getCause());
  }

  /*</method>*/



  /*<method signature="ProgrammingError(String)">*/
  //-----------------------------------------------------------------------

  public void testProgrammingErrorString1() {
    testProgrammingErrorString(TEST_MESSAGE);
  }

  public void testProgrammingErrorString2() {
    testProgrammingErrorString(EMPTY);
  }

  public void testProgrammingErrorString3() {
    testProgrammingErrorString(null);
  }

  private void testProgrammingErrorString(String message) {
    ProgrammingError subject = new ProgrammingError(message);
    testInvariants(subject);
    assertEquals(ProgrammingError.defaultMessage(message, null), subject.getMessage());
    assertEquals(null, subject.getCause());
  }

  /*</method>*/



  /*<method signature="defaultMessage(String, Throwable)">*/
  //-----------------------------------------------------------------------

  public void testDefaultMessage1() {
    testDefaultMessage(null, null);
  }

  public void testDefaultMessage2() {
    testDefaultMessage(TEST_MESSAGE, null);
  }

  public void testDefaultMessage3() {
    testDefaultMessage(null, TEST_THROWABLE);
  }

  public void testDefaultMessage4() {
    testDefaultMessage(TEST_MESSAGE, TEST_THROWABLE);
  }

  private void testDefaultMessage(String message, Throwable t) {
    String expected = (message != null) ?
                        message :
                        ((t != null) ?
                           ProgrammingError.UNEXPECTED_MESSAGE :
                           ProgrammingError.COULD_NOT_CONTINUE_MESSAGE);
    String result = ProgrammingError.defaultMessage(message, t);
    assertEquals(expected, result);
  }

  /*</method>*/

}

