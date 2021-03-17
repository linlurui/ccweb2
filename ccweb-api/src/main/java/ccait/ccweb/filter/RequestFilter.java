/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */


package ccait.ccweb.filter;

import ccait.ccweb.config.LangConfig;
import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.utils.NetworkUtils;
import ccait.ccweb.utils.StaticVars;
import entity.query.Datetime;
import entity.query.core.ApplicationConfig;
import entity.tool.util.JsonUtils;
import entity.tool.util.StringUtils;
import org.apache.http.HttpException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ccait.ccweb.wrapper.CCWebRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static ccait.ccweb.utils.StaticVars.LOG_PRE_SUFFIX;

@Order(-666)
@javax.servlet.annotation.WebFilter(urlPatterns = "/*", asyncSupported=true)
public class RequestFilter implements WebFilter, Filter {

    private static final Logger log = LoggerFactory.getLogger( RequestFilter.class );
    private static final String TRACE_ID = "TRACE_ID";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException
    {

        String traceId = UUID.randomUUID().toString().replace("-","");
        MDC.put(TRACE_ID, traceId);

        final HttpServletRequest req = (HttpServletRequest)request;
        final HttpServletResponse res = (HttpServletResponse)response;

        log.info(LOG_PRE_SUFFIX + "Request Url：" + req.getRequestURL());
        ((HttpServletRequest) request).getSession(); //确保Session可以带入websocket
        CCWebRequestWrapper requestWrapper = new CCWebRequestWrapper(req);
//        ResponseWrapper responseWrapper = new ResponseWrapper(res);

        try
        {
            try {
                log.info(LOG_PRE_SUFFIX + "Status：" + res.getStatus());
                log.info(LOG_PRE_SUFFIX + "Client Ip：" + NetworkUtils.getClientIp(req));
                log.info(LOG_PRE_SUFFIX + "Method：" + req.getMethod());
            }
            catch (Exception ex) {

                String message = getErrorMessage(ex);

                log.error( LOG_PRE_SUFFIX + message, ex );
            }

            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
            res.setHeader("Access-Control-Max-Age", "3600");
            res.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            res.setContentType("application/json; charset=utf-8");
            res.setCharacterEncoding("UTF-8");

            String begin = ApplicationConfig.getInstance().get("${entity.limitTime.begin}", "");
            String end = ApplicationConfig.getInstance().get("${entity.limitTime.end}", "");
            if(StringUtils.isNotEmpty(begin) && StringUtils.isNotEmpty(end) && Datetime.isEffectiveDate(begin, end)) {
                throw new Exception(LangConfig.getInstance().get("is_effective_date"));
            }

            final long startTime = System.currentTimeMillis();
            chain.doFilter(requestWrapper, res);

            final long endTime = System.currentTimeMillis() - startTime;
            log.info(LOG_PRE_SUFFIX + "TimeMillis：" + endTime + "ms");
        }

        catch ( Exception e )
        {
            String message = getErrorMessage(e);

            log.error( LOG_PRE_SUFFIX + message, e );

            ResponseData responseData = new ResponseData();
            responseData.setStatus(-2);
            responseData.setMessage(message);

            if(res.isCommitted()) {
                return;
            }
            res.reset();
            res.setCharacterEncoding("UTF-8");

            if(e instanceof HttpException) {
                res.setStatus(HttpStatus.FORBIDDEN.value());
            }

            else {
                res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            res.getWriter().write(JsonUtils.toJson(responseData));
            res.getWriter().flush();
            res.getWriter().close();
        }
        finally {
            CCApplicationContext.dispose();
        }
    }

    @Override
    public void destroy() {

    }

    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        if(e.getCause() != null) {
            if(e.getCause() instanceof InvocationTargetException && ((InvocationTargetException)e.getCause()).getTargetException() != null) {
                message = ((InvocationTargetException) e.getCause()).getTargetException().getMessage();
            }

            else if(StringUtils.isNotEmpty(e.getCause().getMessage())) {
                message = e.getCause().getMessage();
            }
        }

        return message;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        return null;
    }
}
