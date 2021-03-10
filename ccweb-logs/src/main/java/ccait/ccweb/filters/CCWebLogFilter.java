/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.filters;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

@Plugin(name = "CCWebLogFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class CCWebLogFilter extends AbstractFilter {

    private final String threadName;

    private CCWebLogFilter(String threadName, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.threadName = threadName;
    }

    public Result filter(Logger logger, Level level, Marker marker, String msg, Object[] params) {
        return filter(Thread.currentThread());
    }

    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return filter(Thread.currentThread());
    }

    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filter(Thread.currentThread());
    }

    @Override
    public Result filter(LogEvent event) {
        return filter(Thread.currentThread());
    }

    public Result filter(Thread thread) {
        if (thread.getName() == null) return onMismatch;
        return thread.getName().indexOf(threadName) >= 0 ? onMatch : onMismatch;
    }

    @Override
    public String toString() {
        return threadName.toString();
    }

    @PluginFactory
    public static CCWebLogFilter createFilter(@PluginAttribute("threadName") String threadName,
                                            @PluginAttribute("onMatch") final Result match,
                                            @PluginAttribute("onMismatch") final Result mismatch) {
        return new CCWebLogFilter(threadName, match, mismatch);
    }
}