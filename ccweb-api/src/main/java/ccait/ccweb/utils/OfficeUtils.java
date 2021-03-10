package ccait.ccweb.utils;

import ccait.ccweb.config.LangConfig;
import ccait.ccweb.excel.Excel2Pdf;
import ccait.ccweb.excel.ExcelObject;
import ccait.ccweb.model.SheetHeaderModel;
import ccait.ccweb.pdf.PdfFile;
import ccait.ccweb.pdf.PdfFileObject;
import ccait.ccweb.ppt.PPTTool;
import ccait.ccweb.word.Html2Pdf;
import ccait.ccweb.word.Word2Html;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficeUtils {

    private static final Logger logger = LoggerFactory.getLogger(OfficeUtils.class);
    private static final String ISO_8859_1 = "ISO-8859-1";

    public static void saveImagesByPPT(String table, String currentDatasource, List<Map<String, Object>> resultSet, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws IOException {

        resultSet = getImagesByPPT(entry, filename, fileBytes);

        if(resultSet == null) {
            return;
        }

        for(int i=0; i < resultSet.size(); i++) {

            byte[] bytes = (byte[]) resultSet.get(i).get(entry.getKey());

            // save the output
            String pptPagePath = String.format("/preview/image/%s/%s/%s", currentDatasource, table, entry.getKey());

            String realname = String.format("%s_%s.png", filename, i);

            String fullpath = UploadUtils.upload(pptPagePath, realname, bytes);

            resultSet.get(i).put(entry.getKey(), fullpath);
        }
    }

    public static List<Map<String, Object>> getImagesByPPT(Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws IOException {

        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();

        String[] arr = filename.split("\\.");

        String extesion = arr[arr.length - 1];

        SlideShow ppt = PPTTool.getSildeShow(extesion, fileBytes);
        Dimension pgsize = ppt.getPageSize();

        for (int i = 0; i < ppt.getSlides().size(); i++) {
            try {
                //防止中文乱码
                PPTTool.setPPTFont(extesion, (Slide) ppt.getSlides().get(i), "宋体");
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                // clear the drawing area
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

                // render
                PPTTool.drawPPT(extesion, graphics, (Slide) ppt.getSlides().get(i));

                Map<String, Object> result = new HashMap<String, Object>();
                result.put(entry.getKey(), ImageUtils.toBytes(img));
                result.put("page_number", i + 1);
                resultSet.add(result);

            } catch (Exception e) {
                logger.error("第"+(i+1)+"页ppt转换出错");
            }
        }

        return resultSet;
    }

    public static List<SheetHeaderModel> getHeadersByExcel(byte[] fileBytes, List<String> fieldList) throws IOException {
        InputStream is = new ByteArrayInputStream(fileBytes);
        List<SheetHeaderModel> headerList = new ArrayList<SheetHeaderModel>();
        if(FileMagic.valueOf(new ByteArrayInputStream(fileBytes)).name().equalsIgnoreCase("OOXML")) {
            XSSFSheet sheet = new XSSFWorkbook(is).getSheet("schema");
            XSSFRow titleRow = sheet.getRow(0);
            XSSFRow fieldRow = sheet.getRow(1);
            XSSFRow typeRow = sheet.getRow(2);
            for(int i=0; i<titleRow.getLastCellNum(); i++) {
                SheetHeaderModel headerModel = new SheetHeaderModel();
                String title = titleRow.getCell(i).getStringCellValue();
                String field = fieldRow.getCell(i).getStringCellValue();
                String type = typeRow.getCell(i).getStringCellValue();

                headerModel.setHeader(title);
                headerModel.setField(field);
                headerModel.setIndex(i);
                headerModel.setType(type);

                fieldList.add(field);
                headerList.add(headerModel);
            }
        }

        else if(FileMagic.valueOf(new ByteArrayInputStream(fileBytes)).name().equalsIgnoreCase("OLE2")) {
            HSSFSheet sheet = new HSSFWorkbook(is).getSheet("schema");
            HSSFRow titleRow = sheet.getRow(0);
            HSSFRow fieldRow = sheet.getRow(1);
            HSSFRow typeRow = sheet.getRow(2);
            for(int i=0; i<titleRow.getLastCellNum(); i++) {
                SheetHeaderModel headerModel = new SheetHeaderModel();
                String title = titleRow.getCell(i).getStringCellValue();
                String field = fieldRow.getCell(i).getStringCellValue();
                String type = typeRow.getCell(i).getStringCellValue();

                headerModel.setHeader(title);
                headerModel.setField(field);
                headerModel.setIndex(i);
                headerModel.setType(type);

                fieldList.add(field);
                headerList.add(headerModel);
            }
        }

        else {
            throw new IOException(LangConfig.getInstance().get("can_not_supported_file_type"));
        }
        return headerList;
    }

    public static String getTextByPPT(byte[] fileBytes) throws IOException {
        InputStream iis = new ByteArrayInputStream(fileBytes);
        PowerPointExtractor extractor=new PowerPointExtractor(iis);
        String text = extractor.getText();
        iis.close();

        return text;
    }

    public static Integer getPageCountByPPT(String extesion, byte[] fileBytes) throws IOException {

        SlideShow ppt = PPTTool.getSildeShow(extesion, fileBytes);

        return ppt.getSlides().size();
    }

    public static List<Map<String, Object>> savePdfByPPT(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws IOException, DocumentException {

        List<Map<String, Object>> resultSet = getImagesByPPT(entry, filename, fileBytes);

        if(resultSet == null) {
            return resultSet;
        }

        for(int i=0; i < resultSet.size(); i++) {

            // save the output
            String path = String.format("/preview/ppt/%s/%s/%s", currentDatasource, table, entry.getKey());

            String realname = String.format("%s_%s.pdf", filename, i);

            if(OSUtils.isWindows() && "/".equals(path.substring(0, 1))) {
                path = System.getProperty("user.dir") + path;
            }

            UploadUtils.mkdirs(path);

            String filepath = UploadUtils.ensureFilePath(realname, path);
            if(OSUtils.isWindows()) {
                filepath = filepath.replaceAll("/", "\\\\");
            }
            else {
                filepath = filepath.replaceAll("\\\\", "/");
            }

            String fullpath = path + filepath;
            File file = new File(fullpath);

            byte[] bytes = (byte[]) resultSet.get(i).get(entry.getKey());
            Image image = Image.getInstance(bytes);
            Document document = new Document(new Rectangle(-72,-72,image.getWidth(), image.getHeight()));
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            document.add(image);
            document.close();

            resultSet.get(i).put(entry.getKey(), fullpath);
        }

        return resultSet;
    }

    public static List<Map<String, Object>> savePdfByExcel(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws IOException, InvalidFormatException, DocumentException {

        String path = String.format("/preview/excel/%s/%s/%s", currentDatasource, table, entry.getKey());

        String realname = String.format("%s_%s.pdf", filename, 0);

        if(OSUtils.isWindows() && "/".equals(path.substring(0, 1))) {
            path = System.getProperty("user.dir") + path;
        }

        UploadUtils.mkdirs(path);

        String filepath = UploadUtils.ensureFilePath(realname, path);
        String fullpath = path + filepath;
        if(OSUtils.isWindows()) {
            fullpath = fullpath.replaceAll("/", "\\\\");
        }
        else {
            fullpath = fullpath.replaceAll("\\\\", "/");
        }

        File file = new File(fullpath);
        List<ExcelObject> objects = new ArrayList<ExcelObject>();

        InputStream is = new ByteArrayInputStream(fileBytes);
        objects.add(new ExcelObject(filename, is));

        FileOutputStream fos = new FileOutputStream(file);
        Excel2Pdf pdf = new Excel2Pdf(objects , fos);
        pdf.convert();

        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        String finalFullpath = fullpath;
        resultSet.add(new HashMap<String, Object>() {
            {put(entry.getKey(), finalFullpath); }
        });

        return resultSet;
    }

    public static List<Map<String, Object>> savePdfByWord2003(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws Exception {

        String path = String.format("/preview/word/%s/%s/%s", currentDatasource, table, entry.getKey());

        String realname = String.format("%s_%s.pdf", filename, 0);

        String html = Word2Html.convertDoc2Html(fileBytes);
        fileBytes = Html2Pdf.convertHtmlToPdf(html);

        if(OSUtils.isWindows() && "/".equals(path.substring(0, 1))) {
            path = System.getProperty("user.dir") + path;
        }

        String filepath = UploadUtils.upload(path, realname, fileBytes);
        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        filepath = path + filepath;

        if(OSUtils.isWindows()) {
            filepath = filepath.replaceAll("/", "\\\\");
        }
        else {
            filepath = filepath.replaceAll("\\\\", "/");
        }

        String finalFilepath = filepath;
        resultSet.add(new HashMap<String, Object>() {
            {put(entry.getKey(), finalFilepath); }
        });

        return resultSet;
    }

    public static List<Map<String, Object>> savePdfByWord2007(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws Exception {

        String path = String.format("/preview/word/%s/%s/%s", currentDatasource, table, entry.getKey());

        String realname = String.format("%s_%s.pdf", filename, 0);

        String html = Word2Html.convertDocx2Html(fileBytes);
        fileBytes = Html2Pdf.convertHtmlToPdf(html);

        if(OSUtils.isWindows() && "/".equals(path.substring(0, 1))) {
            path = System.getProperty("user.dir") + path;
        }

        String filepath = UploadUtils.upload(path, realname, fileBytes);
        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        filepath = path + filepath;

        if(OSUtils.isWindows()) {
            filepath = filepath.replaceAll("/", "\\\\");
        }
        else {
            filepath = filepath.replaceAll("\\\\", "/");
        }

        String finalFilepath = filepath;
        resultSet.add(new HashMap<String, Object>() {
            {put(entry.getKey(), finalFilepath); }
        });

        return resultSet;
    }

    public static List<Map<String, Object>> savePdfByHtml(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws Exception {

        String filepath = String.format("/preview/word/%s/%s/%s", currentDatasource, table, entry.getKey());

        String realname = String.format("%s_%s.pdf", filename, 0);

        fileBytes = Html2Pdf.convertHtmlToPdf(new String(fileBytes, ISO_8859_1));

        filepath = UploadUtils.upload(filepath, realname, fileBytes);
        String finalFilepath = filepath;
        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        resultSet.add(new HashMap<String, Object>() {
            {put(entry.getKey(), finalFilepath); }
        });

        return resultSet;
    }

    public static List<Map<String, Object>> savePdfForSplit(String table, String currentDatasource, Map.Entry<String, Object> entry, String filename, byte[] fileBytes) throws Exception {

        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        InputStream is = new ByteArrayInputStream(fileBytes);
        PdfFile pdfFile = new PdfFile(is);
        pdfFile.load();
        for (int i=0; i<pdfFile.getPageList().size(); i++) {

            String path = String.format("/preview/word/%s/%s/%s", currentDatasource, table, entry.getKey());

            String realname = String.format("%s_%s.pdf", filename, i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfFileObject pdfObject = pdfFile.getPageList().get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("page_number", i + 1);
            pdfObject.writeToOutputStream(outputStream);
            String filepath = UploadUtils.upload(path, realname, outputStream.toByteArray());
            map.put(entry.getKey(), filepath);
            resultSet.add(map);
        }

        return resultSet;
    }
}
