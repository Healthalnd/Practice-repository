package com.qvc.selenium.plugin;

import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WebDriverManager {

    protected static WebDriverManager wdm;

    private static final Logger logger = Logger.getLogger(WebDriverManager.class.getName());
    private static final HashMap<String,String> androidLocalStartActivityMap = new HashMap<String, String>() {
        {
            put("de", "com.qvc.Home.HomePage");
        }

        {
            put("uk", "com.qvc.Home.HomePage");
        }

        {
        	put("us", "com.qvc.Home.HomePage");
        }
    };
    private static final HashMap<String,String> androidLocalAppPackageMap = new HashMap<String, String>() {
        {
            put("de", "de.qvc.qa");
        }

        {
            put("uk", "com.qvcuk.qa");
        }

        {
        	put("us", "com.qvc.qa");
        }
    };
    private WebDriverManager() {
    }

    public static WebDriverManager getSingletonManager() {
        if (null == wdm) {
            synchronized (WebDriverManager.class) {
                if (null == wdm) {
                    wdm = new WebDriverManager();
                }
            }
        }
        return wdm;
    }

    public RemoteWebDriver buildDriverInstance(String reqBrowser,
                                               String platform, Boolean dtEnabled, String grid, HashMap<String,String> options) {

        logger.debug("get driver with " + reqBrowser + " - " + platform + " - " + dtEnabled + " - " + grid);
        RemoteWebDriver driverInstance;
        DesiredCapabilities caps;

        org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
		proxy.setHttpProxy("wchwe21.qvc.com:80");
		proxy.setSslProxy("wchwe21.qvc.com:80");
		proxy.setFtpProxy("wchwe21.qvc.com:80");
		//TODO:Find better way to set vital proxy info than this shot in the dark.
		proxy.setNoProxy("csoew*,167.140.215*,lmctwb*,denpwl,167.140.28*,167.140.82*,167.140.33*,167.140.196*,167.140.155*,167.140.165*,10.50.1.26,167.140.218*,167.140.186*,167.140.236*,10.4.0.*,v*.qvc.com,v*.qvcdev.qvc.net,mail.qvc.com,qvc-intern.de,*web.qvc.com,nwms*,images-cs.qvc.com,167.140.186.*,z*.qvc.com,vismain*,vtd*,vital*,myqvc.com,MYQVC.NET,10.5.*,10.129*,score*,*.qvc.net,167.140.33.*,167.140.9.129,rmw*,10.100.1.18:1180,167.140.185*,cs.*.qvc.com,61.121.215.231,qvcjpa01.qvcjp.com,notes*.qvc.com,qsd*.qvc.com,172*,167.140.83*,csoe*,192.168.156.*,elearn*.qvc.com,10.9*,10.100.*,qvcproductsearch.com,10.2.5.212,10.2.5.215,10.175.82.*,172.22.24.*,eas.qvc.com,216.76.27.191,192.168.40.53,im.qvc.com,vpwf1093,localhost,127.0.0.1,int.qvc.com,qa.qvc.de,int.qvc.de,qa.qvcuk.com,int.qvcuk.com");


        // set the client
        if (reqBrowser.equalsIgnoreCase("ie")) {
            caps = DesiredCapabilities.internetExplorer();
            caps.setCapability(CapabilityType.PROXY, proxy);//add proxy for web browser
        } else if (reqBrowser.equalsIgnoreCase("chrome")) {
            caps = DesiredCapabilities.chrome();
            caps.setCapability(CapabilityType.PROXY, proxy);//add proxy for web browser
			//caps.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);//delete cache every time the browser instance is launched
        } else if (reqBrowser.equalsIgnoreCase("safari")) {
            caps = DesiredCapabilities.safari();
            caps.setCapability(CapabilityType.PROXY, proxy);//add proxy for web browser
            System.setProperty("webdriver.safari.noinstall", "true");
        } else if (reqBrowser.equalsIgnoreCase("opera")) {
            caps = DesiredCapabilities.opera();
            caps.setCapability(CapabilityType.PROXY, proxy);//add proxy for web browser
        } else if (reqBrowser.equalsIgnoreCase("iphone") || reqBrowser.equalsIgnoreCase("ipad")) {

            caps = new DesiredCapabilities();
            caps.setCapability("deviceName", options.get("deviceName"));
//            caps.setCapability("app", getAppPath(options.get("app"), options.get("env")));
            caps.setCapability("platformName", Platform.MAC);
            proxy = new org.openqa.selenium.Proxy();
            proxy.setNoProxy("localhost");
            caps.setCapability("proxy",proxy);
//            caps.setCapability("waitForAppScript",true);
            if (reqBrowser.equalsIgnoreCase("ipad")){
                caps.setCapability("platformVersion", "7.1");
            }
        } else if (reqBrowser.equalsIgnoreCase("android")) {
        	
        	 caps = new DesiredCapabilities();
             caps.setCapability("deviceName", options.get("deviceName"));
//           caps.setCapability("app", getAppPath(options.get("app"), options.get("env")));
             caps.setCapability("platformName", Platform.ANDROID);
             //caps.setCapability("applicationName", options.get("deviceName")); 'Use specific device to run
//             caps.setCapability("appPackage", "com.qvc.qa");//required - if we not set app path
             caps.setCapability("appPackage", (String) androidLocalAppPackageMap.get(options.get("ui").toLowerCase()));
                         
             String appActivity = (String) androidLocalStartActivityMap.get(options.get("ui").toLowerCase());
             if (appActivity!=null)
                 caps.setCapability("appActivity", appActivity);

        } else {
            //default to firefox
            logger.debug("In firefox the default client");

            // default to firefox
            caps = DesiredCapabilities.firefox();

            FirefoxProfile newProf = new FirefoxProfile();

            newProf.setPreference("intl.accept_languages", "en-us");
            
            newProf.setPreference(
                    "capability.policy.default.Window.pageXOffset.get",
                    "allAccess");
            newProf.setPreference(
                    "capability.policy.default.Window.pageYOffset.get",
                    "allAccess");
            newProf.setPreference(
                    "capability.policy.default.Window.mozInnerScreenX.get",
                    "allAccess");
            newProf.setPreference(
                    "capability.policy.default.Window.mozInnerScreenY.get",
                    "allAccess");
            newProf.setPreference(
                    "capability.policy.default.HTMLDocument.compatMode.get",
                    "allAccess");
            newProf.setPreference(
                    "network.proxy.type", 1);
            newProf.setPreference(
                    "network.proxy.http", "http://QVCVoicesProxyUser:Cali_999@qvcproxy.qvcdev.qvc.net:80");
            caps.setCapability(FirefoxDriver.PROFILE, newProf);
//            caps.setCapability(CapabilityType.PROXY, proxy);//add proxy for web browser

        }
        caps.setCapability("honorSystemProxy", true);
        
        // do not set proxy for mobile
        Boolean mobilePlatform = reqBrowser.equalsIgnoreCase("android") || reqBrowser.equalsIgnoreCase("iphone") || reqBrowser.equalsIgnoreCase("ipad");
        if (!mobilePlatform) {

            if (platform != null) {
                caps.setPlatform(Platform.valueOf(platform.toUpperCase()));
            } else {
                // defalut LINUX
                logger.debug("defaulting to platform LINUX");
                caps.setPlatform(Platform.LINUX);
            }
        }

        URL gridURL = null;

        try {
        	if(mobilePlatform)
        		gridURL = new URL(" http://" + grid + "/wd/hub");
        	else
        		gridURL = new URL(" http://" + grid + ":4444/wd/hub");
        } catch (MalformedURLException e) {

            // TODO Auto-generated catch block
            logger.error("Error creating url for grid", e);
        }

        logger.info(String.format("Creating driver %s %s", gridURL, caps));

        if (reqBrowser.equalsIgnoreCase("android")) {
            driverInstance = new QVCAndroidDriver(gridURL, caps);
        } else if (reqBrowser.equalsIgnoreCase("iphone") || reqBrowser.equalsIgnoreCase("ipad")) {
            driverInstance = new QVCiOSDriver(gridURL, caps);
        } else{
            driverInstance = new RemoteWebDriver(gridURL, caps);
			driverInstance.manage().deleteAllCookies();
		}
        // http://ecombase7016c.qvcdev.qvc.net:4444/wd/hub

        if (!mobilePlatform){
            driverInstance.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            driverInstance.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
//			Since you're running tests in a headless mode, there are no active browser window available, the driver doesn't know which window to maximize - comment it	 Simon, 02/24/2018		
//          driverInstance.manage().window().maximize();
            driverInstance.manage().window().setSize(new Dimension(1360,1020)); //dimensions of the linux machine 1360X1020
			driverInstance.manage().deleteAllCookies();
            //add "TakeScreenShot" to control screenshot rule - Web: ForError 
            System.setProperty("TakeScreenShot", "ForError");
        }else //Mobile: Always (For each steps)
            System.setProperty("TakeScreenShot", "Always");

        logger.debug("driver created");
        return driverInstance;
        
    }
    
    
    public static String getAppPath(String appName, String env) {
        String mobBuildsDir = System.getProperty("MobBuildsDir");
        if (mobBuildsDir == null || mobBuildsDir.isEmpty())
            return appName;

        return Paths.get(mobBuildsDir, env, appName).toString();
    }
}
