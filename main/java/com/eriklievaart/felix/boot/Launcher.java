package com.eriklievaart.felix.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class Launcher {

	private File root;
	private File bundleDir;

	public Launcher(File root) {
		this.root = root;
		this.bundleDir = new File(root, "bundle");
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
		System.out.println("========================");
		System.out.println("== initializing felix ==");
		System.out.println("========================");
		System.out.println("root dir: " + root.getAbsolutePath());
		System.out.println("bundle dir: " + bundleDir.getAbsolutePath());
	}

	private Hashtable<String, String> createProperties() {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("org.osgi.framework.storage.clean", "onFirstInit");
		properties.put("org.osgi.service.http.port", "8000");
		properties.put("felix.log.level", "2");
		properties.put("felix.cache.rootdir", root.getAbsolutePath());
		return properties;
	}

	private void installBundles(Framework framework) throws Exception {
		System.out.println("installing bundles");
		BundleContext context = framework.getBundleContext();
		for (File file : bundleDir.listFiles()) {
			if (file.getName().endsWith(".jar")) {
				System.out.println("installing bundle: " + file);
				Bundle bundle = context.installBundle("file:" + file.getCanonicalPath());
				bundle.start();
			}
		}
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