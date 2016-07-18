using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited
{
    public interface SdLogListener {
        void run(SdSource source, String tag, String msg, Exception tr);
    }
}
