/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.tools.maven.plugins.du;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import javax.slee.ServiceID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.mobicents.slee.tools.maven.plugins.du.ant.DeployConfig;
import org.mobicents.slee.tools.maven.plugins.du.ant.RAEntity;
import org.mobicents.slee.tools.maven.plugins.du.ant.ServiceIds;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Generates a Ant script, which can be used to deploy/undeploy JAIN SLEE
 * Deployable Units.
 */
public class AntManagementScriptMojo extends AbstractMojo {

	/**
	 * The location of the deploy-config.xml file, which is used by Mobicents
	 * JAIN SLEE to create RA entities and links.
	 */
	private File deployConfigFile;

	/**
	 * Name of the generated script file.
	 * 
	 */
	private String scriptFileName;

	/**
	 * Name of the DU jar file.
	 * 
	 */
	private String duFileName;

	/**
	 * Directory to be used as the source for SLEE service xml descriptors.
	 * 
	 */
	private File serviceInputDirectory;

	/**
	 * Directory to be used as the output for the generated ant management
	 * script.
	 * 
	 */
	private File outputDirectory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException {

		if (getLog().isDebugEnabled()) {
			getLog().debug("Collecting SLEE service descriptors...");
		}
		Set<String> services = collectFiles(serviceInputDirectory, ".xml");

		// generate the xml
		String xml = generateManagementAntScript(services);

		File file = new File(outputDirectory, scriptFileName);

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(xml);
			getLog().info("Ant management script generated with success.");
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating file " + file, e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private String generateManagementAntScript(Set<String> services)
			throws MojoExecutionException {

		getLog().info("Generating ant script for management without maven...");

		// read header and footer
		String header = "";
		String footer = "";
		LinkedList<String> deployElements = new LinkedList<String>();
		LinkedList<String> undeployElements = new LinkedList<String>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("build.header")));
			String str = "";
			while ((str = in.readLine()) != null) {
				header += str + "\r\n";
			}

			in = new BufferedReader(new InputStreamReader(this.getClass()
					.getResourceAsStream("build.footer")));

			while ((str = in.readLine()) != null) {
				footer += str + "\r\n";
			}
		} catch (IOException e) {
			throw new MojoExecutionException(
					"failed to read header and footer of build.xml file", e);
		}

		// now lets glue everything and write the build.xml script
		String xml = header + "\r\n\t<property name=\"du.filename\" value=\""
				+ duFileName + "\" />\r\n";
		xml += footer;

		return xml;
	}

	private Set<String> collectFiles(File inputDirectory, String suffix) {

		if (getLog().isDebugEnabled()) {
			getLog().debug(
					"Collecting non hidden files with " + suffix
							+ " name suffix from directory "
							+ inputDirectory.getAbsolutePath());
		}

		if (inputDirectory == null || !inputDirectory.exists()
				|| !inputDirectory.isDirectory()) {
			return Collections.emptySet();
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug(
					"Directory " + inputDirectory.getAbsolutePath()
							+ " successfully validated.");
		}

		Set<String> result = new HashSet<String>();

		for (File f : inputDirectory.listFiles()) {
			if (f.isDirectory() || f.isHidden()
					|| !f.getName().endsWith(suffix)) {
				continue;
			} else {
				if (getLog().isDebugEnabled()) {
					getLog().debug("Collecting file " + f.getName());
				}
				result.add(f.getName());
			}
		}

		return result;
	}
}
