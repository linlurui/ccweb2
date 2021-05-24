package ccait.ccweb.client;

import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthFallback implements AuthClient {

    private ResponseData responseData = null;

    @Autowired
    void init() {
        responseData = new ResponseData();
        responseData.setMessage("network error");
        responseData.setStatus(400);
    }

    @Override
    public ResponseData userLogin(UserModel user) {
        return null;
    }

    @Override
    public ResponseData logout() {
        return null;
    }
}