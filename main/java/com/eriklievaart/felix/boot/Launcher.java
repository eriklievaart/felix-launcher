package com.eriklievaart.felix.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class Launcher {
	private static final String HOME = System.getProperty("user.home");

	private File root;
	private File bundleDir;
	private File configFile;

	public Launcher(File root) {
		this.root = root;
		this.bundleDir = new File(root, "bundle");
		this.configFile = findConfigFile();
		if (!bundleDir.isDirectory()) {
			throw new Error("directory does not exist " + bundleDir);
		}
	}

	private File findConfigFile() {
		File file = new File(root, "osgi.properties");
		if (file.exists()) {
			return file;
		} else {
			return new File(root, "dev-osgi.properties");
		}
	}

	public void start() throws Exception {
		try {
			printHeader();

			Framework framework = getFrameworkFactory().newFramework(createProperties());
			framework.init();
			installBundles(framework);
			framework.start();
			framework.waitForStop(0);
			System.exit(0);

		} catch (Exception ex) {
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private void printHeader() {
		System.out.println();
		System.out.println("========================");
		System.out.println("== initializing felix ==");
		System.out.println("========================");
		System.out.println("root dir: " + root.getAbsolutePath());
		System.out.println("bundle dir: " + bundleDir.getAbsolutePath());
		System.out.println("property file: " + configFile.getAbsolutePath());
	}

	private Hashtable<String, String> createProperties() {
		Hashtable<String, String> properties = defaultProperties();
		loadConfigFile().forEach((k, v) -> {
			properties.put(k.toString().trim(), resolveSystemProperties(v.toString().trim()));
		});
		printProperties(properties);
		return properties;
	}

	private String resolveSystemProperties(String value) {
		return value.replace("${project}", root.getName()).replace("${user.home}", HOME);
	}

	private void printProperties(Hashtable<String, String> properties) {
		System.out.println();
		System.out.println("== felix properties ==");
		new TreeMap<>(properties).forEach((k, v) -> {
			if (k.toLowerCase().contains("password")) {
				System.out.println(k + " = ????????");
			} else {
				System.out.println(k + " = " + v);
			}
		});
		System.out.println();
	}

	private Properties loadConfigFile() {
		try {
			Properties local = new Properties();
			if (configFile.isFile()) {
				local.load(new FileReader(configFile));
			}
			return local;

		} catch (IOException e) {
			return new Properties();
		}
	}

	private Hashtable<String, String> defaultProperties() {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("org.osgi.framework.storage.clean", "onFirstInit");
		properties.put("org.osgi.service.http.port", "8000");
		properties.put("felix.log.level", "3");
		properties.put("felix.cache.rootdir", root.getAbsolutePath());
		properties.put("org.apache.felix.http.debug", "true");
		return properties;
	}

	private void installBundles(Framework framework) throws Exception {
		BundleContext context = framework.getBundleContext();
		if (bundleDir.listFiles().length == 0) {
			System.out.println("no bundles found in " + bundleDir);
			System.exit(50);
		}
		for (File file : getBundles()) {
			System.out.println("installing bundle: " + file);
			Bundle bundle = context.installBundle("file:" + file.getCanonicalPath());
			bundle.start();
			if (bundle.getSymbolicName() == null) {
				throw new RuntimeException("bundle must be assigned a name: " + file);
			}
		}
	}

	private List<File> getBundles() {
		List<File> files = new ArrayList<>();
		for (File file : bundleDir.listFiles()) {
			if (file.getName().endsWith(".jar") && !file.getName().endsWith("-src.jar")) {
				files.add(file);
			}
		}
		sortVersionedBundlesFirst(files);
		return files;
	}

	static void sortVersionedBundlesFirst(List<File> files) {
		Collections.sort(files, (a, b) -> {
			if (hasVersionNumber(a.getName()) && !hasVersionNumber(b.getName())) {
				return -1000;
			}
			if (hasVersionNumber(b.getName()) && !hasVersionNumber(a.getName())) {
				return 1000;
			}
			return a.getName().compareTo(b.getName());
		});
	}

	static boolean hasVersionNumber(String name) {
		return name.matches(".*-[0-9.]++jar");
	}

	private FrameworkFactory getFrameworkFactory() throws Exception {
		String resource = "META-INF/services/org.osgi.framework.launch.FrameworkFactory";
		java.net.URL url = Main.class.getClassLoader().getResource(resource);
		if (url != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();
					// Try to load first non-empty, non-commented line.
					if (s.length() > 0 && s.charAt(0) != '#') {
						return (FrameworkFactory) Class.forName(s).newInstance();
					}
				}
			}
		}
		throw new Exception("Could not find framework factory.");
	}
}
