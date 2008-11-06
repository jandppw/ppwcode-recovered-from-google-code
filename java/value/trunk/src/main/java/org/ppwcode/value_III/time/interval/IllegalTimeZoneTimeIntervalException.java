/*<license>
Copyright 2004 - $Date: 2008-10-24 20:52:27 +0200 (Fri, 24 Oct 2008) $ by PeopleWare n.v..

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

package org.ppwcode.value_III.time.interval;


import static org.ppwcode.metainfo_I.License.Type.APACHE_V2;
import static org.ppwcode.util.reflect_I.CloneHelpers.klone;

import java.util.Date;
import java.util.TimeZone;

import org.ppwcode.metainfo_I.Copyright;
import org.ppwcode.metainfo_I.License;
import org.ppwcode.metainfo_I.vcs.SvnInfo;
import org.toryt.annotations_I.Basic;
import org.toryt.annotations_I.Expression;
import org.toryt.annotations_I.MethodContract;


/**
 * Cannot create a new {@link TimeInterval} with the given parameters.
 *
 * @author Jan Dockx
 * @author PeopleWare n.v.
 */
@Copyright("2008 - $Date: 2008-10-24 20:52:27 +0200 (Fri, 24 Oct 2008) $, PeopleWare n.v.")
@License(APACHE_V2)
@SvnInfo(revision = "$Revision: 3289 $",
         date     = "$Date: 2008-10-24 20:52:27 +0200 (Fri, 24 Oct 2008) $")
public class IllegalTimeZoneTimeIntervalException extends IllegalTimeIntervalException {


  @MethodContract(post = {
    @Expression("begin == _begin"),
    @Expression("end == _end"),
    @Expression("timeZone == _timeZone"),
    @Expression("message == _messageKey"),
    @Expression("cause == null")
  })
  public IllegalTimeZoneTimeIntervalException(Date begin, Date end, TimeZone timeZone, String messageKey) {
    super(begin, end, messageKey);
    $timeZone = klone(timeZone);
  }



  /*<property name="time zone">*/
  //------------------------------------------------------------------

  @Basic
  public final TimeZone getTimeZone() {
    return klone($timeZone);
  }

  private TimeZone $timeZone;

  /*</property>*/

}

