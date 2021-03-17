package ccait.ccweb;

import entity.query.core.ApplicationConfig;
import entity.tool.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@SpringBootApplication( exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class} )
public class CCWebAuthApplication {

    private static final Logger log = LoggerFactory.getLogger( CCWebAuthApplication.class );

    public static void main(String[] args) throws FileNotFoundException, MalformedURLException {
        run(null, args);
    }

    public static void run(Class clazz, String[] args) throws FileNotFoundException, MalformedURLException {
        SpringApplication app = new SpringApplication(CCWebAuthApplication.class);
        if(clazz == null) {
            app.run(args);
        }

        else {
            app.run(clazz, args);
        }

//        String path = null;
//        File file = null;
//
//        if(file==null || !file.exists()) {
//            if(StringUtils.isNotEmpty(ApplicationConfig.getInstance().get("${log4j.config.path}"))) {
//                String logConfigPath = System.getProperty("user.dir") + "/" +
//                        ApplicationConfig.getInstance().get("${log4j.config.path}");
//
//                file = new File(logConfigPath);
//            }
//        }
//
//        if(!file.exists()) {
//            try {
//                path = Thread.currentThread().getContextClassLoader()
//                        .getResource(ApplicationConfig.getInstance()
//                                .get("${log4j.config.path}")).toURI().getPath();
//
//                file = new File(path);
//            } catch (URISyntaxException e) {
//                System.out.println("URISyntaxException message=======>" + e.getMessage());
//            }
//        }
//
//        if(!file.exists()) {
//            String property = System.getProperty("catalina.home");
//            path =property+ File.separator + "conf" + File.separator + "log4j2.xml";
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

        log.info( "---------------------------------------------------------------------------------------" );
        log.info( "ccweb-auth started!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }
}
