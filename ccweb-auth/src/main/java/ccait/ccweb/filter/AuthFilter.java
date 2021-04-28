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

import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.utils.NetworkUtils;
import ccait.ccweb.wrapper.CCWebRequestWrapper;
import entity.query.core.ApplicationConfig;
import entity.tool.util.JsonUtils;
import entity.tool.util.StringUtils;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static ccait.ccweb.utils.StaticVars.LOG_PRE_SUFFIX;

@Order(Ordered.HIGHEST_PRECEDENCE+555)
@Component
@javax.servlet.annotation.WebFilter(urlPatterns = "/login", asyncSupported=true)
public class AuthFilter implements WebFilter, Filter {

    private static final Logger log = LoggerFactory.getLogger( AuthFilter.class );

//    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void init(FilterConfig filterConfig) {
        // TODO filterConfig
    }

    @PostConstruct
    private void init() {
        requestMappingHandlerMapping = new RequestMappingHandlerMapping();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException
    {

        final HttpServletResponse res = (HttpServletResponse)response;
        res.setHeader("Access-Control-Allow-Origin", ApplicationConfig.getInstance().get("${ccweb.auth.allowOrigin}", "*"));
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods",  ApplicationConfig.getInstance().get("${ccweb.auth.allowMethods}", "*"));
        res.setHeader("Access-Control-Max-Age", ApplicationConfig.getInstance().get("${ccweb.auth.allowMethods}", "3600"));
        res.setHeader("Access-Control-Allow-Headers", ApplicationConfig.getInstance().get("${ccweb.auth.allowHeaders}", "*"));
        res.setHeader("Access-Control-Expose-Headers", ApplicationConfig.getInstance().get("${ccweb.auth.exposeHeaders}", "*"));
        res.setContentType("application/json; charset=utf-8");
        res.setCharacterEncoding("UTF-8");

        try
        {
            try {
                log.info(LOG_PRE_SUFFIX + "Status：" + res.getStatus());
                log.info(LOG_PRE_SUFFIX + "Client Ip：" + NetworkUtils.getClientIp((HttpServletRequest) request));
                log.info(LOG_PRE_SUFFIX + "Method：" + ((HttpServletRequest)request).getMethod());
            }
            catch (Exception ex) {

                String message = getErrorMessage(ex);

                log.error( LOG_PRE_SUFFIX + message, ex );
            }

            final long startTime = System.currentTimeMillis();
            final HttpServletRequest req = (HttpServletRequest)request;

            log.info(LOG_PRE_SUFFIX + "Request Url：" + req.getRequestURL());

            if(request instanceof CCWebRequestWrapper) {
                chain.doFilter(request, res);
            }

            else {
                CCWebRequestWrapper requestWrapper = new CCWebRequestWrapper(req);
                chain.doFilter(requestWrapper, res);
            }

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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return requestMappingHandlerMapping.getHandler(exchange).switchIfEmpty(chain.filter(exchange)).flatMap(handler-> {
            // TODO 检查登录权限
            return chain.filter(exchange);
        });
    }
}
