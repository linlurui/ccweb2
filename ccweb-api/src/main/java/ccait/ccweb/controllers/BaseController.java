/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */


package ccait.ccweb.controllers;


import ccait.ccweb.abstracts.AbstractWebController;
import ccait.ccweb.config.LangConfig;
import ccait.ccweb.context.*;
import ccait.ccweb.dynamic.DynamicClassBuilder;
import ccait.ccweb.entites.*;
import ccait.ccweb.enums.*;
import ccait.ccweb.excel.Excel;
import ccait.ccweb.excel.Excel2Pdf;
import ccait.ccweb.excel.ExcelObject;
import ccait.ccweb.handler.NonStaticResourceHttpRequestHandler;
import ccait.ccweb.listener.ExcelListener;
import ccait.ccweb.model.*;
import ccait.ccweb.pdf.PdfFile;
import ccait.ccweb.pdf.PdfFileObject;
import ccait.ccweb.pdf.PdfSplitLoader;
import ccait.ccweb.ppt.PPTTool;
import ccait.ccweb.utils.*;
import ccait.ccweb.word.Word2Html;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import entity.query.*;
import entity.query.core.ApplicationConfig;
import entity.tool.util.*;
import org.apache.http.client.HttpResponseException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import static ccait.ccweb.utils.StaticVars.*;

public abstract class BaseController extends AbstractWebController {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);


    @Autowired
    private NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    public BaseController() {
        RMessage = new ResponseData<Object>();
    }

    protected void download(String table, String field, String id) throws Exception {
        ccait.ccweb.model.DownloadData downloadData = new BaseController.DownloadData(CCEntityContext.getCurrentDatasourceId(), table, field, id).invoke();

        CCTriggerContext.exec(table, EventType.Download, downloadData, request);

        byte[] buffer = preDownloadProcess(downloadData, downloadData.getMediaType());

        download(downloadData.getFilename(), downloadData.getMimeType(), buffer);
    }

    protected void download(String filename, String mimeType, byte[] data) throws IOException {

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                URLEncoder.encode(filename, "UTF-8") );
        response.setHeader(HttpHeaders.CONTENT_TYPE, mimeType);

        ServletOutputStream output = response.getOutputStream();
        output.write(data);
    }

    protected void export(String filename, List data, QueryInfo queryInfo) throws IOException {

        if(getLoginUser() == null) {
            throw new IOException("login please!!!");
        }

        if(data.size() < 1) {
            throw new IOException("can not find data!!!");
        }

        filename = filename + ExcelTypeEnum.XLSX.getValue();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                URLEncoder.encode(filename, "UTF-8") );
        response.setHeader(HttpHeaders.CONTENT_TYPE, UploadUtils.getMIMEType("xlsx"));

        EasyExcel.write(response.getOutputStream()).sheet().head(data.get(0).getClass()).doWrite(data);
    }

    protected Mono downloadAs(String table, String field, String id) throws Exception {
        ccait.ccweb.model.DownloadData downloadData = new DownloadData(CCEntityContext.getCurrentDatasourceId(), table, field, id).invoke();

        CCTriggerContext.exec(table, EventType.Download, downloadData, request);

        byte[] buffer = preDownloadProcess(downloadData, downloadData.getMediaType());

        return downloadAs(downloadData.getFilename(), buffer);
    }

    protected Mono downloadAs(String filename, byte[] data) throws IOException {

        ByteArrayResource resource = new ByteArrayResource(data);

        return ServerResponse.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"))
                .contentType(new MediaType("application", "force-download"))
                .body(BodyInserters.fromResource(resource)).switchIfEmpty(Mono.empty());
    }

    protected Mono exportAs(String filename, List data, QueryInfo queryInfo) throws IOException, SQLException {

        List<ColumnInfo> columns = DynamicClassBuilder.getColumnInfosBySelectList(queryInfo.getSelectList());

        Object entity = DynamicClassBuilder.create(CCEntityContext.getCurrentTable(), columns);

        filename = filename + ExcelTypeEnum.XLSX.getValue();

        response.setHeader(HttpHeaders.CONTENT_TYPE, UploadUtils.getMIMEType("xlsx"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        EasyExcel.write(bos).sheet().head(entity.getClass()).doWrite(data);

        ByteArrayResource resource = new ByteArrayResource(bos.toByteArray());

        return ServerResponse.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"))
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(BodyInserters.fromResource(resource)).switchIfEmpty(Mono.empty());
    }

    protected void preview(String table, String field, String id, Integer page) throws Exception {
        ccait.ccweb.model.DownloadData downloadData = new DownloadData(CCEntityContext.getCurrentDatasourceId(), table, field, id, page).invoke();

        CCTriggerContext.exec(table, EventType.PreviewDoc, downloadData, request);

        byte[] buffer = preDownloadProcess(downloadData, downloadData.getMediaType());

        preview(downloadData.getMimeType(), buffer);
    }

    protected void preview(String mimeType, byte[] data) throws IOException {

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;" );
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
        response.setHeader(HttpHeaders.CONTENT_TYPE, mimeType);
        ServletOutputStream output = response.getOutputStream();
        output.write(data);
    }

    protected Mono previewAs(String table, String field, String id, Integer page) throws Exception {
        ccait.ccweb.model.DownloadData downloadData = new DownloadData(CCEntityContext.getCurrentDatasourceId(), table, field, id, page).invoke();
        if(downloadData.getMimeType().indexOf("image") != 0) {
            throw new  Exception(LangConfig.getInstance().get("not_support_file_format"));
        }

        CCTriggerContext.exec(table, EventType.PreviewDoc, downloadData, request);

        byte[] buffer = preDownloadProcess(downloadData, downloadData.getMediaType());

        return previewAs(downloadData.getMimeType(), buffer);
    }

    protected Mono previewAs(String mimeType, byte[] data) throws IOException {

        ByteArrayResource resource = new ByteArrayResource(data);

        return ServerResponse.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline;")
            .contentType(MediaType.valueOf(mimeType)).contentLength(data.length)
            .body(BodyInserters.fromDataBuffers(Mono.create(r -> {
                DataBuffer buf = new DefaultDataBufferFactory().wrap(data);
                r.success(buf);
                return;
            })));
    }

    private byte[] preDownloadProcess(ccait.ccweb.model.DownloadData downloadData, MediaType mediaType) throws Exception {

        if(downloadData.getBuffer() == null) {
            return null;
        }

        if(mediaType.getType().equalsIgnoreCase("image")) {
            BufferedImage image = ImageUtils.getImage(downloadData.getBuffer());

            if(downloadData.isPreview()) {
                image = previewProcess(image);
            }

            if(StringUtils.isNotEmpty(watermark)) {
                image = ImageUtils.watermark(image, watermark, new Color(41, 35, 255, 33), new Font("微软雅黑", Font.PLAIN, 35));
            }
            return ImageUtils.toBytes(image, downloadData.getExtension());
        }

        if(downloadData.isPreview()) {
            if (mediaType.getType().equalsIgnoreCase("video")) {

                downloadData.setMimeType("image/jpeg");
                byte[] bytes = VideoUtils.getThumbnail(new File(downloadData.getPath()), scalRatio);
                downloadData.cleanTempFile();

                return bytes;
            }

            String extesion = UploadUtils.getExtesion(mediaType.toString());
            if (extesion.equalsIgnoreCase("ppt") ||
                    extesion.equalsIgnoreCase("pptx")) {

                downloadData.setMimeType("image/jpeg");
                try {
                    BufferedImage image = PPTTool.getPageImageByPPT(downloadData.getBuffer(), downloadData.getPage(), extesion);
                    if (image == null) {
                        return null;
                    }
                    byte[] bytes = ImageUtils.toBytes(image);

                    return bytes;
                }
                catch (IOException e) {
                    log.error(e.getMessage(), e);
                    String msg = LangConfig.getInstance().get(e.getMessage());
                    if(StringUtils.isNotEmpty(msg)) {
                        throw new Exception(msg);
                    }
                    throw e;
                }
            }

            if (extesion.equalsIgnoreCase("doc")) {

                downloadData.setMimeType("text/html");
                String content = Word2Html.convertDoc2Html(downloadData.getBuffer());
                if (content == null) {
                    return null;
                }
                byte[] bytes = content.getBytes();

                return bytes;
            }

            if (extesion.equalsIgnoreCase("docx")) {
                downloadData.setMimeType("text/html");
                String content = Word2Html.convertDocx2Html(downloadData.getBuffer());
                if (content == null) {
                    return null;
                }
                byte[] bytes = content.getBytes();

                return bytes;
            }

            if (extesion.equalsIgnoreCase("xls") ||
                    extesion.equalsIgnoreCase("xlsx")) {

                downloadData.setMimeType("application/pdf");
                byte[] bytes = Excel.toPdfBytes(downloadData.getBuffer());

                return bytes;
            }
        }

        return downloadData.getBuffer();
    }

    private BufferedImage previewProcess(BufferedImage image) throws IOException {

        if (scalRatio > 0 || fixedWidth > 0) {

            if (scalRatio > 0) {
                image = ImageUtils.zoomImage(image, scalRatio);
            }

            if (fixedWidth > 0) {
                image = ImageUtils.resizeImage(image, fixedWidth);
            }
        }

        return image;
    }

    protected Map<String, Object> upload(String table, String field, Map<String, Object> uploadFiles) throws Exception {

        Map<String, Object> result = new HashMap<String, Object>();
        if(uploadFiles == null) {
            throw new IOException(LangConfig.getInstance().get("upload_empty"));
        }

        if(getLoginUser() == null) {
            throw new HttpResponseException(HttpStatus.UNAUTHORIZED.value(), LangConfig.getInstance().get("login_please"));
        }

        List<ColumnInfo> columns = Queryable.getColumns(CCEntityContext.getCurrentDatasourceId(), table);
        if(!columns.stream().filter(a->a.getColumnName().equals(field)).findAny().isPresent()) {
            throw new IOException(LangConfig.getInstance().get("invalid_table_or_field"));
        }

        String currentDatasource = "default";
        if(CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE) != null) {
            currentDatasource = CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE).toString();
        }

        Map<String, Object> configMap = ApplicationConfig.getInstance().getMap(String.format("ccweb.upload.%s.%s.%s", currentDatasource, table, field));
        if(configMap == null || configMap.size() < 1) {
            configMap = new HashMap<>();
        }

        String mimeTypes = ApplicationConfig.getInstance().get("${ccweb.upload.mimeTypes}", "");
        if(!configMap.containsKey("mimeType") && StringUtils.isNotEmpty(mimeTypes)) {
            configMap.put("mimeType", mimeTypes);
        }

        String maxSize = ApplicationConfig.getInstance().get("${ccweb.upload.maxSize}", "");
        if(!configMap.containsKey("maxSize") && StringUtils.isNotEmpty(maxSize)) {
            configMap.put("maxSize", maxSize);
        }

        String basePath = ApplicationConfig.getInstance().get("${ccweb.upload.basePath}", "");
        if(configMap.containsKey("path") && configMap.get("path")!=null) {
            basePath = basePath + configMap.get("path").toString();
        }

        if(configMap.size() < 1) {
            throw new IOException(String.format("can not find upload config for %s.%s!!!", table, field));
        }

        if(StringUtils.isEmpty(basePath)) {
            throw new IOException("can not find path for upload config!!!");
        }

        for(Map.Entry<String, Object> fileEntry : uploadFiles.entrySet()) {

            boolean isFile = fileEntry.getValue() instanceof UploadFileInfo;
            if(!isFile) { //没有文件名的不是文件
                continue;
            }

            UploadFileInfo uploadFileInfo = (UploadFileInfo) fileEntry.getValue();
            String filename = uploadFileInfo.getFilename();
            String[] arr = filename.split("\\.");
            String extName = arr[arr.length - 1].toLowerCase();

            String mimeType = uploadFileInfo.getContentType();

            if(StringUtils.isEmpty(mimeType)) {
                mimeType = UploadUtils.getMIMEType(extName);
            }

            if(StringUtils.isEmpty(mimeType)) {
                continue;
            }

            if (configMap.get("mimeType") != null) {
                List<String> typeList = StringUtils.splitString2List(configMap.get("mimeType").toString(), ",");
                if (!typeList.stream()
                        .anyMatch(a -> extName.equalsIgnoreCase(a.toString().trim()))) {
                    throw new IOException(LangConfig.getInstance().get("can_not_supported_file_type"));
                }
            }

            byte[] fileBytes = UploadUtils.getBytes(uploadFileInfo.getBuffer());

            uploadFileInfo.getBuffer().clear();
            uploadFileInfo.setBuffer(null);

            if (configMap.get("maxSize") != null) {
                if (fileBytes.length / 1024 / 1024 >= Long.parseLong(configMap.get("maxSize").toString())) {
                    throw new IOException(LangConfig.getInstance().get("upload_field_to_be_long"));
                }
            }

            String value = null;
            String root = basePath;
            if(root.lastIndexOf("/") == root.length() - 1 ||
                    root.lastIndexOf("\\") == root.length() - 1) {
                root = root.substring(0, root.length() - 2);
            }

            value = UploadUtils.upload(root, filename, fileBytes);

            result.put(fileEntry.getKey(), value);

            if("ppt".equals(extName) || "pptx".equals(extName)) {
                result.put("pageCount", OfficeUtils.getPageCountByPPT(extName, fileBytes));
            }
        }

        return result;
    }

    protected void importExcel(String table, Map<String, Object> uploadFiles) throws Exception {

        if(uploadFiles == null) {
            throw new IOException("request error!!!");
        }

        if(getLoginUser() == null) {
            throw new IOException("login please!!!");
        }

        String currentDatasource = "default";
        if(CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE) != null) {
            currentDatasource = CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE).toString();
        }

        for(Map.Entry<String, Object> fileEntry : uploadFiles.entrySet()) {

            if(fileEntry.getValue() == null) {
                continue;
            }

            boolean isFile = fileEntry.getValue() instanceof UploadFileInfo;
            if(!isFile) { //没有文件名的不是文件
                continue;
            }

            UploadFileInfo uploadFileInfo = (UploadFileInfo) fileEntry.getValue();

            String filename = uploadFileInfo.getFilename();
            String[] arr = filename.split("\\.");
            byte[] fileBytes = UploadUtils.getBytes(uploadFileInfo.getBuffer());
            uploadFileInfo.getBuffer().clear();
            uploadFileInfo.setBuffer(null);

            String extName = arr[arr.length - 1];

            //MagicMatch mimeMatcher = Magic.getMagicMatch(fileBytes, true);
            String mimeType = uploadFileInfo.getContentType();


            if(StringUtils.isEmpty(mimeType)) {
                mimeType = UploadUtils.getMIMEType(extName); //mimeMatcher.getMimeType();
            }

            if(StringUtils.isEmpty(mimeType)) {
                continue;
            }

            List<String> fieldList = new ArrayList<String>();
            List<SheetHeaderModel> headerList = OfficeUtils.getHeadersByExcel(fileBytes, fieldList);

            Queryable entity = (Queryable) CCEntityContext.getEntity(table, fieldList);

            EasyExcel.read(new ByteArrayInputStream(fileBytes), entity.getClass(), new ExcelListener(table, entity, headerList)).sheet().doRead();
        }
    }

    protected List<Map<String, Object>> importToPdf(String table, Map<String, Object> uploadFiles) throws Exception {

        if(uploadFiles == null) {
            throw new IOException("request error!!!");
        }

        if(getLoginUser() == null) {
            throw new IOException("login please!!!");
        }

        List<Map.Entry<String, Object>> data = uploadFiles.entrySet().stream()
                .filter(a -> a.getValue() instanceof UploadFileInfo).collect(Collectors.toList());
        if(data == null || data.size() < 1) {
            throw new IOException("Post data can not be empty!!!");
        }

        Boolean save_full_text = uploadFiles.entrySet().stream()
                .filter(a-> a.getKey().equals("save_full_text") && Boolean.TRUE.equals(a.getValue()))
                .isParallel();

        Boolean save_source_ppt = uploadFiles.entrySet().stream()
                .filter(a-> a.getKey().equals("save_full_text") && Boolean.TRUE.equals(a.getValue()))
                .isParallel();

        String currentDatasource = "default";
        if(CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE) != null) {
            currentDatasource = CCApplicationContext.getThreadLocalMap().get(CURRENT_DATASOURCE).toString();
        }

        Map<String, Object> fieldSet = new HashMap<String, Object>();
        List<Map<String, Object>> resultSet = null;
        for(Map.Entry<String, Object> fileEntry : data) {

            UploadFileInfo uploadFileInfo = (UploadFileInfo) fileEntry.getValue();
            byte[] fileBytes = UploadUtils.getBytes(uploadFileInfo.getBuffer());
            String filename = uploadFileInfo.getFilename();
            String[] arr = filename.split("\\.");
            String extName = arr[arr.length - 1].toLowerCase();

            //另存为PDF
            if("ppt".equals(extName) || "pptx".equals(extName)) {
                resultSet = OfficeUtils.savePdfByPPT(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else if("xls".equals(extName) || "xlsx".equals(extName)) {
                resultSet = OfficeUtils.savePdfByExcel(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else if("doc".equals(extName)) {
                resultSet = OfficeUtils.savePdfByWord2003(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else if("docx".equals(extName)) {
                resultSet = OfficeUtils.savePdfByWord2007(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else if("htm".equals(extName) || "html".equals(extName)) {
                resultSet = OfficeUtils.savePdfByHtml(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else if("pdf".equals(extName)) {
                resultSet = OfficeUtils.savePdfForSplit(table, currentDatasource, fileEntry, filename, fileBytes);
            }

            else {
                throw new IOException(LangConfig.getInstance().get("can_not_supported_file_type"));
            }

            //保存ppt源文件
            if(save_source_ppt) {
                String pprSourcePath = String.format("/preview/ppt/%s/%s/%s/source", currentDatasource, table, fileEntry.getKey());
                String value = UploadUtils.upload(pprSourcePath, filename, fileBytes);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put(fileEntry.getKey(), value);
                result.put("number", 0);
                resultSet.add(result);
            }

            //创建全文索引
            if(save_full_text) {
                String value = OfficeUtils.getTextByPPT(fileBytes);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put(fileEntry.getKey(), value);
                result.put("number", 0);
                resultSet.add(result);
            }
        }

        //准备实体所需的字段
        List<String> fields = new ArrayList<String>();
        for(String field : fieldSet.keySet()) {
            fields.add(field);
        }
        for(Map<String, Object> item : resultSet) {
            for(String field : item.keySet()) {
                if(fields.contains(field)) {
                    continue;
                }
                fields.add(field);
            }
        }

        //保存到数据库
        Queryable query = (Queryable)CCEntityContext.getEntity(table, fields);
        for(Map<String, Object> item : resultSet) {
            for(String field : item.keySet()) {
                if(item.get(field) == null) {
                    continue;
                }
                ReflectionUtils.setFieldValue(query, field, item.get(field));
            }
            for(String field : fieldSet.keySet()) {
                if(fieldSet.get(field) == null) {
                    continue;
                }
                ReflectionUtils.setFieldValue(query, field, fieldSet.get(field));
            }
            Integer id = query.insert();
            CCApplicationContext.setGroupsUserIdValue(request, table, item, getLoginUser(), id);
        }

        return resultSet;
    }

    protected void playVideo(String table, String field, String id) throws Exception {

        String currentDatasource = CCEntityContext.getCurrentDatasourceId();
        Map<String, Object> uploadConfigMap = ApplicationConfig.getInstance().getMap(
                String.format("ccweb.upload.%s.%s.%s", currentDatasource, table, field)
        );
        if(uploadConfigMap == null || uploadConfigMap.size() < 1) {
            throw new IOException("can not find the upload config!!!");
        }

        String basePath = ApplicationConfig.getInstance().get("${ccweb.upload.basePath}", "");
        if(uploadConfigMap.containsKey("path") && uploadConfigMap.get("path")!=null) {
            basePath = basePath + uploadConfigMap.get("path").toString();
        }
        if(StringUtils.isEmpty(basePath)) {
            throw new IOException("can not find the upload path on config!!!");
        }

        Map<String, Object> data = get(table, id);
        if(!data.containsKey(field)) {
            throw new IOException(LangConfig.getInstance().get("wrong_field_name"));
        }

        decrypt(data);

        String content = data.get(field).toString();
        DownloadData downloadData = new DownloadData(CCEntityContext.getCurrentDatasourceId(), table, field, id);
        String filePath = downloadData.getFullPath(content, uploadConfigMap);
        if(!(new File(filePath)).exists()) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            return;
        }

        CCTriggerContext.exec(table, EventType.PlayVideo, downloadData, request);

        if (!StringUtils.isEmpty(downloadData.getMediaType().getType())) {
            response.setContentType(downloadData.getMediaType().getType());
        }
        request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, filePath);
        try {
            nonStaticResourceHttpRequestHandler.handleRequest(request, response);
        } catch (ServletException e) {
            log.error(LOG_PRE_SUFFIX + "<playVideo error===>" + e.getMessage());
        }
    }

    public class DownloadData extends ccait.ccweb.model.DownloadData {

        public DownloadData(String datasourceId, String table, String field, String id) {
            super(datasourceId, table, field, id);
        }

        public DownloadData(String datasourceId, String table, String field, String id, int page) {
            super(datasourceId, table, field, id, page);
        }

        @Override
        public ccait.ccweb.model.DownloadData invoke() throws Exception {
            Map<String, Object> data = get(table, id);
            if(data == null) {
                throw new Exception(LangConfig.getInstance().get("image_field_is_empty"));
            }
            if(!data.containsKey(field)) {
                throw new Exception(LangConfig.getInstance().get("wrong_field_name"));
            }

            decrypt(data);

            String content = data.get(field).toString();

            if(StringUtils.isEmpty(content)) {
                throw new Exception(LangConfig.getInstance().get("image_field_is_empty"));
            }

            Map<String, Object> uploadConfigMap = ApplicationConfig.getInstance().getMap(
                    String.format("ccweb.upload.%s.%s.%s", datasourceId, table, field)
            );

            String basePath = ApplicationConfig.getInstance().get("${ccweb.upload.basePath}", "");
            if(uploadConfigMap == null || uploadConfigMap.size() < 1) {
                uploadConfigMap = new HashMap<>();
                String mimeTypes = ApplicationConfig.getInstance().get("${ccweb.upload.mimeTypes}", "");
                if(!uploadConfigMap.containsKey("mimeType") && StringUtils.isNotEmpty(mimeTypes)) {
                    uploadConfigMap.put("mimeType", mimeTypes);
                }

                String maxSize = ApplicationConfig.getInstance().get("${ccweb.upload.maxSize}", "");
                if(!uploadConfigMap.containsKey("maxSize") && StringUtils.isNotEmpty(maxSize)) {
                    uploadConfigMap.put("maxSize", maxSize);
                }

                if(uploadConfigMap.containsKey("path") && uploadConfigMap.get("path")!=null) {
                    basePath = basePath + uploadConfigMap.get("path").toString();
                }
            }

            if(uploadConfigMap == null || uploadConfigMap.size() < 1) {
                throw new IOException(LangConfig.getInstance().get("not_found_upload_config"));
            }

            if(StringUtils.isNotEmpty(basePath)) {
                this.path = getFullPath(content, uploadConfigMap);
                File file = new File(this.path);
                if("/".equals(this.path.substring(0,1)) && !file.exists()) {
                    this.path = String.format("%s%s", System.getProperty("user.dir"), this.path);
                    file = new File(this.path);
                }
                if(!file.exists()) {
                    throw new IOException(LangConfig.getInstance().get("not_found_file"));
                }
                buffer = UploadUtils.getFileByteArray(file);
            }

            else {
                int splitPoint = content.indexOf("|::|");
                String fileString = content.substring(splitPoint + 4);
                if(splitPoint < 1) {
                    throw new IOException(LangConfig.getInstance().get("not_found_file"));
                }
                String messageBody = content.substring(0, splitPoint);
                arrMessage = messageBody.split("::");
                String[] arr = getMimeType().split("/");

                mediaType = new MediaType(arr[0], arr[1]);

                buffer = Base64.getDecoder().decode(fileString);

                saveTempFile(buffer);
            }

            return this;
        }
    }

}
