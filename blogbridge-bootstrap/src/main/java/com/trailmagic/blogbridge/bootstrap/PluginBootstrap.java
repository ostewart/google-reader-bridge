package com.trailmagic.blogbridge.bootstrap;

import com.salas.bb.plugins.domain.ICodePlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


/**
 * Created by: oliver on Date: Jan 3, 2010 Time: 8:19:38 PM
 */
public class PluginBootstrap implements ICodePlugin {
    private ClassLoader loader;
    private String googleUser;


    public void setPackageLoader(ClassLoader classLoader) {
        URL jarLocation = PluginBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("Jar location: " + jarLocation);

        List<URL> classPathUrls = new ArrayList<URL>();
        try {
            File rootFile = new File(jarLocation.toURI());
            if (rootFile.isDirectory()) {
                String libPath = rootFile.getPath() + File.separator + "lib";
                System.out.println("libpath: " + libPath);
                File libDir = new File(libPath);
                File[] libs = libDir.listFiles();

                for (File lib : libs) {
                    if (lib.exists() && !lib.isDirectory()) {
                        classPathUrls.add(lib.toURI().toURL());
                        System.out.println("added: " + lib.toURI().toURL());
                    }
                }
            } else {
                if (rootFile.exists()) {
                    ZipFile rootZip = new ZipFile(rootFile);
                    Enumeration<? extends ZipEntry> entries = rootZip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        if (!entry.isDirectory() && entry.getName().startsWith("lib/")) {
                            System.out.println("Found entry: " + entry.getName());
                            //                            log.debug("Found entry: {}", entry.getName());
                            URL newUrl = new URL(jarLocation + "!/" + entry.getName());
                            System.out.println("adding url: " + newUrl);
                            classPathUrls.add(newUrl);
                        }
                    }
                }
            }
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            loader = new URLClassLoader(classPathUrls.toArray(new URL[]{}), classLoader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> pluginClass = loader.loadClass("com.trailmagic.blogbridge.googlereader.GoogleReaderBridgePlugin");
            Object plugin = pluginClass.newInstance();
            Method method = pluginClass.getMethod("loadPlugin");
            method.invoke(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParameters(Map<String, String> stringStringMap) {
        googleUser = stringStringMap.get("googlereader.user");
    }

    public String getTypeName() {
        return null;
    }
}
