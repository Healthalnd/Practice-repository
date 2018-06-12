package com.qvc.selenium.data;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;

@ThreadSafe
public class TestManager {
    static final Logger logger = Logger.getLogger(TestManager.class
            .getName());


    private static TestDataLoad<String, HashMap<String, ArrayList<HashMap<String, String>>>> t = new TestDataLoad<String, HashMap<String, ArrayList<HashMap<String, String>>>>() {


        @Override
        public HashMap<String, ArrayList<HashMap<String, String>>> getData(String fileName)  throws Exception{
            try
            {
                return QVCTestXlsParser.loadTestFile(fileName);
            }
            catch (Exception e)
            {
                throw e;
            }
        }

    };


    private static TestDataLoad<String, HashMap<String, ArrayList<HashMap<String, String>>>> TESTCACHE = new TestDataLoadManager<String,HashMap<String, ArrayList<HashMap<String, String>>>>(t);



    public static HashMap<String, ArrayList<HashMap<String, String>>> getTestXSLFile (
            String filename) throws Exception {
        HashMap<String, ArrayList<HashMap<String, String>>> result = null;
        try {
            result = TESTCACHE.getData(filename + ".xlsx");
        } catch (Exception e) {
            logger.error("error parsing file "+ filename, e);
            throw e;
        }

        return result;
    }

    public static void clearCache(){
        TESTCACHE = new TestDataLoadManager<String,HashMap<String, ArrayList<HashMap<String, String>>>>(t);
    }

	private static HashMap<String, ArrayList<HashMap<String, String>>> loadTestFile(
			String filenameWithPath) {
		XSSFWorkbook curXssfWorkBook;
		String fileToParse = null;
		HashMap<String, ArrayList<HashMap<String, String>>> dataObject = new HashMap<String, ArrayList<HashMap<String, String>>>();
		try {

			OPCPackage pkg = OPCPackage.open(filenameWithPath);
			logger.debug("Init: Get Excel file from: " + filenameWithPath);
			curXssfWorkBook = new XSSFWorkbook(pkg);


			int sheetNumber = curXssfWorkBook.getNumberOfSheets();
			logger.debug("Number of sheets: " + sheetNumber);
			dataObject = new HashMap<String, ArrayList<HashMap<String, String>>>();
			for (int i = 0; i < sheetNumber; i++) {
				XSSFSheet tempSheet = curXssfWorkBook.getSheetAt(i);
				logger.debug("sheet Name : " + curXssfWorkBook.getSheetName(i));
				dataObject.put(curXssfWorkBook.getSheetName(i).toUpperCase(),
						populateSheet(tempSheet));
			}
		} catch (Exception e) {
			logger.debug("Exception when opening file : " +filenameWithPath);
			logger.debug(e.getMessage());
			logger.fatal(e);
		}
		logger.debug("finished loading test file");
		return dataObject;
	}

	private static ArrayList<HashMap<String, String>> populateSheet(
			XSSFSheet workSheet) {
		ArrayList<HashMap<String, String>> curSheet = new ArrayList<HashMap<String, String>>();
		XSSFRow headerRow = workSheet.getRow(0);
		XSSFRow tempRow = null;
		String key = null;
		logger.debug("Row Count: " + workSheet.getLastRowNum());

		for (int i = 1; i <= workSheet.getLastRowNum(); i++) {
			tempRow = workSheet.getRow(i);
			HashMap<String, String> hashMapRow = new HashMap<String, String>();
			logger.debug("Cell Count: " + tempRow.getLastCellNum());
			for (int j = 0; j < tempRow.getLastCellNum(); j++) {
				key = getCellValue(headerRow.getCell(j));
				if ( key != null) { // key not null
					String cellData = getCellValue(tempRow.getCell(j));
					if(cellData != null){ // value not null
						logger.debug("Cell(" + i + ", " + j + "):" + cellData);
						hashMapRow.put(key.toString(), cellData.toString());
						
					}
				}
			}
			if(!hashMapRow.isEmpty()){//row's data not empty then add
				logger.debug("hashMapRow: " + hashMapRow);
				curSheet.add(hashMapRow);
			}

		}
		logger.debug("curSheet: " + curSheet);
		return curSheet;
	}

	private static String getCellValue(XSSFCell cell) {
		// check the cell type and process accordingly
		String cellValue = null;
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				cellValue = cell.getStringCellValue().trim();
				break;
			case Cell.CELL_TYPE_NUMERIC:
//				cellValue = Double.toString(cell.getNumericCellValue());
				cellValue = String.format("%1$.0f", cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cellValue = Boolean.toString(cell.getBooleanCellValue());
				break;
			}
		}
		return cellValue;
	}

}
