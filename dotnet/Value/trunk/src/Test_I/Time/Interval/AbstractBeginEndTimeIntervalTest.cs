﻿/*<license>
Copyright 2011 - $Date: 2008-11-06 15:27:53 +0100 (Thu, 06 Nov 2008) $ by PeopleWare n.v..

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

#region Using

using System;
using System.Collections.Generic;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;

using PPWCode.Value.I.Time.Interval;

#endregion

namespace PPWCode.Value.Test_I.Time.Interval
{
    [TestClass]
    public class AbstractBeginEndTimeIntervalTest : AbstractTimeIntervalTest
    {
        private class StubAbstractBeginEndTimeInterval : AbstractBeginEndTimeInterval
        {
            public StubAbstractBeginEndTimeInterval(DateTime? begin, DateTime? end)
                : base(begin, end)
            {
                // NOP
            }

            public override ITimeInterval Determinate(DateTime? stubBegin, DateTime? stubEnd)
            {
                return null;
            }
        }

        #region Additional test attributes

        public IList<AbstractBeginEndTimeInterval> AbstractBeginEndSubjects
        {
            get
            {
                return Subjects.Cast<AbstractBeginEndTimeInterval>().ToList();
            }
        }

        [TestInitialize()]
        public override void TestInitialize()
        {
            InitSubjects();
            DateTime now = DateTime.Now;
            AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(now, null);
            AddSubject(subject);
            subject = new StubAbstractBeginEndTimeInterval(null, now);
            AddSubject(subject);
            subject = new StubAbstractBeginEndTimeInterval(now, now);
            AddSubject(subject);
            DateTime b = new DateTime(2000, 1, 1);
            DateTime e = new DateTime(2122, 12, 31);
            subject = new StubAbstractBeginEndTimeInterval(b, e);
            AddSubject(subject);
        }

        #endregion

        [TestMethod]
        public void TestDuration()
        {
            TimeSpan? result = null;
            foreach (AbstractBeginEndTimeInterval subject in AbstractBeginEndSubjects)
            {
                result = subject.Duration;
            }
        }

        [TestMethod]
        public virtual void TestConstructor()
        {
            DateTime past = new DateTime(1995, 4, 24);
            DateTime future = new DateTime(2223, 5, 13);
            DateTime now = DateTime.Now;
            DateTime?[] dates = { null, past, now, future };
            foreach (DateTime? d1 in dates)
            {
                foreach (DateTime? d2 in dates)
                {
                    try
                    {
                        AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(d1, d2);
                    }
                    catch (IllegalTimeIntervalException)
                    {
                        // NOP
                    }
                }
            }
        }

        #region extra constructor tests
        // extra constructor tests to find test failures in above method

        [TestMethod]
        public virtual void TestConstructorNotNullNotNull()
        {
            try
            {
                DateTime past = new DateTime(1995, 4, 24);
                DateTime future = new DateTime(2223, 5, 13);
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(past, future);
            }
            catch (IllegalTimeIntervalException)
            {
                Assert.Fail();
            }
        }

        [TestMethod]
        public virtual void TestConstructorNotNullNotNullZeroDuration()
        {
            try
            {
                DateTime d = new DateTime(1995, 4, 24);
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(d, d);
            }
            catch (IllegalTimeIntervalException)
            {
                Assert.Fail();
            }
        }

        [TestMethod]
        public virtual void TestConstructorNotNullNull()
        {
            try
            {
                DateTime past = new DateTime(1995, 4, 24);
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(past, null);
            }
            catch (IllegalTimeIntervalException)
            {
                Assert.Fail();
            }
        }

        [TestMethod]
        public virtual void TestConstructorNullNotNull()
        {
            try
            {
                DateTime future = new DateTime(2223, 5, 13);
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(null, future);
            }
            catch (IllegalTimeIntervalException)
            {
                Assert.Fail();
            }
        }

        [TestMethod]
        public virtual void TestConstructorNotNullNotNullReversed()
        {
            try
            {
                DateTime past = new DateTime(1995, 4, 24);
                DateTime future = new DateTime(2223, 5, 13);
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(future, past);
                Assert.Fail();
            }
            catch (IllegalTimeIntervalException)
            {
                // NOP
            }
        }

        [TestMethod]
        public virtual void TestConstructorNullNull()
        {
            try
            {
                AbstractBeginEndTimeInterval subject = new StubAbstractBeginEndTimeInterval(null, null);
                Assert.Fail();
            }
            catch (IllegalTimeIntervalException)
            {
                // NOP
            }
        }

        #endregion
    }
}