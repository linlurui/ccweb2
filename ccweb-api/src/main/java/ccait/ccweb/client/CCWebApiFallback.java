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
public class CCWebApiFallback implements CCWebApiClient {

    private ResponseData responseData = null;

    @Autowired
    void init() {
        responseData = new ResponseData();
        responseData.setMessage("network error");
        responseData.setStatus(400);
    }

    @Override
    public ResponseData timestamp() {
        return responseData;
    }

    @Override
    public ResponseData doJoinQuery(QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doJoinQueryCount(QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doCreateOrAlterTable(String datasource, String table, List<ColumnInfo> columns) {
        return responseData;
    }

    @Override
    public ResponseData doCreateOrAlterView(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doDropTable(String datasource, String table) {
        return responseData;
    }

    @Override
    public ResponseData doGet(String datasource, String table, String id) {
        return responseData;
    }

    @Override
    public ResponseData doQuery(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doSearch(String datasource, String table, SearchInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doExist(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doCount(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doInsert(String datasource, String table, List<Map<String, Object>> postData) {
        return responseData;
    }

    @Override
    public ResponseData doUpdate(String datasource, String table, String id, Map<String, Object> postData) {
        return responseData;
    }

    @Override
    public ResponseData doQueryUpdate(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doDelete(String datasource, String table, String id) {
        return responseData;
    }

    @Override
    public ResponseData tables(String datasource) {
        return responseData;
    }

    @Override
    public ResponseData columns(String datasource, String table) {
        return responseData;
    }

    @Override
    public ResponseData deleteByIds(String datasource, String table, List<String> idList) {
        return responseData;
    }

    @Override
    public void downloaded(String datasource, String table, String field, String id) {

    }

    @Override
    public void previewed(String datasource, String table, String field, String id) {

    }

    @Override
    public void previewedPage(String datasource, String table, String field, String id, Integer page) {

    }

    @Override
    public ResponseData stream(String datasource, String table, QueryInfo queryInfo) {
        return responseData;
    }

    @Override
    public ResponseData doInsertAndReturnId(String datasource, String table, String field, List<Map<String, Object>> postData) {
        return responseData;
    }

    @Override
    public ResponseData uploaded(String datasource, String table, String field, Map<String, Object> uploadFiles) {
        return responseData;
    }

    @Override
    public void playVideo(String datasource, String table, String field, String id) {

    }

    @Override
    public void doExport(String datasource, String table, QueryInfo queryInfo) {

    }

    @Override
    public void doExport(QueryInfo queryInfo) {

    }

    @Override
    public ResponseData doImportExcel(String datasource, String table, Map<String, Object> uploadFiles) {
        return responseData;
    }

    @Override
    public ResponseData doImportFileToPdf(String datasource, String table, Map<String, Object> uploadFiles) {
        return responseData;
    }

    @Override
    public ResponseData getCurrentUser() {
        return responseData;
    }

    @Override
    public ResponseData userLogin(UserModel user) {
        return responseData;
    }

    @Override
    public ResponseData logout() {
        return responseData;
    }
}
