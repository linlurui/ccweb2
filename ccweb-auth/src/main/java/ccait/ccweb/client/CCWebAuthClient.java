package ccait.ccweb.client;


import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "ccweb-auth", path = "api", fallback = CCWebAuthFallback.class)
public interface CCWebAuthClient {
    @RequestMapping( value = "login", method = RequestMethod.POST  )
    public ResponseData userLogin(@RequestBody UserModel user);

    @RequestMapping( value = "logout", method = RequestMethod.GET  )
    public ResponseData logout();
}
