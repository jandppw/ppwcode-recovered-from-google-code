﻿
using PPWCode.Util.OddsAndEnds.I.Extensions;

using Spring.Context.Support;

namespace PPWCode.Util.OddsAndEnds.I.DateTimeProvider
{
    public static class DateTimeProviderService
    {
        private static IDateTimeProvider CreateInstance()
        {
            return ContextRegistry
                .GetContext()
                .GetObject<IDateTimeProvider>("DateTimeProviderFactory");
        }
    }
}
