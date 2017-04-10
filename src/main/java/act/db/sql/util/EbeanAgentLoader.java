package act.db.sql.util;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;
import org.avaje.agentloader.AgentLoader;
import sun.tools.attach.BsdVirtualMachine;
import sun.tools.attach.LinuxVirtualMachine;
import sun.tools.attach.SolarisVirtualMachine;
import sun.tools.attach.WindowsVirtualMachine;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EbeanAgentLoader extends AgentLoader {


    private static final Logger log = Logger.getLogger(AgentLoader.class.getName());

    private static final List<String> loaded = new ArrayList<String>();

    private static final String discoverPid() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        return nameOfRunningVM.substring(0, p);
    }

    private static final AttachProvider ATTACH_PROVIDER = new AttachProvider() {
        @Override
        public String name() {
            return null;
        }

        @Override
        public String type() {
            return null;
        }

        @Override
        public VirtualMachine attachVirtualMachine(String id) {
            return null;
        }

        @Override
        public List<VirtualMachineDescriptor> listVirtualMachines() {
            return null;
        }
    };

    /**
     * Load an agent providing the full file path.
     */
    public static void loadAgent(String jarFilePath) {
        loadAgent(jarFilePath, "");
    }

    /**
     * Load an agent providing the full file path with parameters.
     */
    public static void loadAgent(String jarFilePath, String params) {

        log.info("dynamically loading javaagent for " + jarFilePath);
        try {

            String pid = discoverPid();

            VirtualMachine vm;
            if (AttachProvider.providers().isEmpty()) {
                vm = getVirtualMachineImplementationFromEmbeddedOnes(pid);
            } else {
                vm = VirtualMachine.attach(pid);
            }

            vm.loadAgent(jarFilePath, params);
            vm.detach();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the agent from the classpath using its name.
     */
    public static void loadAgentFromClasspath(String agentName) {
        loadAgentFromClasspath(agentName, "");
    }

    /**
     * Load the agent from the classpath using its name and passing params.
     */
    public synchronized static boolean loadAgentFromClasspath(String agentName, String params) {

        if (loaded.contains(agentName)) {
            // the agent is already loaded
            return true;
        }
        try {
            // Search for the agent jar in the classpath
            ClassLoader cl0 = AgentLoader.class.getClassLoader();
            if (!(cl0 instanceof URLClassLoader)) {
                cl0 = cl0.getParent();
            }
            if (cl0 instanceof URLClassLoader) {
                URLClassLoader cl = (URLClassLoader) (cl0);
                for (URL url : cl.getURLs()) {
                    if (isMatch(url, agentName)) {
                        // We have found the agent jar in the classpath
                        String fullName = url.toURI().getPath();
                        if (fullName.startsWith("/") && isWindows()) {
                            fullName = fullName.substring(1);
                        }
                        loadAgent(fullName, params);
                        loaded.add(fullName);
                        return true;
                    }
                }
            }

            // Agent not found and not loaded
            return false;

        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    /**
     * Check to see if this url/jar matches our agent name.
     */
    private static boolean isMatch(URL url, String partial) {
        String fullPath = url.getFile();
        int lastSlash = fullPath.lastIndexOf('/');
        if (lastSlash < 0) {
            return false;
        }
        String jarName = fullPath.substring(lastSlash + 1);
        // Use startsWith so ignoring the version of the agent
        return jarName.startsWith(partial);
    }

    private static final boolean isWindows() {
        return File.separatorChar == '\\';
    }

    private static VirtualMachine getVirtualMachineImplementationFromEmbeddedOnes(String pid) {
        try {
            if (isWindows()) {
                return new WindowsVirtualMachine(ATTACH_PROVIDER, pid);
            }

            String osName = System.getProperty("os.name");

            if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
                return new LinuxVirtualMachine(ATTACH_PROVIDER, pid);

            } else if (osName.startsWith("Mac OS X")) {
                return new BsdVirtualMachine(ATTACH_PROVIDER, pid);

            } else if (osName.startsWith("Solaris")) {
                return new SolarisVirtualMachine(ATTACH_PROVIDER, pid);
            }

        } catch (AttachNotSupportedException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException("Native library for Attach API not available in this JRE", e);
        }

        return null;
    }

}
