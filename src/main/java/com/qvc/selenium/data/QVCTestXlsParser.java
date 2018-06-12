package com.qvc.selenium.data;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;

public class QVCTestXlsParser {
	
	static final Logger logger = Logger.getLogger(QVCTestXlsParser.class
			.getName());
	
	
	protected static HashMap<String, ArrayList<HashMap<String, String>>> loadTestFile(
			String filename) throws Exception {
		XSSFWorkbook curXssfWorkBook;

		HashMap<String, ArrayList<HashMap<String, String>>> dataObject = new HashMap<String, ArrayList<HashMap<String, String>>>();
		try {

			logger.debug("Init: Get Excel file from: " + filename);
			OPCPackage pkg = OPCPackage.open( filename);
			logger.debug("Init: pkg file from: " + filename);
			curXssfWorkBook = new XSSFWorkbook(pkg);
			logger.debug("Init: work file from: " + filename);
//			pkg.close(); Close the open, writable package and save its content.- We not need this way, so comment it
			pkg.revert(); //Close the package WITHOUT saving its content - Simon, 5/16/2016
			// logger.deb			}
        } catch (Exception e) {
            logger.error("Exception when opening file : " + filename, e);
            throw e;

        }

        int sheetNumber = curXssfWorkBook.getNumberOfSheets();
        logger.debug("Number of sheets: " + sheetNumber);
        dataObject = new HashMap<String, ArrayList<HashMap<String, String>>>();
        for (int i = 0; i < sheetNumber; i++) {
            XSSFSheet tempSheet = curXssfWorkBook.getSheetAt(i);
            logger.debug("sheet Name : " + curXssfWorkBook.getSheetName(i));
            dataObject.put(curXssfWorkBook.getSheetName(i).toUpperCase(),
                    populateSheet(tempSheet));
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
			if (tempRow == null)
				continue;
			HashMap<String, String> hashMapRow = new HashMap<String, String>();
			logger.debug("Cell Count: " + tempRow.getLastCellNum());
			for (int j = 0; j < tempRow.getLastCellNum(); j++) {
				key = getCellValue(headerRow.getCell(j));
				if ( key != null) { // key not null
					String cellData = getCellValue(tempRow.getCell(j));
					if(cellData != null){//empty value will not add, Simon - 10/30/2015
						
						logger.debug("Cell(" + i + ", " + j + "):" + cellData);
						hashMapRow.put(key.toString(), cellData.toString());
					}
				}
			}
			logger.debug("hashMapRow: " + hashMapRow);
			if(hashMapRow.size() != 0)//add by Simon, remove the empty row - 12/03/2015
				curSheet.add(hashMapRow);

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
				//cell.getNumericCellValue() - return Double like: 2712.0, use format "%1$.of" to remove the extra 0
				cellValue = String.format("%1$.0f", cell.getNumericCellValue());
//      		cellValue = Double.toString(cell.getNumericCellValue()); Comment by Simon, this statement still return value like: 5.0
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cellValue = Boolean.toString(cell.getBooleanCellValue());
				break;
			}
		}
		return cellValue;
	}

}

