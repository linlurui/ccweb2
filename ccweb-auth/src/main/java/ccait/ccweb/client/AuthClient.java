package ccait.ccweb.client;


import ccait.ccweb.config.FeignConfig;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "ccweb-auth", path = "api", fallback = AuthFallback.class, configuration = { FeignConfig.class })
public interface AuthClient {
    @RequestMapping( value = "login", method = RequestMethod.POST  )
    ResponseData userLogin(@RequestBody UserModel user);

    @RequestMapping( value = "logout", method = RequestMethod.GET  )
    ResponseData logout();
}
