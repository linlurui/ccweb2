package ccait.ccweb.processor;

import ccait.ccweb.context.ApplicationContext;
import ccait.ccweb.entites.AppConfig;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import entity.query.ColumnInfo;
import entity.query.Queryable;
import entity.query.core.ApplicationConfig;
import entity.query.core.DataSource;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Resource
@Component
public class ApplicationConfigProcessor implements EnvironmentPostProcessor {

    private static final String ENCODING = "UTF-8";
    private static final Logger log = LoggerFactory.getLogger( ApplicationConfigProcessor.class );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        File file = getApplicationConfig(configurableEnvironment, ".yml");
        if(file != null  && file.exists()) {
            setPropertys(configurableEnvironment, loadPropertiesByYaml(file));
            return;
        }

        file = getApplicationConfig(configurableEnvironment, ".yaml");
        if(file != null  && file.exists()) {
            setPropertys(configurableEnvironment, loadPropertiesByYaml(file));
            return;
        }

        file = getApplicationConfig(configurableEnvironment, ".properties");
        if(file != null  && file.exists()) {
            setPropertys(configurableEnvironment, loadProperties(file));
            return;
        }
    }

    public File getApplicationConfig(ConfigurableEnvironment configurableEnvironment, String suffix) {
        //tomcat路径
        String property = System.getProperty("catalina.home");
        String path =property+ File.separator + "conf" + File.separator+"application" + suffix;
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        else {
            file = new File(property+ File.separator + "config" + File.separator+"application" + suffix);
        }

        if (file.exists()) {
            return file;
        }
        else {
            file = new File(property + File.separator + "application" + suffix);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/conf/application" + suffix);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/config/application" + suffix);
        }

        if (file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/application" + suffix);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/resources/application" + suffix);
        }

        if(file.exists()) {
            return file;
        }
        else {
            file = new File(System.getProperty("user.dir") + "/src/main/resources/application" + suffix);
        }
        return file;
    }

    private void setPropertys(ConfigurableEnvironment configurableEnvironment, Properties properties) {
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        fillPropertiesFromDatabase(properties);

        //以外部配置文件为准
        propertySources.addFirst(new PropertiesPropertySource("Config", properties));
        //以application.properties文件为准
        //propertySources.addLast(new PropertiesPropertySource("Config", properties));
    }

    private void fillPropertiesFromDatabase(Properties properties) {
        String configTable = ApplicationConfig.getInstance().get("${entity.app-config.table}", "");
        String applicationName = ApplicationConfig.getInstance().get("${spring.application.name}", "");

        if(StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(configTable)) {
            return;
        }

        try {
            log.info(String.format("Fill properties from database table [%s] for %s...", configTable, applicationName));
            ensureConfigTable(configTable);
            AppConfig appConfigEntity = new AppConfig();
            appConfigEntity.setService(applicationName);
            for(Object key : properties.keySet()) {
                appConfigEntity.setKey(key.toString());
                appConfigEntity.setValue(properties.getProperty(key.toString()));
                if(!appConfigEntity.where("[service]=#{service}").and("[key]=#{key}").exist()) {
                    appConfigEntity.insert();
                }
            }

            List<AppConfig> appConfigs = appConfigEntity.where("[service]=#{service}").query();
            for (AppConfig appConfig : appConfigs) {
                if(appConfig.getKey().startsWith("entity.datasource") ||
                        "entity.account".equalsIgnoreCase(appConfig.getKey()) ||
                        "entity.license".equalsIgnoreCase(appConfig.getKey())) {
                    continue;
                }
                properties.setProperty(appConfig.getKey(), appConfig.getValue());
                ApplicationConfig.getInstance().set(appConfig.getKey(), appConfig.getValue());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void ensureConfigTable(String configTable) throws Exception {
        log.info("ensure config table!!!");
        List<String> tables = null;
        DataSource ds = ApplicationContext.getDefaultDataSource(null);
        if(StringUtils.isEmpty(ds.getId())) {
            return;
        }


        tables = Queryable.getTables(ds.getId());
        if(!tables.stream().anyMatch(a->a.equals(configTable))) {
            List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
            columns.add(new ColumnInfo(){{
                setColumnName("id");
                setIsPrimaryKey(true);
                setIsAutoIncrement(true);
                setCanNotNull(true);
                setMaxLength(32);
                setType(Integer.class);
                setColumnComment("主键");
            }});
            columns.add(new ColumnInfo(){{
                setColumnName("service");
                setCanNotNull(true);
                setMaxLength(64);
                setUnique(true);
                setType(String.class);
            }});
            columns.add(new ColumnInfo(){{
                setColumnName("key");
                setCanNotNull(true);
                setUnique(true);
                setMaxLength(650);
                setType(String.class);
            }});
            columns.add(new ColumnInfo(){{
                setColumnName("value");
                setType(String.class);
                setMaxLength(1024);
            }});

            log.info(ds.getId()+ "." + configTable + " has been creating!!!");
            if(!ApplicationContext.existTable(ds.getId(), configTable)) {
                Queryable.createTable(ds.getId(), configTable, columns);
            }
        }
    }

    private Properties loadProperties(File f) {
        FileSystemResource resource = new FileSystemResource(f);
        try {
            return PropertiesLoaderUtils.loadProperties(resource);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to load local settings from " + f.getAbsolutePath(), ex);
        }
    }

    public Properties loadPropertiesByYaml(File file) {
        final String DOT = ".";
        Properties result = new Properties();
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLParser parser = yamlFactory.createParser(
                    new InputStreamReader(new FileInputStream(file), Charset.forName(ENCODING)));

            String key = "";
            String value = null;
            JsonToken token = parser.nextToken();
            while (token != null) {
                if (JsonToken.START_OBJECT.equals(token)) {
                    // do nothing
                } else if (JsonToken.FIELD_NAME.equals(token)) {
                    if (key.length() > 0) {
                        key = key + DOT;
                    }
                    key = key + parser.getCurrentName();

                    token = parser.nextToken();
                    if (JsonToken.START_OBJECT.equals(token)) {
                        continue;
                    }
                    value = parser.getText();
                    result.setProperty(key, value);

                    int dotOffset = key.lastIndexOf(DOT);
                    if (dotOffset > 0) {
                        key = key.substring(0, dotOffset);
                    }
                    value = null;
                } else if (JsonToken.END_OBJECT.equals(token)) {
                    int dotOffset = key.lastIndexOf(DOT);
                    if (dotOffset > 0) {
                        key = key.substring(0, dotOffset);
                    } else {
                        key = "";
                    }
                }
                token = parser.nextToken();
            }
            parser.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
