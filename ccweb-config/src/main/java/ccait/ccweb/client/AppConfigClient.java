package ccait.ccweb.client;


import ccait.ccweb.config.FeignConfig;
import ccait.ccweb.entites.AppConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Properties;


@FeignClient(name = "ccweb-config", path = "config", fallback = AppConfigFallback.class, configuration = { FeignConfig.class })
public interface AppConfigClient {
    @RequestMapping( value = "list/{applicationName}", method = RequestMethod.POST  )
    List<AppConfig> getAppConfigs(@RequestBody Properties properties, @PathVariable String applicationName);
}
