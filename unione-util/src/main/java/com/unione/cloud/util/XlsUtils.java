package com.unione.cloud.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.Data;
import lombok.Getter;


public class XlsUtils {

    /**
     * 读取Excel文件
     * @param is Excel文件输入流
     * @param sheetIndex 工作表索引
     * @param headRow 表头行索引
     */
    public static void read(InputStream is,XlsDataProcess process) {
        XlsReadListener listener = new XlsReadListener(process);
        EasyExcel.read(is, listener)
            .sheet(process.getSheetIndex())
            .headRowNumber(process.getHeadRow())
            .doRead();
    }
    
    /**
     * 写入Excel文件
     * @param tmpl 模板文件输入流
     * @param data 数据列表
     * @param headRow 表头行索引
     * @return 生成的Excel文件
     */
    public static File write(InputStream tmpl, List<Map<String,Object>> data, int headRow) {
        File outputFile = FileUtil.createTempFile("xls_output_", ".xlsx", true);
        byte[] templateBytes = IoUtil.readBytes(tmpl);
        FileUtil.writeBytes(templateBytes, outputFile);
        
        XlsReadListener listener = new XlsReadListener(new XlsDataProcess(headRow) {
            @Override
            public void process(List<Map<String, Object>> rows) {}
        });
        
        EasyExcel.read(new ByteArrayInputStream(templateBytes), listener)
            .sheet(0)
            .headRowNumber(headRow)
            .doRead();
        
        List<XlsHeaderItem> headerList = listener.getHeaders();
        AssertUtil.service().notEmpty(headerList, "xls表头不能为空");
        
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter(outputFile);
            writer.setSheet(0);
            Row rowTmpl = writer.getOrCreateRow(headRow);
            writer.setCurrentRow(headRow + 1);

            for (int index = 0; index < data.size(); index++) {
                Map<String, Object> rowData = data.get(index);
                Row rowObj = writer.getOrCreateRow(headRow + index);
                rowObj.setRowStyle(rowTmpl.getRowStyle());

                for (XlsHeaderItem head : headerList) {
                    Cell cellTmpl = rowTmpl.getCell(head.getIndex());

                    Cell cell = null;
                    if(index==0){
                        cell=cellTmpl;
                    }else{
                        cell = rowObj.createCell(head.getIndex(), cellTmpl != null ? cellTmpl.getCellType() : CellType.STRING);
                        if (cellTmpl != null) {
                            cell.setCellStyle(cellTmpl.getCellStyle());
                            if(cellTmpl.getCellType()==CellType.FORMULA && cellTmpl.getCellFormula()!=null){
                                cell.setCellFormula(cellTmpl.getCellFormula());
                                continue;
                            }
                        }
                    }

                    Object value = rowData.get(head.getName());
                    if (value != null) {
                        if (value instanceof Date) {
                            cell.setCellValue((Date) value);
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("写入Excel文件失败", e);
        }finally{
            IoUtil.close(writer);
        }
        
        return outputFile;
    }


     /**
      * 写入Excel文件
      * @param tmpl 模板文件输入流
      * @param header 表头
      * @param data 数据列表
      * @param headRow 表头行索引
      * @return 生成的Excel文件
      */
    public static File write(InputStream tmpl,XlsHeader header, List<Map<String,Object>> data) {
        File outputFile = FileUtil.createTempFile("xls_output_", ".xlsx", true);
        FileUtil.writeFromStream(tmpl, outputFile);

        ExcelWriter writer = ExcelUtil.getWriter(outputFile);
        writer.setSheet(0);

        // 生成表头
        try{
            Row rowTmpl = writer.getOrCreateRow(header.getRow()-1);
            Cell cellTmpl = rowTmpl.getCell(header.getCol()-1);
            cellTmpl.setCellValue(header.getNames().get(0));

            for(int index=1;index<header.getNames().size();index++){
                String name=header.getNames().get(index);
                Cell cell = rowTmpl.createCell(header.getCol() + index-1, CellType.STRING);
                cell.setCellStyle(cellTmpl.getCellStyle());
                cell.setCellValue(name);
            }
            if(header.getRow()>1){
                CellRangeAddress rangeAddress=writer.getSheet().getMergedRegions().stream()
                    .filter(range->range.isInRange(header.getRow()-2, header.getCol()-1)).findFirst().orElse(null);
                if(rangeAddress!=null){
                    rangeAddress.setLastColumn(rangeAddress.getLastColumn()+header.getNames().size()-1);
                }
            }
        }catch(Exception e){
            writer.close();
            throw new ServiceException("生成表头失败", e);
        }

        // 生成数据
        try{
            Row rowTmpl = writer.getOrCreateRow(header.getRow());
            Cell colTmp=rowTmpl.getCell(header.getCol()-1);
            Cell colSeq=null;
            if(header.getCol()>1){
                colSeq=rowTmpl.getCell(header.getCol()-2);
            }
            
            int headRow=header.getRow();
            for(int rowIndex=0;rowIndex<data.size();rowIndex++){
                Map<String,Object> rowData=data.get(rowIndex);
                Row rowObj = writer.getOrCreateRow(headRow + rowIndex);
                rowObj.setRowStyle(rowTmpl.getRowStyle());

                if(colSeq!=null && rowIndex>0){
                    Cell cell = rowObj.createCell(header.getCol()-2, colSeq.getCellType());
                    if(colSeq.getCellType()==CellType.FORMULA && colSeq.getCellFormula()!=null){
                        cell.setCellFormula(colSeq.getCellFormula());
                    }
                    cell.setCellStyle(colSeq.getCellStyle());
                }
                for(int colIndex=0;colIndex<header.getNames().size();colIndex++){
                    String name=header.getNames().get(colIndex);
                    Cell cell = null;
                    if(rowIndex==0 && colIndex==0){
                        cell=colTmp;
                    }else{
                        cell = rowObj.createCell(header.getCol() + colIndex-1,colTmp.getCellType());
                    }

                    cell.setCellStyle(colTmp.getCellStyle());

                    Object value = rowData.get(name);
                    if (value != null) {
                        if (value instanceof Date) {
                            cell.setCellValue((Date) value);
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }
        }catch(Exception e){
            throw new ServiceException("生成数据失败", e);
        }finally{
            writer.close();
        }

        return outputFile;
    }

    @Data
    public static abstract class XlsDataProcess{
        private int sheetIndex=0;
        private int headRow;
        private int batchSize=5000;

        public XlsDataProcess(int headRow) {
            this.headRow = headRow;
        }

        public XlsDataProcess(int sheetIndex, int headRow) {
            this.sheetIndex = sheetIndex;
            this.headRow = headRow;
        }

        public XlsDataProcess(int sheetIndex, int headRow,int batchSize) {
            this.sheetIndex = sheetIndex;
            this.headRow = headRow;
            this.batchSize = batchSize;
        }

        public abstract void process(List<Map<String, Object>> rows);
    }


    @Data
    public static class XlsHeader{
        /**
         * 开始行号
         */
        private int row;
        /**
         * 开始列号
         */
        private int col;
        /**
         * 标题列表
         */
        private List<String> names;

    }

    private static class XlsHeaderItem {
        @Getter
        private String name;
        @Getter
        private int index;
        public XlsHeaderItem(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    /**
     * Excel读取监听器
     */
    private static class XlsReadListener extends AnalysisEventListener<Map<Integer, Object>> {
        
        private XlsDataProcess process;
        private List<Map<String, Object>> dataList = new ArrayList<>();
        private List<List<XlsHeaderItem>> headers = new ArrayList<>();

        public XlsReadListener(XlsDataProcess process) {
            this.process = process;
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            List<XlsHeaderItem> names=new ArrayList<>();
            int maxIndex = headMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            for (int i = 0; i <= maxIndex; i++) {
                String header = headMap.get(i);
                header = normalizeHeader(header);
                if (!StrUtil.isEmpty(header)) {
                    names.add(new XlsHeaderItem(header.trim(), i));
                }
            }
            headers.add(names);
        }

        private String normalizeHeader(String header) {
            if (header == null) {
                return null;
            }
            header = header.trim();
            header = header.replace("\uFEFF", "").replace("\u200B", "");
            return header;
        }

        @Override
        public void invoke(Map<Integer, Object> rowData, AnalysisContext context) {
            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 0; i < headers.get(headers.size()-1).size(); i++) {
                XlsHeaderItem header = headers.get(headers.size()-1).get(i);
                Object value = rowData.get(header.getIndex());
                rowMap.put(header.getName(), value);
            }
            dataList.add(rowMap);
            if (dataList.size() >= process.getBatchSize()) {
                process.process(dataList);
                dataList.clear();
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            if (!dataList.isEmpty()) {
                process.process(dataList);
                dataList.clear();
            }
        }
        
        public List<XlsHeaderItem> getHeaders() {
            if (headers.isEmpty()) {
                return null;
            }
            return headers.get(headers.size()-1);
        }
    }



    public static void main(String[] args) {
        List<Map<String, Object>> data = new ArrayList<>();
        read(FileUtil.getInputStream("d://xls_data.xlsx"), new XlsDataProcess(3){
            @Override
            public void process(List<Map<String, Object>> rows) {
                System.out.println(rows);
                data.addAll(rows);
            }
        });

        File file = write(FileUtil.getInputStream("d://xls_tmpl.xlsx"), data, 3);
        System.out.println(file.getAbsolutePath());

        XlsHeader header=new XlsHeader();
        header.setRow(3);
        header.setCol(3);
        header.setNames(List.of("状态", "台账名称"));
        file = write(FileUtil.getInputStream("d://xls_base.xlsx"),header, data);
        System.out.println(file.getAbsolutePath());
    }

}