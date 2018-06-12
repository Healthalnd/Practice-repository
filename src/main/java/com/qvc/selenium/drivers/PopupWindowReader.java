package com.qvc.selenium.drivers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopupWindowReader {
    private static final Logger logger = Logger.getLogger(PopupWindowReader.class.getName());
    public static final Integer PORT = 4939;
    public static Map<String, String> cuttingElementMatrix = new HashMap<String, String>() {
        {
            put("className", "class");
        }

        {
            put("mID", "id");
        }

        {
            put("accessibility:getContentDescription()", "content-desc");
        }

        {
            put("layout:getLocationOnScreen_x()", "x");
        }

        {
            put("layout:getLocationOnScreen_y()", "y");
        }

        {
            put("layout:getWidth()", "width");
        }

        {
            put("layout:getHeight()", "height");
        }

        {
            put("text:mText", "text");
        }

        {
            put("level", "level");
        }
    };


    //  private static final Logger logger = Logger.getLogger(PopupWindowReaderMikus.class.getName());

    public static void main(String[] args) throws IOException {
        org.apache.log4j.BasicConfigurator.configure();
        //   logger.debug(getCenterOfTextElement("PopupWindow", "Swiss Duo"));
        tap(getCenterOfTextElement("PopupWindow", "Swiss Duo"));
    }

    public static Point getCenterOfTextElement(String winName, String text) throws IOException {
        //get WindowHashCode by part of name
        String popupWinHashCode = getWinHashCode(winName);

        //Get Activity DUMP for current Window
        List<String> src = winSource(popupWinHashCode);

        //Parsing And Cutting ElementSource
        List<Map<String, String>> usefulElementsData = getUsefulElements(src);

        //Find TextElement in then win source
        Map<String, String> elementSource = findElementByText(usefulElementsData, text);

        //getElementCenter
        Point res = getElementCenter(elementSource);

        //correct point with Absolute window coordinate
        res.y = getAbsoluteDelta(winName).y + res.y;

        logger.debug("getCenterOfTextElement()=" + res);

        return res;
    }

    private static List<Map<String, String>> getUsefulElements(List<String> src) {
        List<Map<String, String>> res = new ArrayList<>();
        for (String e : src) {
            res.add(getUsefulElement(e));
        }
        return res;
    }


    private static Point getAbsoluteDelta(String winName) throws IOException {
        String src = cmd("adb shell dumpsys window " + winName);
        String coord = src.split("containing=\\[")[1].split("\\]")[0];
        Point res = new Point(Integer.parseInt(coord.split(",")[0]), Integer.parseInt(coord.split(",")[1]));
        logger.warn("getAbsoluteDelta(" + winName + ")=" + res);
        return res;
    }

    private static Point getElementCenter(Map<String, String> usefulElementData) {
        Point res = null;
        try {
            int x = Integer.parseInt(usefulElementData.get("x"));
            int y = Integer.parseInt(usefulElementData.get("y"));
            int width = Integer.parseInt(usefulElementData.get("width"));
            int height = Integer.parseInt(usefulElementData.get("height"));
            logger.debug("Element().bundle()=[" + x + "," + y + "][w=" + width + ",h=" + height + "]");
            res = new Point(x + width / 2, y + height / 2);
        } catch (Exception e) {
            logger.warn("invalid Element for centerGetting:" + usefulElementData);
        }
        return res;
    }

    private static Map<String, String> getUsefulElement(String src) {
        //       logger.debug("Element source=" + src);
        Map<String, String> res = new HashMap<>();
        String next = "\\s\\S+=\\d+,";
        for (Map.Entry<String, String> entry : cuttingElementMatrix.entrySet()) {
            String key = entry.getKey();
            String usKey = entry.getValue();
            String keyRegExp = (key + "=\\d+,").replace("(", "\\(").replace(")", "\\)");
            if (key.equals("className")) {
                res.put(usKey, src.split("@")[0].trim());
            } else {
                if (key.equals("level")) {
                    res.put(usKey, src.split("\\S")[0].length() + "");
                } else {
                    try {
                        String value = src.split(keyRegExp)[1].split(next)[0];
                        res.put(usKey, value);
                    } catch (Exception e) {
                        res.put(usKey, null);
                    }
                }
            }
        }
        logger.debug("getUsefulElement()=" + res);
        return res;
    }

    private static Map<String, String> findElementByText(List<Map<String, String>> src, String text) {
        for (Map<String, String> e : src) {
            if (e.get("text") != null) {
                if (e.get("text").contains(text)) {
                    logger.debug("findElementByText(" + text + ")=" + e);
                    return e;
                }
            }
        }
        logger.debug("findElementByText(" + text + ")=null");
        return null;
    }


    private static void putPortMapping(String device) throws IOException {
        cmd("adb -s " + device + " forward tcp:" + PORT + " tcp:" + PORT);
    }

    private static void openViewServer(String device) throws IOException {
        if (!cmd("adb -s " + device + " shell service call window 3").contains("00000001")) {
            //open ViewServer
            cmd("adb -s " + device + " shell service call window 1 i32 " + PORT);
        }
        logger.debug("openViewServer()");
    }

    private static void startServer() throws IOException {
        cmd("adb start-server");
        logger.debug("startServer()");
    }

    public static String getDevice() throws IOException {
        String[] devices = cmd("adb devices").split("\t")[0].split("\n");
        if (devices.length > 1){
            logger.debug("getDevice()=" + devices[1]);
            return devices[1];
        }
        return null;
    }

    private static List<String> winSource(String popupWinHandler) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", PORT), 40000);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
        String outCmd = "DUMP " + popupWinHandler;
        out.write(outCmd);
        out.newLine();
        out.flush();
        List<String> src = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null) {
            if ("DONE.".equalsIgnoreCase(line)) {
                break;
            }
            src.add(line);
        }
        socket.close();
        return src;
    }

    private static String getWinHashCodeByPartOfName(Map<String, String> activities, String winName) {
        String res = null;
        for (Map.Entry<String, String> e : activities.entrySet()) {
            if (e.getValue().contains(winName)) {
                res = e.getKey();
                break;
            }
        }
        logger.debug("getWinHashCodeByPartOfName('" + winName + "')=" + res);
        return res;
    }

    private static Map<String, String> getActivityMap() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", PORT), 40000);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
        out.write("LIST");
        out.newLine();
        out.flush();
        Map<String, String> res = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null) {
            if ("DONE.".equalsIgnoreCase(line)) {
                break;
            }
            res.put(line.split(" ")[0], line.split(" ")[1]);
        }
        socket.close();
        logger.debug("getActivityMap()=" + res);
        return res;
    }

    public static String cmd(String strCMD) throws IOException {
        String returnValue = "";
        String line;
        InputStream inStream;
        try {
            Process process = Runtime.getRuntime().exec(strCMD);
            inStream = process.getInputStream();
            BufferedReader brCleanUp = new BufferedReader(
                    new InputStreamReader(inStream));
            while ((line = brCleanUp.readLine()) != null) {
                returnValue = returnValue + line + "\n";
            }
            brCleanUp.close();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static String getWinSource(String winName) throws IOException {
        //get WindowHashCode by part of name
        String popupWinHashCode = getWinHashCode(winName);

        //Get Activity DUMP for current Window
        List<String> src = winSource(popupWinHashCode);

        //Parsing And Cutting ElementSource
        List<Map<String, String>> usefulElementsData = getUsefulElements(src);

        return xml(usefulElementsData);
    }

    private static String getWinHashCode(String winName) throws IOException {
        //start server
        startServer();

        //get all Android Devices
        String device = getDevice();

        //put Device in the 4939 port mapping to the 4939 port of the PC:
        putPortMapping(device);

        //open ViewServer services.
        openViewServer(device);

        //obtain the activities Activity
        Map<String, String> activities = getActivityMap();

        //get WindowHashCode by part of name
        String popupWinHashCode = getWinHashCodeByPartOfName(activities, winName);

        logger.debug("getWinHashCode(" + winName + ")=" + popupWinHashCode);
        return popupWinHashCode;
    }

    private static String xml(List<Map<String, String>> usefulElementsData) {
        StringBuilder xmlDoc = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><hierarchy rotation=\"0\">");
        for (Map<String, String> element : usefulElementsData) {
            int level = Integer.parseInt(element.get("level"));
            String className = element.get("class");
            String text = element.get("text");
            String id = element.get("id");
            String contentDesc = element.get("content-desc");
            String x = element.get("x");
            String y = element.get("y");
            String width = element.get("width");
            String height = element.get("height");
            xmlDoc.append(StringUtils.repeat("  ", level)).append("<").
                    append(className).append(" text=\"").
                    append(text).append("\" id=\"").
                    append(id).append("\" class=\"").append(className).
                    append("\" content-desc=\"").append(contentDesc).
                    append("\" bounds=\"[").append(x).append(",").
                    append(y).append("][").append(width).
                    append(",").append(height).append("]\"/>\n");
        }
        xmlDoc.append("</hierarchy>");
        return xmlDoc.toString();
    }

    public static void tap(Point p) {
        String strCMD = "adb shell input tap " + p.x + " " + p.y;
        try {
            Runtime.getRuntime().exec(strCMD);
        } catch (IOException e) {
            logger.error("tap(" + p + ") Error.");
            e.printStackTrace();
        }
    }

    public static void sendKeys(String text) {
        String strCMD = "adb shell input text " + text;
        try {
            Runtime.getRuntime().exec(strCMD);
            logger.debug("shell sendKeys: send(" + text + ") success.");
        } catch (IOException e) {
            logger.error("shell sendKeys: send(" + text + ") Error.");
            e.printStackTrace();
        }
    }
}
