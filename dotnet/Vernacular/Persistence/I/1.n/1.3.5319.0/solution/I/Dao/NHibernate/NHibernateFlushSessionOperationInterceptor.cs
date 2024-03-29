﻿/*
 * Copyright 2004 - $Date: 2008-11-15 23:58:07 +0100 (za, 15 nov 2008) $ by PeopleWare n.v..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#region Using

using System;
using System.ServiceModel.Dispatcher;

using PPWCode.Vernacular.Persistence.I.Dao.Wcf.Helpers.GenericInterceptor;

#endregion

namespace PPWCode.Vernacular.Persistence.I.Dao.NHibernate
{
    public class NHibernateFlushSessionOperationInterceptor
        : GenericInvoker
    {
        public NHibernateFlushSessionOperationInterceptor(IOperationInvoker oldInvoker)
            : base(oldInvoker)
        {
        }

        #region Overrides of GenericInvoker

        protected override void PreInvoke(object instance, object[] inputs)
        {
        }

        protected override void PostInvoke(object instance, object returnedValue, object[] outputs, Exception exception)
        {
            NHibernateContextExtension nHibernateContextExtension = NHibernateContext.Current;
            if (nHibernateContextExtension != null
                && nHibernateContextExtension.Session != null)
            {
                if (exception == null)
                {
                    nHibernateContextExtension.Session.Flush();
                }
                nHibernateContextExtension.Session.Dispose();
            }
        }

        #endregion

        public class NHibernateFlushSessionOperationAttribute : OperationInterceptorBehaviorAttribute
        {
            protected override GenericInvoker CreateInvoker(IOperationInvoker oldInvoker)
            {
                return new NHibernateFlushSessionOperationInterceptor(oldInvoker);
            }
        }
    }
}