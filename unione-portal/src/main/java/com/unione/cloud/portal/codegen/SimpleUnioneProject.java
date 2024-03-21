package com.unione.cloud.portal.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.gen.simple.SimpleMavenProject;

public class SimpleUnioneProject extends SimpleMavenProject {
	
	private String basePackage = null;
	
	
	public SimpleUnioneProject() {
		super();
		this.basePackage="com.unione.cloud.project";
	}

	public SimpleUnioneProject(String basePackage) {
		super(basePackage);
		this.basePackage=basePackage;
	}

	
	@Override
	public String getBasePackage(String sourceBuilerName){
		if(StringUtils.isEmpty(sourceBuilerName)) {
			return basePackage;
		}
		return basePackage+"."+sourceBuilerName;
	}
	
	@Override
	public Writer getWriterByName(String sourceBuilderName, String targetName) {
		FileWriter writer = null;
		if(sourceBuilderName.startsWith("resources.")){
			StringBuffer src=new StringBuffer();
			src.append(this.root).append(File.separator).append("src/main/");
			String tt[]=sourceBuilderName.split("\\.");
			for(int i=0;i<tt.length;i++) {
				src.append(tt[i]).append(File.separator);
			}
			src.append(targetName);
			
			String output = src.toString();
			try {
				checkFile(output);
				writer = new FileWriter(new File(output));
			} catch (IOException e) {
				throw new IllegalArgumentException(output);
			}
		}else{

			String src = this.root+ File.separator+"src/main/java";
			String pkg = getBasePackage(sourceBuilderName);
			String subPath = pkg.replace('.',File.separatorChar);
			String output = src+File.separator+subPath+File.separator+targetName;

			try {
				checkFile(output);
				writer = new FileWriter(new File(output));
			} catch (IOException e) {
				throw new IllegalArgumentException(output,e);
			}

		}
		return writer;
	}

}
