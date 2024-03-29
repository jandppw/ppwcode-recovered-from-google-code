/*<license>
  Copyright 2008, PeopleWare n.v.
  NO RIGHTS ARE GRANTED FOR THE USE OF THIS SOFTWARE, EXCEPT, IN WRITING,
  TO SELECTED PARTIES.
</license>*/

package org.ppwcode.value_III.time.interval;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ppwcode.value_III.time.TimeHelpers.dayDate;
import static org.ppwcode.value_III.time.TimeHelpers.le;
import static org.ppwcode.value_III.time.TimeHelpers.sameDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AbstractIntradayTimeIntervalTest extends AbstractBeginEndTimeTimeZoneIntervalTest {

  public static class StubAbstractIntradayTimeInterval extends AbstractIntradayTimeInterval {

    public StubAbstractIntradayTimeInterval(Date begin, Date end, TimeZone tz) throws IllegalTimeIntervalException {
      super(begin, end, tz);
    }

    public TimeInterval determinate(Date stubBegin, Date stubEnd) throws IllegalTimeIntervalException {
      return null;
    }

  }


  @SuppressWarnings("unchecked")
  @Override
  public List<? extends AbstractIntradayTimeInterval> subjects() {
    return (List<? extends AbstractIntradayTimeInterval>)$subjects;
  }

  @Override
  @Before
  public void setUp() throws IllegalTimeIntervalException {
    List<AbstractIntradayTimeInterval> s = new ArrayList<AbstractIntradayTimeInterval>();
    final GregorianCalendar gcB = new GregorianCalendar(2000, 0, 1, 14, 22, 2);
    final GregorianCalendar gcE = new GregorianCalendar(2000, 0, 1, 22, 34, 12);
    final Date now = new Date();
    final Date b = gcB.getTime();
    final Date e = gcE.getTime();
    for (TimeZone tz : TIMEZONES) {
      AbstractIntradayTimeInterval subject = new StubAbstractIntradayTimeInterval(now, null, tz);
      s.add(subject);
      subject = new StubAbstractIntradayTimeInterval(null, now, tz);
      s.add(subject);
      subject = new StubAbstractIntradayTimeInterval(now, now, tz);
      s.add(subject);
      if (sameDay(b, e, tz)) {
        subject = new StubAbstractIntradayTimeInterval(b, e, tz);
        s.add(subject);
      }
    }
    $subjects = s;
  }

  protected Date[] $dates;

  @Before
  public void initDates() {
    GregorianCalendar past1 = new GregorianCalendar(1995, 3, 24, 14, 55, 34);
    GregorianCalendar past2 = new GregorianCalendar(1995, 3, 24, 16, 3, 12);
    Date now = new Date();
    GregorianCalendar future1 = new GregorianCalendar(2223, 4, 13, 3, 45, 45);
    GregorianCalendar future2 = new GregorianCalendar(2223, 4, 13, 6, 32, 44);
    $dates = new Date[] {null, past1.getTime(), past2.getTime(), now, future1.getTime(), future2.getTime()};
  }

  @After
  public void deInitDates() {
    $dates = null;
  }

  @Override
  protected void assertInvariants(TimeInterval subject) {
    super.assertInvariants(subject);
    assertTrue(subject.getBegin() == null || subject.getEnd() == null ||
               sameDay(subject.getBegin(), subject.getEnd(), ((AbstractIntradayTimeInterval)subject).getTimeZone()));
  }

  @Override
  @Test
  public void testCONSTRUCTOR() {
    for (Date d1 : $dates) {
      for (Date d2 : $dates) {
        for (TimeZone tz : TIMEZONES) {
          try {
            AbstractIntradayTimeInterval subject = new StubAbstractIntradayTimeInterval(d1, d2, tz);
            assertEquals(d1, subject.getBegin());
            assertEquals(d2, subject.getEnd());
            assertEquals(tz, subject.getTimeZone());
            assertInvariants(subject);
          }
          catch (IllegalTimeIntervalException exc) {
            assertTrue(! le(d1, d2) || ! sameDay(d1, d2, tz) || (d1 == null && d2 == null));
          }
        }
      }
    }
  }

//  @Test
//  public void testDeterminate() {
//    for (AbstractIntradayTimeInterval subject : subjects()) {
//      for (Date d1 : $dates) {
//        for (Date d2 : $dates) {
//          try {
//            AbstractIntradayTimeInterval result = subject.determinate(d1, d2);
//            TIMEINTERVAL_CONTRACT.assertDeterminate(subject, d1, d2, result);
//          }
//          catch (IllegalTimeIntervalException exc) {
//            assertTrue(! le(subject.determinateBegin(d1), subject.determinateEnd(d2)) ||
//                       ! sameDay(subject.determinateBegin(d1), subject.determinateEnd(d2), subject.getTimeZone()));
//          }
//          assertInvariants(subject);
//        }
//      }
//    }
//  }

  @Test
  public void testGetDay() {
    for (AbstractIntradayTimeInterval subject : subjects()) {
      Date result = subject.getDay();
      Date expected = subject.getBegin() != null ? dayDate(subject.getBegin(), subject.getTimeZone()) :
                        (subject.getEnd() != null ? dayDate(subject.getEnd(), subject.getTimeZone()) :
                          null);
      assertEquals(expected, result);
      assertInvariants(subject);
    }
  }

}

