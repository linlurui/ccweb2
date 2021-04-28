package ccait.ccweb.client;


import ccait.ccweb.entites.SMTPMessage;
import ccait.ccweb.enums.ReceivesType;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "ccweb-mail", path = "api/mail", fallback = CCWebMailFallback.class)
public interface CCWebMailClient {
    @RequestMapping( value = "{userId}", method = RequestMethod.POST )
    ResponseData sendTo(@PathVariable Integer userId, @RequestBody SMTPMessage message);

    @RequestMapping( value = "send/{type}", method = RequestMethod.POST )
    ResponseData sendForGroup(@PathVariable Integer receivesType, @RequestBody SMTPMessage message);
}
