package com.qvc.selenium.plugin;

import com.qvc.selenium.data.TestManager;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link SeleniumFramework} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #frameworkJAR})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Cheryl Foster
 */
public class SeleniumFramework extends Builder {

    private final String grid;
    private final String suite;
    private final String testPackage;
    private final String logLevel;
    private final String rerun;
	private final String env;
    private final String country;
    private final String os;
    private final String client;
    private final String deviceName;
    private final String mobBuildsDir;
    private final String threadCount;


	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public SeleniumFramework(String grid, String suite, String testPackage, String env, String threadCount, String rerun, String logLevel,  String country, String os, String client, String deviceName, String mobBuildsDir) {
        this.grid = grid;
        this.suite = suite;
        this.testPackage = testPackage;
        this.rerun = rerun;
        this.logLevel = logLevel;
        this.env = env;
        this.threadCount = threadCount;

        // filters
        this.country = country;
        this.os = os;
        this.client = client;
        this.deviceName = deviceName;

        this.mobBuildsDir = mobBuildsDir;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */


    public String getGrid() {
        return grid;
    }

	public String getSuite() {
		return suite;
	}

	public String getTestPackage() {
		return testPackage;
	}
	
	public String getThreadCount() {
		return threadCount;
	}
	
    public String getLogLevel() {
        return logLevel;
    }
    
    public String getRerun() {
		return rerun;
	}

    public String getEnv(){
        return  env;
    }

    public String getCountry(){
        return  country;
    }

    public String getOs(){
        return  os;
    }

    public String getClient(){
        return  client;
    }

    public String getDeviceName(){
        return  deviceName;
    }

    public String getMobBuildsDir(){
        return  mobBuildsDir;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException  {

        PrintStream stdout = System.out;
        PrintStream stderr = System.err;

        // Redirect output to job console
        System.setOut(listener.getLogger());
        System.setErr(listener.getLogger());

    	listener.getLogger().println("[Selenium Framework Tester] Start");
     	listener.getLogger().println("[Selenium Framework Tester] Getting Global Configuration");
         	
    	String frameworkJAR = "I NEED TO BE SET TO SOMETHING!";
    	
    	if(getDescriptor().getAnotherJAR()){   		
    		frameworkJAR = getDescriptor().getFrameworkJAR();
    	}
    	else{
    		frameworkJAR = "default";
    	}
    	
    	//if bundledJAR is true, override!!!
    	if(!frameworkJAR.equalsIgnoreCase("default") && getDescriptor().getBundledJAR()){
    		frameworkJAR = "default";
    	}

        // Set reportPath, rerun in System properties
        setSystemProperties();

        TestManager.clearCache();

    	//call the runner
        TestSuiteRunner localTestSuiteRunner = new TestSuiteRunner(
                build,
                listener,
                frameworkJAR,
                grid,
                suite,
                testPackage,
                threadCount,
                rerun,
                logLevel,
                env,
                country,
                os,
                client,
                deviceName
        );
    	boolean ret = launcher.getChannel().call(localTestSuiteRunner);
        localTestSuiteRunner.cleanUpLogger();
        launcher.getChannel().close();

    	listener.getLogger().println("[Selenium Framework Tester] Done");

        // Restore output
        System.setOut(stdout);
        System.setErr(stderr);

        return ret;
    }

    private void setSystemProperties() {
        System.setProperty("reportRootPath", "testOutput");
        System.setProperty("rerun", rerun);
        System.setProperty("MobBuildsDir", mobBuildsDir);
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    /**
     * Descriptor for {@link SeleniumFramework}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/com/qvc/jenkins/plugin/SeleniumFramework/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String frameworkJAR;
        private boolean bundledJAR;
        private boolean anotherJAR;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'frameworkJAR'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         
        public FormValidation doCheckFrameworkJAR(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set path to Framework JAR");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the Framework JAR too short?");
            return FormValidation.ok();
        }
         */
        
        
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable frameworkJAR is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Execute QVC Selenium Framework Test(s)";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            
        	frameworkJAR = formData.getString("frameworkJAR");
        	anotherJAR = formData.getBoolean("anotherJAR");
        	bundledJAR = formData.getBoolean("bundledJAR");
        	
        	//TODO override for now... need to clean this up
        	anotherJAR = false;
        	bundledJAR = true;

            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns the value of the frameworkJAR
         */
        public String getFrameworkJAR() {
            return frameworkJAR;
        }

		public boolean getBundledJAR(){
			return bundledJAR;
		}
        
		public boolean getAnotherJAR(){
			return anotherJAR;
		}
    }
}

