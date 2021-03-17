/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb;

import entity.query.core.ApplicationConfig;
import entity.tool.util.StringUtils;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.MalformedURLException;


//@EnableHystrixDashboard
//@EnableDiscoveryClient
//@EnableFeignClients
//@EnableHystrix
//@EnableZuulProxy
//@EnableEurekaClient
@ServletComponentScan
@SpringBootApplication( scanBasePackages = {"ccait.ccweb"}, exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class} )
public class CcwebAppliction  implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger( CcwebAppliction.class );

    private static int useServerPort = 0;
    private static int useServerSSLPort = 0;

    public static void main(String[] args) throws FileNotFoundException, MalformedURLException {
        run(null, args);
    }

    public static void run(Class clazz, String[] args) throws FileNotFoundException {
        if(System.getProperty("server.port")!=null) {
            useServerPort = Integer.parseInt(System.getProperty("server.port"));
        }

        if(System.getProperty("server.ssl.port")!=null) {
            useServerSSLPort = Integer.parseInt(System.getProperty("server.ssl.port"));
        }

        SpringApplication app = new SpringApplication(CcwebAppliction.class);
        if(clazz == null) {
            app.run(args);
        }

        else {
            app.run(clazz, args);
        }
        app.setWebApplicationType(WebApplicationType.SERVLET);

        initLogConfig();
        log.info( "---------------------------------------------------------------------------------------" );
        log.info( "ccweb started!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }

    private static void initLogConfig() throws FileNotFoundException {
//        String path = null;
//        File file = null;
//
//        if(file==null || !file.exists()) {
//            if(StringUtils.isNotEmpty(ApplicationConfig.getInstance().get("${log4j.config.path}"))) {
//                String logConfigPath = System.getProperty("user.dir") + "/" +
//                        ApplicationConfig.getInstance().get("${log4j.config.path}");
//
//                if(StringUtils.isNotEmpty(logConfigPath)) {
//                    file = new File(logConfigPath);
//                }
//            }
//        }
//
//        if(!file.exists()) {
//            try {
//                path = Thread.currentThread().getContextClassLoader()
//                        .getResource(ApplicationConfig.getInstance()
//                                .get("${log4j.config.path}")).toURI().getPath();
//
//                if(StringUtils.isNotEmpty(path)) {
//                    file = new File(path);
//                }
//            } catch (URISyntaxException e) {
//                System.out.println("URISyntaxException message=======>" + e.getMessage());
//            }
//        }
//
//        String property = System.getProperty("catalina.home");
//        if(!file.exists()) {
//            path =property + File.separator + "conf" + File.separator + "log4j2.xml";
//            if(StringUtils.isNotEmpty(path)) {
//                file = new File(path);
//            }
//        }
//
//        if(!file.exists()) {
//            path = property + File.separator + "config" + File.separator + "log4j2.xml";
//            file = new File(path);
//        }
//
//        if(!file.exists()) {
//            path = property + File.separator + "log4j2.xml";
//            file = new File(path);
//        }
//
//        if(!file.exists()) {
//            path = System.getProperty("user.dir") + "/log4j2.xml";
//            file = new File(path);
//        }
//
//        if(!file.exists()) {
//            path = System.getProperty("user.dir") + "/config/log4j2.xml";
//            file = new File(path);
//        }
//
//        if(!file.exists()) {
//            path = System.getProperty("user.dir") + "/conf/log4j2.xml";
//            file = new File(path);
//        }
//
//        if(file.exists() && StringUtils.isNotEmpty(path)) {
//
//            PropertyConfigurator.configure(path);
//
//            if(file.exists()) {
//
//                ConfigurationSource source = new ConfigurationSource(new FileInputStream(path), file);
//
//                if (source != null) {
//                    Configurator.initialize(null, source);
//                }
//            }
//
//            System.out.println("Current log4j path: " + path);
//        }
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        if("true".equals(ApplicationConfig.getInstance().get("${server.ssl.enable}"))) {
            tomcat.addAdditionalTomcatConnectors(createSslConnector()); // 添加http
        }
        return tomcat;
    }

    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

        try {
            File keystore = getKeystore();
            connector.setScheme("https");
            connector.setSecure(true);
            int port = useServerSSLPort;
            if(port == 0) {
                port = Integer.parseInt(ApplicationConfig.getInstance().get("${server.ssl.port}", "8088"));
            }
            connector.setPort(port);
            protocol.setSSLEnabled(true);
            protocol.setKeystoreFile(keystore.getAbsolutePath());
            protocol.setKeystorePass(ApplicationConfig.getInstance().get("${server.ssl.key-store-password}"));
            protocol.setKeyPass(ApplicationConfig.getInstance().get("${server.ssl.key-store-password}"));
        }
        catch (IOException ex) {
            log.error("Access keystore error----->" , ex);
        }

        return connector;
    }

    private File getKeystore() throws IOException {

        String keystorePath = ApplicationConfig.getInstance().get("${server.ssl.key-store}");

        if(StringUtils.isEmpty(keystorePath)) {
            throw new IllegalStateException("keystore path can not be empty!!!");
        }

        //tomcat路径
        String property = System.getProperty("catalina.home");
        String path = property + File.separator + "conf" + File.separator + keystorePath;
        File file = new File(path);

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(property + File.separator + keystorePath);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/conf/" + keystorePath);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/config/" + keystorePath);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/resources/" + keystorePath);
        }

        if(file.exists()) {
            return file;
        }

        return new ClassPathResource(keystorePath).getFile();
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        Integer port = useServerPort;
        if(port == 0) {
            port = ApplicationConfig.getInstance().get("${server.port}", 0);
        }

        if(port > 0) {
            server.setPort(port);
        }
    }
}
