package ccait.ccweb.client;

import ccait.ccweb.entites.SMTPMessage;
import ccait.ccweb.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailFallback implements MailClient {

    private ResponseData responseData = null;

    @Autowired
    void init() {
        responseData = new ResponseData();
        responseData.setMessage("network error");
        responseData.setStatus(400);
    }

    @Override
    public ResponseData sendTo(Integer userId, SMTPMessage message) {
        return null;
    }

    @Override
    public ResponseData sendForGroup(Integer receivesType, SMTPMessage message) {
        return null;
    }
}