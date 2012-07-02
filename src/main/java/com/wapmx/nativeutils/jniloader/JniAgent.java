package com.wapmx.nativeutils.jniloader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wiehann Matthysen
 */
public class JniAgent {
    
    private static boolean debug = false;
    
    static {
        String s = System.getProperty("java.library.debug");
        if(s != null && (s.toLowerCase().startsWith("y") || s.startsWith("1")))
            debug = true;
    }
    
    private static class CommandLineArgs {
        
        @Parameter(names = {"-load", "-l"}, description = "Native libraries to extract and load from temporary folder.")
        private List<String> loadList = new ArrayList<String>();

        @Parameter(names = {"-extract", "-e"}, description = "Native libraries to extract to temporary folder.")
        private List<String> extractList = new ArrayList<String>();
    }
    
    public static void premain(String args, Instrumentation instrumentation) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander commander = new JCommander(commandLineArgs);
        
        try {
            if (debug) {
                if (args != null) {
                    System.out.format("Agent arguments: %s%n", args);
                } else {
                    System.out.format("No agent arguments provided.%n");
                }
            }
            
            if (args != null) {
                commander.parse(args.split(" "));
            }
            
            List<String> loadList = commandLineArgs.loadList;
            List<String> extractList = commandLineArgs.extractList;
            
            if (debug) {
                System.out.format("Load list: %s%n",loadList);
                System.out.format("Extract list: %s%n", extractList);
            }
            
            for (String library : loadList) {
                try {
                    NativeLoader.loadLibrary(library);
                } catch (IOException exception) {
                    System.err.format("Error loading library '%s'.%n", library);
                    System.err.println(exception);
                    System.exit(1);
                }
            }
            
            for (String library: extractList) {
                try {
                    NativeLoader.getJniExtractor().extractJni(library);
                } catch (IOException exception) {
                    System.err.format("Error extracting library '%s'.%n", library);
                    System.err.println(exception);
                    System.exit(1);
                }
            }
        } catch (ParameterException exception) {
            System.err.format("Error parsing parameters '%s'.%n", args);
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }
}
