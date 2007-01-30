/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xalan.xsltc.cmdline.Compile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.xsltc.compiler.XSLTC;

/**
 * This utility pre-compiles xsl files into xsltc translets. The final location
 * of the translets will be <class path of xsl>/<translet-package-name>.
 * This is useful if you wanted to pre-compile all the stylesheets prior
 * to starting the portlet, for example.
 * The main method takes two arguements.
 *     1) The base directory containing stylesheets to pre-compile.
 *     2) The directory where the translets should be build to.
 */
public class BuildXsltcTranslets {

    private static final Log log = LogFactory.getLog(BuildXsltcTranslets.class);
    
    private File sourceDir;
    private File buildDir;
    private String packageName;
    private boolean inlining;
    private SslFilter sslFilter = new SslFilter();
    private DirFilter dirFilter = new DirFilter();
    private String[] buildDirArgs = {"-d", null, "-p", "translets", "-x" };
    private static int DIRECTORY_POS = 1;
    private static int PACKAGE_NAME_POS = 3;
    private static String FILE_SEP = System.getProperty("file.separator");
    private static boolean debug =
        Boolean.getBoolean("BuildXsltcTranslets.debug");
    
    private Pattern hrefPattern = Pattern.compile("href=\"(.+?)\"");

    public BuildXsltcTranslets(String sourceDirName, String buildDirName, String packageName, boolean inlining) throws Exception {
        this(sourceDirName, buildDirName, packageName, inlining, true);
    }
    
    public BuildXsltcTranslets(String sourceDirName, String buildDirName, String packageName, boolean inlining, boolean buildDirectory) throws Exception {
        this.sourceDir = new File(sourceDirName);
        this.buildDir = new File(buildDirName);
        this.packageName = packageName;
        this.inlining = inlining;

        buildDirArgs[PACKAGE_NAME_POS] = packageName;

        if (!sourceDir.exists()) {
            throw new RuntimeException("Source directory doesn't exist: " + sourceDirName);
        }
        
        buildDir.mkdirs();

        if (buildDirectory) {
            traverseFileSystem(sourceDir);
        }
    }
    
    private void traverseFileSystem(File dir) throws Exception {
        
        if (debug) {
            System.out.println("Traversing dir: " + dir.getAbsolutePath());
        }

        String[] sslFiles = dir.list(sslFilter);

        if (debug) {
            System.out.println("ssl file count: " + sslFiles.length);
        }

        if (sslFiles.length > 0) {
            String[] xslFiles = parseMainXslFiles(dir, sslFiles);
            buildDir(dir, xslFiles);
        }
        File[] subDirs = dir.listFiles(dirFilter);
        for (int i=0; i<subDirs.length; i++) {
            traverseFileSystem(subDirs[i]);
        }
    }
    
    private String[] parseMainXslFiles(File dir, String[] sslFiles)
    throws Exception {
        
        Set s = new HashSet();
        String content = null;
        Matcher m = null;
        String sourceFileName;
        File f;
        
        for (int i=0; i<sslFiles.length; i++) {
            if (debug) {
                System.out.println("loading ssl file: " + sslFiles[i]);
            }

            sourceFileName = new StringBuffer(dir.getAbsolutePath()).
                append(FILE_SEP).append(sslFiles[i]).toString();
            content = loadFile(sourceFileName);
            m = hrefPattern.matcher(content);
            while (m.find()) {
                f = new File(m.group(1));
                
                // only add if the name is the same as the path,
                // which means that the ssl file is specifying
                // an xsl file that's in the same directory as itself.
                // Some ssl files give relative path pointing to
                // xsl files in other directories. We just want to omit those.
                if (f.getPath().equals(f.getName())) {
                    if (debug) {
                        System.out.println("Adding xsl: " + f.getName());
                    }
                    s.add(f.getName());
                }
            }
        }
        
        return (String[])s.toArray(new String[s.size()]);
    }
    
    private String loadFile(String filename) throws Exception {
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[1024];
        int numRead;
        
        FileReader reader = new FileReader(filename);
        
        while ((numRead = reader.read(buf, 0, 1024)) >= 0) {
            sb.append(buf, 0, numRead);
        }
        reader.close();
        
        return sb.toString();
    }

    public void buildFile(File targetDir, String xslFile) throws Exception {
        if (targetDir == null) {
            throw new RuntimeException("BuildXsltcTranslets::buildFile : null targetDir file");
        }
        if (xslFile == null) {
            throw new RuntimeException("BuildXsltcTranslets::buildFile : null xsl file");
        }
        
        File transletDir = new File(targetDir, packageName);
        transletDir.mkdirs();

        String [] stylesheets = { new File(targetDir, xslFile).getAbsolutePath() };
        compile(stylesheets, targetDir.getAbsolutePath());
    }
    
    public void buildDir(File dir, String[] xslFiles) throws Exception {

        List fileArgs = new ArrayList();
        String sourceFileName;
        String targetFileName;
        File sourceFile;
        File targetFile;
        File targetDir = new File(buildDir + dir.getAbsolutePath().substring(sourceDir.getAbsolutePath().length()));
        targetDir.mkdirs();

        for (int i=0; i<xslFiles.length; i++) {
            String baseXslName = xslFiles[i].substring(0, xslFiles[i].indexOf('.'));

            sourceFileName = new StringBuffer(dir.getAbsolutePath()).
                append(FILE_SEP).append(xslFiles[i]).toString();
            targetFileName = new StringBuffer(targetDir.getAbsolutePath()).
                append(FILE_SEP).append(buildDirArgs[PACKAGE_NAME_POS]).
                append(FILE_SEP).append(baseXslName.replaceAll("-", "_")).
                append(".class").toString();
            sourceFile = new File(sourceFileName);
            targetFile = new File(targetFileName);
            if (targetFile.exists()) {
                if (sourceFile.lastModified() > targetFile.lastModified()) {
                    fileArgs.add(sourceFileName);
                }
            } else {
                fileArgs.add(sourceFileName);
            }
        }

        if (!fileArgs.isEmpty()) {
            String [] args = new String[buildDirArgs.length+fileArgs.size()];
            System.arraycopy(buildDirArgs, 0, args, 0, buildDirArgs.length);
            args[DIRECTORY_POS] = targetDir.getAbsolutePath();
            System.arraycopy((String[])fileArgs.toArray(new String[fileArgs.size()]), 0, args, buildDirArgs.length, fileArgs.size());

            System.err.print("org.apache.xalan.xsltc.cmdline.Compile ");
            for (int i=0; i<args.length; i++) {
                System.err.print(args[i] + " ");
            }
            System.err.println();
            System.err.flush();
            Compile.main(args);
        }
    }

    private void compile(String[] stylesheets, String dest) throws MalformedURLException{
        final XSLTC xsltc = new XSLTC();
        xsltc.init();
        xsltc.setDestDirectory(dest);
        xsltc.setPackageName(packageName);
        xsltc.setDebug(true);
        xsltc.setTemplateInlining(inlining);

        boolean compileOK;

        final Vector   stylesheetVector = new Vector();
        for (int i = 0; i < stylesheets.length; i++) {
            final String name = stylesheets[i];
            URL url = new File(name).toURL();
            stylesheetVector.addElement(url);
        }
        compileOK = xsltc.compile(stylesheetVector);

        if (log.isWarnEnabled()) {
            Vector warnings = xsltc.getWarnings();
            for (int i=0; i<warnings.size(); i++) {
                log.warn("BuildXsltcTranslets::compile : " + warnings.get(i));
            }
        }
        if (!compileOK) {
            Vector errors = xsltc.getErrors();
            for (int i=0; i<errors.size(); i++) {
                log.error("BuildXsltcTranslets::compile : " + errors.get(i));
            }
        }
    }

    public static void main(String args[]) {
        if (args.length < 3) {
            System.out.println("Usage: BuildXsltcTranslets <stylesheet-source-dir> <build-home-dir> <translet-package-name> [-n]");
            System.exit(-1);
        }

        try {
            boolean inlining = args.length >= 4 && "-n".equals(args[3]);
            new BuildXsltcTranslets(args[0], args[1], args[2], inlining);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
    
    public class SslFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.matches("^.*\\.ssl$");
        }
    }
    
    public class DirFilter implements FileFilter {
        public boolean accept(File dir) {
            return dir.isDirectory();
        }
    }
}
