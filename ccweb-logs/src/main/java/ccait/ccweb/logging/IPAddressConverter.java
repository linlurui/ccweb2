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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressConverter extends ClassicConverter {

    private static String ipAddress;
    private static final Logger log = LoggerFactory.getLogger(IPAddressConverter.class);

    static {
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("fetch localhost host address failed", e);
            ipAddress = "UNKNOWN";
        }
    }

    @Override
    public String convert(ILoggingEvent event) {
        return ipAddress;
    }
}