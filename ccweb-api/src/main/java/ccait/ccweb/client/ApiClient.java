package ccait.ccweb.client;

import ccait.ccweb.config.FeignConfig;
import ccait.ccweb.entites.QueryInfo;
import ccait.ccweb.entites.SearchInfo;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import entity.query.ColumnInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ccweb-api", path = "api/{datasource}", fallback = ApiFallback.class, configuration = { FeignConfig.class })
public interface ApiClient {
    @RequestMapping( value = "timestamp")
    ResponseData timestamp();

    @RequestMapping( value = "join", method = RequestMethod.POST )
    ResponseData doJoinQuery(@RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "join/count", method = RequestMethod.POST )
    ResponseData doJoinQueryCount(@RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/build/table", method = {RequestMethod.POST}  )
    ResponseData doCreateOrAlterTable(@PathVariable String datasource, @PathVariable String table, @RequestBody List<ColumnInfo> columns);

    @RequestMapping( value = "{table}/build/view", method = {RequestMethod.POST}  )
    ResponseData doCreateOrAlterView(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/drop/table", method = {RequestMethod.POST}  )
    ResponseData doDropTable(@PathVariable String datasource, @PathVariable String table);

    @RequestMapping( value = "{table}/{id}", method = RequestMethod.GET  )
    ResponseData doGet(@PathVariable String datasource, @PathVariable String table, @PathVariable String id);

    @RequestMapping( value = "{table}", method = RequestMethod.POST  )
    ResponseData doQuery(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "search/{table}", method = RequestMethod.POST  )
    ResponseData doSearch(@PathVariable String datasource, @PathVariable String table, @RequestBody SearchInfo queryInfo);

    @RequestMapping( value = "{table}/exist", method = RequestMethod.POST  )
    ResponseData doExist(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/count", method = RequestMethod.POST  )
    ResponseData doCount(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}", method = RequestMethod.PUT  )
    ResponseData doInsert(@PathVariable String datasource, @PathVariable String table, @RequestBody List<Map<String, Object>> postData);

    @RequestMapping( value = "{table}/{id}", method = RequestMethod.PUT  )
    ResponseData doUpdate(@PathVariable String datasource, @PathVariable String table, @PathVariable String id, @RequestBody Map<String, Object> postData);

    @RequestMapping( value = "{table}/update", method = RequestMethod.POST )
    ResponseData doQueryUpdate(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/{id}", method = RequestMethod.DELETE  )
    ResponseData doDelete(@PathVariable String datasource, @PathVariable String table, @PathVariable String id);

    @RequestMapping( value = "tables", method = RequestMethod.POST  )
    ResponseData tables(@PathVariable String datasource);

    @RequestMapping( value = "{table}/columns", method = RequestMethod.POST  )
    ResponseData columns(@PathVariable String datasource, @PathVariable String table);

    @RequestMapping( value = "{table}/delete", method = RequestMethod.POST  )
    ResponseData deleteByIds(@PathVariable String datasource, @PathVariable String table, @RequestBody List<String> idList);

    @RequestMapping( value = "download/{table}/{field}/{id}", method = RequestMethod.GET  )
    void downloaded(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @PathVariable String id);

    @RequestMapping( value = "preview/{table}/{field}/{id}", method = RequestMethod.GET  )
    void previewed(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @PathVariable String id);

    @RequestMapping( value = "preview/{table}/{field}/{id}/{page}", method = RequestMethod.GET  )
    void previewedPage(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @PathVariable String id, @PathVariable Integer page);

    @RequestMapping( value = "{table}/stream", method = RequestMethod.POST, produces= MediaType.APPLICATION_STREAM_JSON_VALUE  )
    ResponseData stream(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/max/{field}", method = RequestMethod.PUT )
    ResponseData doInsertAndReturnId(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @RequestBody List<Map<String, Object>> postData);

    @RequestMapping( value = "{table}/{field}/upload", method = RequestMethod.POST )
    ResponseData uploaded(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @RequestBody Map<String, Object> uploadFiles);

    @RequestMapping( value = "play/{table}/{field}/{id}", method = RequestMethod.GET )
    void playVideo(@PathVariable String datasource, @PathVariable String table, @PathVariable String field, @PathVariable String id);

    @RequestMapping( value = "{table}/export", method = RequestMethod.POST )
    void doExport(@PathVariable String datasource, @PathVariable String table, @RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "export/join", method = RequestMethod.POST )
    void doExport(@RequestBody QueryInfo queryInfo);

    @RequestMapping( value = "{table}/import", method = {RequestMethod.POST, RequestMethod.PUT} )
    ResponseData doImportExcel(@PathVariable String datasource, @PathVariable String table, @RequestBody Map<String, Object> uploadFiles);

    @RequestMapping( value = "{table}/import/to/pdf", method = {RequestMethod.POST, RequestMethod.PUT} )
    ResponseData doImportFileToPdf(@PathVariable String datasource, @PathVariable String table, @RequestBody Map<String, Object> uploadFiles);

    @RequestMapping(value = "session/user", method = RequestMethod.GET )
    ResponseData getCurrentUser();

    @RequestMapping( value = "login", method = RequestMethod.POST  )
    ResponseData userLogin(@RequestBody UserModel user);

    @RequestMapping( value = "logout", method = RequestMethod.GET  )
    ResponseData logout();
}
