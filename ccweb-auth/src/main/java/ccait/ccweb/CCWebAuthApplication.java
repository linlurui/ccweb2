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

        log.info( "---------------------------------------------------------------------------------------" );
        log.info( "ccweb-auth started!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }
}
