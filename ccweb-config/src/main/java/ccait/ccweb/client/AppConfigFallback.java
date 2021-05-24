package ccait.ccweb.client;

import ccait.ccweb.entites.AppConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
class AppConfigFallback implements AppConfigClient {

    @Override
    public List<AppConfig> getAppConfigs(Properties properties, String applicationName) {
        return new ArrayList<>();
    }
}
