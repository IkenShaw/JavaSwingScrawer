package org.iken.untils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtil {

    public static final Map<String, Map<Integer, List<Object>>> DATA = new HashMap<>();
    public static String path = "";

    public static String dealExlce(String path){
        ExcelUtil.path = path;
        // 存放数据  Map<sheetName, Map<rowNumber, columnsData(List)>>，读取时清除上次的
        DATA.clear();
        String result = "";
        File file = new File(path);
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        if (file.exists()){
            try {
                // 开始读取
                fileInputStream = new FileInputStream(file);
                inputStream = fileInputStream;
                workbook = new XSSFWorkbook(inputStream);
                // 获取页签数
                int sheets = workbook.getNumberOfSheets();
                for (int i = 0; i < sheets; i++) {
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    Map<Integer, List<Object>> rowDataMap = new HashMap<>();
                    DATA.put(sheet.getSheetName(), rowDataMap);
                    int rowNumbers = sheet.getPhysicalNumberOfRows();
                    for (int j = 0; j < rowNumbers; j++) {
                        XSSFRow row = sheet.getRow(j);
                        List<Object> columnsDataList = new ArrayList<>();
                        rowDataMap.put(j, columnsDataList);
                        for (int column = 0; column < 16; column++) {
                            XSSFCell cell = row.getCell(column);
                            Object cellValue = null;
                            switch (cell.getCellType()){
                                case NUMERIC:
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                    break;
                                case STRING:
                                    cellValue = cell.getStringCellValue();
                                    break;
                                default:
                                    break;
                            }
                            columnsDataList.add(cellValue);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != workbook) {
                        workbook.close();
                    }
                    if (null != inputStream){
                        inputStream.close();
                    }
                    if (null != fileInputStream){
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            result = "路径下文件不存在";
        }
        return result;
    }

    /**
     * 根据查询回来的数据生成Excel
     * @return 生成文件结果
     */
    public static String generateExcel(){
        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath();
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            desktopPath += "/Desktop"; // 获取当前系统桌面路径
        }
        String result = "已查询完毕，生成文件在同文件夹下，文件名为\"查询[时间戳]\"";
        StringBuffer filePathBuffer = new StringBuffer();
        filePathBuffer.append(desktopPath + "/");
        filePathBuffer.append("查询-");
        filePathBuffer.append(new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date().getTime()));
        filePathBuffer.append(".xlsx");
        String filePathStr = filePathBuffer.toString();
        OutputStream fileOut = null;
        if (!ExcelUtil.DATA.isEmpty() && ExcelUtil.DATA.size() > 0) {
            try {
                XSSFWorkbook workbook = new XSSFWorkbook();
                Map<String, Map<Integer, List<Object>>> exportDateMap = ExcelUtil.DATA;
                // 循环Excel的准备变量
                Set<Map.Entry<String, Map<Integer, List<Object>>>> sheetSet = exportDateMap.entrySet();
                Iterator<Map.Entry<String, Map<Integer, List<Object>>>> sheetIterator = sheetSet.iterator();
                Map.Entry<String, Map<Integer, List<Object>>> sheetEntry = null;
                XSSFSheet sheet = null;
                // 循环一个Sheet页签的准备变量
                Map<Integer, List<Object>> sheetDateMap = null;
                Set<Map.Entry<Integer, List<Object>>> rowDataEntry = null;
                Iterator<Map.Entry<Integer, List<Object>>> rowIterator = null;
                Map.Entry<Integer, List<Object>> rowEntry = null;
                Integer rowNum = null;
                List<Object> rowData = null;
                XSSFRow row = null;
                while (sheetIterator.hasNext()){
                    sheetEntry = sheetIterator.next();
                    sheet = workbook.createSheet(sheetEntry.getKey()); // 获取sheetName
                    sheetDateMap = sheetEntry.getValue(); // 获取sheetDateMap
                    // 准备循环每行数据
                    rowDataEntry = sheetDateMap.entrySet();
                    rowIterator = rowDataEntry.iterator();
                    while (rowIterator.hasNext()) {
                        rowEntry = rowIterator.next();
                        rowNum = rowEntry.getKey();
                        row = sheet.createRow(rowNum);
                        rowData = rowEntry.getValue();
                        // 每个单元格的数据
                        for (int i = 0; i < rowData.size(); i++) {
                            XSSFCell cell = row.createCell(i);
                            cell.setCellValue(rowData.get(i).toString());
                        }
                    }
                }
                fileOut = new FileOutputStream(filePathStr);
                workbook.write(fileOut);
                fileOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOut != null){
                        fileOut.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            result = "无查询出任何数据，无法生成Excel";
        }
        return result;
    }

    public static void main(String[] args) {
        Integer row = new Integer(0);
        List<Object> dataList = new ArrayList<>();
        dataList.add(0);
        Map<Integer, List<Object>> dataMap = new HashMap<>();
        dataMap.put(row, dataList);
        ExcelUtil.DATA.put("sheet", dataMap);
        ExcelUtil.generateExcel();
    }
}
