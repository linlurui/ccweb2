package ccait.ccweb.client;

import ccait.ccweb.entites.QueryInfo;
import ccait.ccweb.entites.SearchInfo;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import entity.query.ColumnInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CCWebAuthFallback implements CCWebAuthClient {

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