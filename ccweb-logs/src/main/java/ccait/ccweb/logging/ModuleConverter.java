/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ModuleConverter extends ClassicConverter {

    private static final int MAX_LENGTH = 20;

    @Override
    public String convert(ILoggingEvent event) {
        if (event.getLoggerName().length() > MAX_LENGTH) {
            return "";
        } else {
            return event.getLoggerName();
        }
    }
}