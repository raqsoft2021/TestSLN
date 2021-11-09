package com.raqsoft.lib.zip.function;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;  
import java.util.Collections;  
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raqsoft.common.Logger;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;  
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;  
 
/** 
* ZIPѹ���ļ����������� 
*/  
public class ImZipUtil {  
     
	/** 
    * ��ѹָ����ZIPѹ���ļ�����ǰĿ¼
    * @param zip ָ����ZIPѹ���ļ� 
    * @return  ��ѹ���ļ����� 
    */  
   public static File [] unzip(String zip) throws ZipException {  
       return unzip(zip, null);  
   }
   
	/** 
    * ʹ�ø��������ѹָ����ZIPѹ���ļ�����ǰĿ¼ 
    * @param zip ָ����ZIPѹ���ļ� 
    * @param passwd ZIP�ļ������� 
    * @return  ��ѹ���ļ����� 
    * @throws ZipException ѹ���ļ����𻵻��߽�ѹ��ʧ���׳� 
    */  
   public static File [] unzip(String zip, String passwd) throws ZipException {  
       File zipFile = new File(zip);  
       File parentDir = zipFile.getParentFile();  
       return unzip(zipFile, parentDir.getAbsolutePath(), passwd, null);  
   }
   	   
   /** 
    * ʹ�ø��������ѹָ����ZIPѹ���ļ���ָ��Ŀ¼ 
    *  
    * ���ָ��Ŀ¼������,�����Զ�����,���Ϸ���·���������쳣���׳� 
    * @param zip ָ����ZIPѹ���ļ� 
    * @param dest ��ѹĿ¼ 
    * @param passwd ZIP�ļ������� 
    * @return ��ѹ���ļ����� 
    * @throws ZipException ѹ���ļ����𻵻��߽�ѹ��ʧ���׳� 
    */  
   public static File [] unzip(String zip, String dest, String passwd) throws ZipException {  
       File zipFile = new File(zip);  
       return unzip(zipFile, dest, passwd,null);  
   }  
     
        
   /** 
    * ʹ�ø��������ѹָ����ZIPѹ���ļ���ָ��Ŀ¼ 
    *  
    * ���ָ��Ŀ¼������,�����Զ�����,���Ϸ���·���������쳣���׳� 
    * @param zip ָ����ZIPѹ���ļ� 
    * @param dest ��ѹĿ¼ 
    * @param passwd ZIP�ļ������� 
    * @param charsetName ���õı���
    * @return  ��ѹ���ļ����� 
    * @throws ZipException ѹ���ļ����𻵻��߽�ѹ��ʧ���׳� 
    */  
   public static File [] unzip(File zipFile, String dest, String passwd, String charsetName) throws ZipException {  
       ZipFile zFile = new ZipFile(zipFile);  
	   setZipParam(zFile, passwd, charsetName);
		  
       File destDir = new File(dest);  
       if (destDir.isDirectory() && !destDir.exists()) {  
           destDir.mkdir();  
       }  
       zFile.extractAll(dest);  
         
       List<FileHeader> headerList = zFile.getFileHeaders();  
       List<File> extractedFileList = new ArrayList<File>();  
       for(FileHeader fileHeader : headerList) {  
           if (!fileHeader.isDirectory()) {  
               extractedFileList.add(new File(destDir,fileHeader.getFileName()));  
           }  
       }  
       File [] extractedFiles = new File[extractedFileList.size()];  
       extractedFileList.toArray(extractedFiles);  
       return extractedFiles;  
   }  
   
   //��ѹ��ָ��Ŀ¼
   public static File [] unzip( ZipFile zFile, String dest) throws ZipException {  
       File destDir = new File(dest);  
       if ( !destDir.exists()) {  
           destDir.mkdir();  
       }  
     
       zFile.extractAll(dest);  
        
       List<FileHeader> headerList = zFile.getFileHeaders();  
       List<File> extractedFileList = new ArrayList<File>();  
       for(FileHeader fileHeader : headerList) {  
           if (!fileHeader.isDirectory()) {  
               extractedFileList.add(new File(destDir,fileHeader.getFileName()));  
           }  
       }  
       File [] extractedFiles = new File[extractedFileList.size()];  
       extractedFileList.toArray(extractedFiles);  
       return extractedFiles;  
   }  
   
   //��ѹ�������ļ���ָ��Ŀ¼
   public static File [] unzip(ZipFile zFile, ArrayList<String> files, String dest) throws ZipException {  
       File destDir = new File(dest);  
       if (destDir.isDirectory() && !destDir.exists()) {  
           destDir.mkdir();  
       }
  
       File [] extractedFiles = new File[files.size()];  
       int i = 0;
       for(String fileName:files){
    	   zFile.extractFile(fileName, dest);
    	   extractedFiles[i++] = new File(dest+File.separator+fileName);
       }
      
       return extractedFiles;  
   }  
   
 	//��ѹ�������ļ��е�ָ��Ŀ¼
   public static File [] unzipDir(ZipFile zipFile, ArrayList<String> dirs, String dest) throws ZipException { 
	   if (!zipFile.isValidZipFile()) {  
		   return null;
       }  
       File destDir = new File(dest);  
       if (destDir.isDirectory() && !destDir.exists()) {  
           destDir.mkdir();  
       }
  
       List<String> extractNames = new ArrayList<String>();  
       for(String dir : dirs){
	       if (!dir.isEmpty()){
		    	if (!dir.endsWith(File.separator)) dir += File.separator;  
		    }
		    // ���Ŀ¼������������  
		    FileHeader dirHeader = zipFile.getFileHeader(dir);  
		    if (null == dirHeader) continue;  		   
			
		    // ����ѹ���ļ������е�FileHeader, ��ָ��ɾ��Ŀ¼�µ����ļ�����������  
		    String subName = "";
		    List<FileHeader> headersList = zipFile.getFileHeaders();  
		    
		    for(int i=0, len = headersList.size(); i<len; i++) {  
		        FileHeader subHeader = (FileHeader) headersList.get(i);  
		        subName = subHeader.getFileName();
		        if (subName.startsWith(dirHeader.getFileName()) && 
		           !subName.equals(dirHeader.getFileName())) {
		        	extractNames.add(subName);
		        }  
		    }  
       }
       
       File fs[] = new File[extractNames.size()];
       int i = 0;
       for(String s : extractNames){
    	   zipFile.extractFile(s, dest);    	   
    	   fs[i++] = new File(dest+File.separator+s);
       }
       
	    return fs;
   }  
   
   //��ѹ�ļ��������ַ���
   public static File [] unzipFilter(ZipFile zFile, ArrayList<String> filter, String dest) throws Exception {  
       File destDir = new File(dest);  
       if (destDir.isDirectory() && !destDir.exists()) {  
           destDir.mkdir();  
       }
       
       File[] fs = listFiles(zFile);
       //filter fileName
       ArrayList<String> zipFileList = new ArrayList<String>();  
       if (filter!=null && filter.size()>0){
    	   Matcher m = null;
    	   Pattern p = null;
    	   for(String flt:filter){
    		   p = Pattern.compile(flt);
    		   for(int i=0; i<fs.length; i++){
    			   m = p.matcher(fs[i].getName());
    			   if(m.matches()){
        			   zipFileList.add(fs[i].getPath());
        		   }
    		   }    		   
	       }
    	   fs=null;
       }
       
       return unzip(zFile, zipFileList, dest);
   }  
     
   /***************************************
    * ����ѹ����£���ȡѹ���ļ�zip�е��ļ���.
    * **************************************/
   public static File [] listFiles(ZipFile zFile) throws ZipException {
       List<FileHeader> headerList = zFile.getFileHeaders();  
       List<File> extractedFileList = new ArrayList<File>();  
       for(FileHeader fileHeader : headerList) {  
           if (!fileHeader.isDirectory()) {  
               extractedFileList.add(new File(fileHeader.getFileName()));  
           }  
       }  
       File [] extractedFiles = new File[extractedFileList.size()];  
       extractedFileList.toArray(extractedFiles);  
       return extractedFiles;  
   }  
   
   //��ȡzip�ļ���path�µ��ļ���pathΪ�����ȡ���е��ļ�
   public static File [] listFiles(ZipFile zFile, String path, String[] filter) throws ZipException {
	   List<FileHeader> headerList = zFile.getFileHeaders();  
       List<File> extractedFileList = new ArrayList<File>();  
       if (path==null){
	       for(FileHeader fileHeader : headerList) {  
	           if (!fileHeader.isDirectory()) {  
	               extractedFileList.add(new File(fileHeader.getFileName()));  
	           }  
	       }  
       } else{
    	   String subName = "";
    	   if (!path.endsWith(File.separator)) path += File.separator;  
    	   String sDir = path.replace("\\", "/");
    	   sDir = sDir.replace("//", "/");
    	   for(FileHeader fileHeader : headerList) {  
   		        subName = fileHeader.getFileName();
   		        if (subName.startsWith(sDir) && !subName.equals(sDir)) {
		           extractedFileList.add(new File(fileHeader.getFileName()));  
   		        }
	       }     
       }
       
       //filter fileName
       List<File> zipFileList = new ArrayList<File>();  
       if (filter!=null && filter.length>0){
    	   Matcher m = null;
    	   Pattern p = null;
    	   for(String flt:filter){
    		   p = Pattern.compile(flt);
    		   for(int i=0; i<extractedFileList.size(); i++){
    			   m = p.matcher(extractedFileList.get(i).getName());
    			   if(m.matches()){
        			   zipFileList.add(extractedFileList.get(i));
        		   }
    		   }    		   
	       }
    	   extractedFileList.clear();
    	   File [] extractedFiles = new File[zipFileList.size()];  
    	   zipFileList.toArray(extractedFiles);  
	       return extractedFiles; 
       }else{       
		   File [] extractedFiles = new File[extractedFileList.size()];  
	       extractedFileList.toArray(extractedFiles);  
	       return extractedFiles;  
       }
   }
   
   //��ȡzip�ļ���path�µ�Ŀ¼��pathΪ�����ȡ���е�Ŀ¼
   public static File [] listDirs(ZipFile zFile, String path) throws ZipException {
       List<FileHeader> headerList = zFile.getFileHeaders();  
       List<String> extractedFileList = new ArrayList<String>();  
      
       if (path==null || path.isEmpty()){
	       for(FileHeader fileHeader : headerList) {  
	    	   if (fileHeader.isDirectory()) {
	    		   String spath = fileHeader.getFileName();
	    		   if (spath.endsWith("/")) spath=spath.substring(0, spath.length()-1);
	    		   if (!extractedFileList.contains(spath)){
	    			   extractedFileList.add(spath);  
	    		   }
	           } else {
	        	   int off=fileHeader.getFileName().lastIndexOf("/") ;
	        	   if(off>-1){
	        			String subStr = fileHeader.getFileName().substring(0, off);
	        			if (!extractedFileList.contains(subStr)){
	        				extractedFileList.add(subStr);  
	        			}
	        	   }
	           }
	       }  
       }else{
    	   if (!path.endsWith(File.separator)) path += File.separator;  
    	   String spath =  path.replace(File.separator, "/");
		    String subName = "";
		    for(FileHeader fileHeader : headerList) {   		    	
		        if (fileHeader.isDirectory()){
			        subName = fileHeader.getFileName();
			        if (subName.endsWith("/")) subName=subName.substring(0, subName.length()-1);
			        if (subName.startsWith(spath) && !subName.equals(spath)) {
			        	 if (!extractedFileList.contains(subName)){
			        		 extractedFileList.add(subName);  
			        	 }
			        }  
		        }else{
		        	int off=fileHeader.getFileName().lastIndexOf("/") ;
	        	    if(off>-1){
	        			String subStr = fileHeader.getFileName().substring(0, off);
	        			if (subStr.startsWith(path)){
		        			if (!extractedFileList.contains(subStr)){
		        				extractedFileList.add(subStr);  
		        			}
	        			}
	        	    }
		        }
		    }  
       }
     
	   File [] extractedFiles = new File[extractedFileList.size()];  
	   for(int n=0; n<extractedFileList.size(); n++){
		   extractedFiles[n] = new File( extractedFileList.get(n));  
	   }

	   return extractedFiles;  
   }
   
    //zip��������
	public static ZipParameters setZipParam(ZipFile zFile, String charsetName, String passwd) throws ZipException {
	   ZipParameters zRet = new ZipParameters();
	  
	   zRet.setCompressionMethod(CompressionMethod.DEFLATE);  
	   zRet.setCompressionLevel(CompressionLevel.NORMAL);
	   //��������
	   if(passwd!=null && !passwd.isEmpty()){
		   zRet.setEncryptFiles(true);  
		   zRet.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);   
		   zFile.setPassword(passwd.toCharArray());
	   }
	   
	   if(charsetName==null || charsetName.isEmpty()){
		   charsetName = "UTF8";
	   }
	   
	   zFile.setCharset(Charset.forName(charsetName));

	   return zRet;
	   
   }
   /** 
    * ѹ��ָ���ļ�����ǰ�ļ��� 
    * @param src Ҫѹ����ָ���ļ� 
    * @return ���յ�ѹ���ļ���ŵľ���·��,���Ϊnull��˵��ѹ��ʧ��. 
    */  
   public static String zip(String src) {  
       return zip(src,null);  
   }
 
   /** 
    * ʹ�ø�������ѹ��ָ���ļ����ļ��е���ǰĿ¼ 
    * @param src Ҫѹ�����ļ� 
    * @param passwd ѹ��ʹ�õ����� 
    * @return ���յ�ѹ���ļ���ŵľ���·��,���Ϊnull��˵��ѹ��ʧ��. 
    */  
   public static String zip(String src, String passwd) {  
       return zip(src, null, passwd);  
   }  
   
  
   /** 
    * ʹ�ø�������ѹ��ָ���ļ����ļ��е���ǰĿ¼ 
    * @param src Ҫѹ�����ļ� 
    * @param dest ѹ���ļ����·�� 
    * @param passwd ѹ��ʹ�õ����� 
    * @return ���յ�ѹ���ļ���ŵľ���·��,���Ϊnull��˵��ѹ��ʧ��. 
    */  
   public static String zip(String src, String dest, String passwd) {  
       return zip(src, dest, true, passwd);  
   }  
    
   /** 
    * ʹ�ø�������ѹ��ָ���ļ����ļ��е�ָ��λ��. 
    *  
    * dest�ɴ�����ѹ���ļ���ŵľ���·��,Ҳ���Դ����Ŀ¼,Ҳ���Դ�null����"". 
    * �����null����""��ѹ���ļ�����ڵ�ǰĿ¼,����Դ�ļ�ͬĿ¼,ѹ���ļ���ȡԴ�ļ���,��.zipΪ��׺; 
    * �����·���ָ���(File.separator)��β,����ΪĿ¼,ѹ���ļ���ȡԴ�ļ���,��.zipΪ��׺,������Ϊ�ļ���. 
    * @param src Ҫѹ�����ļ����ļ���·�� 
    * @param dest ѹ���ļ����·�� 
    * @param isCreateDir �Ƿ���ѹ���ļ��ﴴ��Ŀ¼,����ѹ���ļ�ΪĿ¼ʱ��Ч. 
    * ���Ϊfalse,��ֱ��ѹ��Ŀ¼���ļ���ѹ���ļ�. 
    * @param passwd ѹ��ʹ�õ����� 
    * @return ���յ�ѹ���ļ���ŵľ���·��,���Ϊnull��˵��ѹ��ʧ��. 
    */  
   
   public static String zip(String src, String dest, boolean isCreateDir, String passwd) {  
	   return zip(src, dest, isCreateDir, passwd, null);
   }
   
   /** 
    * ʹ�ø�������ѹ��ָ���ļ����ļ��е�ָ��λ��. 
    *  
    * @param files Ҫѹ�����ļ����ļ���·�� 
    * @param rootFolder ѹ���ļ����·�� 
    * @param zFile ���ɵ�ѹ���ļ�������.zipΪ��׺; 
    * @param passwd ѹ��ʹ�õ�����
    * @param charsetName ���õı���  
    * @return ���յ�ѹ���ļ���ŵľ���·��,���Ϊnull��˵��ѹ��ʧ��. 
    */  

   // ѹ���ļ�ʱ��·��,
   public static String zips(ArrayList<File> files, String rootFolder, String zFile, String passwd, String charsetName) {  
	   ZipFile zipFile = null;
	   ZipParameters parameters = new ZipParameters();  
       createDestDirectoryIfNecessary(zFile);  
       parameters.setCompressionMethod(CompressionMethod.DEFLATE);           // ѹ����ʽ  
       parameters.setCompressionLevel(CompressionLevel.NORMAL);    			 // ѹ������  
       try { 
	       if (!(passwd==null || passwd.length()==0)) {  
	           parameters.setEncryptFiles(true);  
	           parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD); // ���ܷ�ʽ  
	           zipFile = new ZipFile(zFile, passwd.toCharArray());  
	       }else{        
	    	   zipFile = new ZipFile(zFile);  
	       }
	       
           if (charsetName==null ||charsetName.length()==0){
        	   zipFile.setCharset(Charset.forName("UTF8")); 
           }else{
        	   zipFile.setCharset(Charset.forName(charsetName)); 
           }

           parameters.setRootFolderNameInZip(rootFolder);
           if (files!=null && files.size()>0){
        	   zipFile.addFiles(files, parameters);  
           }
           zipFile.close();
           return zFile;  
       } catch (Exception e) {  
           e.printStackTrace();  
       }  
       return null;  
   }  
   /* ���ļ�׷�ӵ�������dir
    * zFile zip�ļ���
    * file Ҫѹ�����ļ�
    * dir ѹ�������ļ���
    */
   public static boolean zip(ZipFile zipFile, File file, String zipDir, ZipParameters parameters) throws IOException { 
	   try {  
    	   String sFile = zipFile.getFile().getCanonicalPath();
           createDestDirectoryIfNecessary(sFile);  
           parameters.setRootFolderNameInZip(zipDir);
           
           zipFile.addFile(file, parameters);  
           
           return true;  
       } catch (Exception e) {  
    	   Logger.error("zip zipDir="+zipDir+";File="+file.getCanonicalPath() + " false");
       }  
       return false;  
   }  
   
   //�Ը�����files����dirs����ѹ������
   public static String zip(ZipFile zipFile, ZipParameters parameters, ArrayList<File> files, ArrayList<File> dirs) {
	   if (parameters.getCompressionLevel()!=CompressionLevel.ULTRA && parameters.getCompressionMethod()!=CompressionMethod.STORE){
		   parameters.setCompressionMethod(CompressionMethod.DEFLATE);           // ѹ����ʽ  
	       parameters.setCompressionLevel(CompressionLevel.NORMAL);    			 // ѹ������  
       }
       
       try {           
           if (files!=null && files.size()>0){
        	   zipFile.addFiles(files, parameters);  
           }
           if (dirs!=null && dirs.size()>0){
        	   for(int i=0; i<dirs.size(); i++){
        		   zipFile.addFolder(dirs.get(i), parameters);
        	   }
           }
           return zipFile.getFile().getName();  
       } catch (ZipException e) {  
    	   Logger.error(e.getStackTrace());
       }  
       return null;  
   }  
   
   //��srcѹ����dest�ļ�����
   public static String zip(String src, String dest, boolean isCreateDir, String passwd, String charsetName) {  
	   ZipFile zipFile = null;
	   File srcFile = new File(src);  
       dest = buildDestinationZipFilePath(srcFile, dest);  
       ZipParameters parameters = new ZipParameters();  
       parameters.setCompressionMethod(CompressionMethod.DEFLATE);           // ѹ����ʽ  
       parameters.setCompressionLevel(CompressionLevel.NORMAL);    			 // ѹ������  
       
       try {  
    	   if (!(passwd==null || passwd.length()==0)) {  
               parameters.setEncryptFiles(true);  
               parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD); // ���ܷ�ʽ  
               zipFile = new ZipFile(dest, passwd.toCharArray());  
           } else {
        	   zipFile = new ZipFile(dest);  
           }
    	   
    	   if (charsetName==null ||charsetName.length()==0){
        	   zipFile.setCharset(Charset.forName("UTF8")); 
           }else{
        	   zipFile.setCharset(Charset.forName(charsetName)); 
           }
    	   
           if (srcFile.isDirectory()) {  
               // ���������Ŀ¼�Ļ�,��ֱ�ӰѸ���Ŀ¼�µ��ļ�ѹ����ѹ���ļ�,��û��Ŀ¼�ṹ  
               if (!isCreateDir) {  
                   File [] subFiles = srcFile.listFiles();  
                   ArrayList<File> temp = new ArrayList<File>();  
                   Collections.addAll(temp, subFiles);  
                   zipFile.addFiles(temp, parameters);  
                   return dest;  
               }  
               zipFile.addFolder(srcFile, parameters);  
           } else {  
               zipFile.addFile(srcFile, parameters);  
           }  
           
           return dest;  
       } catch (Exception e) {  
           Logger.error(e.getStackTrace()); 
       }  finally{
    	   try {
				zipFile.close();
    	   } catch (IOException e) {
				Logger.error(e.getStackTrace());
    	   }
       }
       return null;  
   }  
   
   /** 
    * ����ѹ���ļ����·��,��������ڽ��ᴴ�� 
    * ����Ŀ������ļ�������Ŀ¼,Ҳ���ܲ���,�˷�������ת������ѹ���ļ��Ĵ��·�� 
    * @param srcFile Դ�ļ� 
    * @param destParam ѹ��Ŀ��·�� 
    * @return ��ȷ��ѹ���ļ����·�� 
    */  
   private static String buildDestinationZipFilePath(File srcFile,String destParam) { 
	   if (destParam==null || destParam.length()==0) {
           if (srcFile.isDirectory()) {  
               destParam = srcFile.getParent() + File.separator + srcFile.getName() + ".zip";  
           } else {  
               String fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));  
               destParam = srcFile.getParent() + File.separator + fileName + ".zip";  
           }  
       } else {  
           createDestDirectoryIfNecessary(destParam);  // ��ָ��·�������ڵ�����½��䴴������  
           if (destParam.endsWith(File.separator)) {  
               String fileName = "";  
               if (srcFile.isDirectory()) {  
                   fileName = srcFile.getName();  
               } else {  
                   fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));  
               }  
               destParam += fileName + ".zip";  
           }  
       }  
       return destParam;  
   }  
     
   /** 
    * �ڱ�Ҫ������´���ѹ���ļ����Ŀ¼,����ָ���Ĵ��·����û�б����� 
    * @param destParam ָ���Ĵ��·��,�п��ܸ�·����û�б����� 
    */  
   private static void createDestDirectoryIfNecessary(String destParam) {  
       File destDir = null;  
       String dest = destParam.replace("/", File.separator);
       dest = dest.replace("\\", File.separator);
       if (dest.endsWith(File.separator)) {  
           destDir = new File(dest);  
       } else {  
           destDir = new File(dest.substring(0, dest.lastIndexOf(File.separator)));  
       }  
       if (!destDir.exists()) {  
           destDir.mkdirs();  
       }  
   }  
   
   // ɾ��Ŀ¼.
   public static boolean removeDirFromZipArchive(ZipFile zipFile, String removeDir) throws ZipException {  
	   boolean bRet = false;
	   try{
		    // ��Ҫɾ����Ŀ¼����·���ָ���  
		   if (!removeDir.endsWith(File.separator)) removeDir += File.separator;  
		   String rDir = removeDir.replace(File.separator, "/");
		   rDir = rDir.replace("//", "/");
		  
		    // ����ѹ���ļ������е�FileHeader, ��ָ��ɾ��Ŀ¼�µ����ļ�����������  
		    List<FileHeader> headersList = zipFile.getFileHeaders();  
		    List<String> removeHeaderNames = new ArrayList<String>();  
		    for(int i=0, len = headersList.size(); i<len; i++) {  
		        FileHeader subHeader = (FileHeader) headersList.get(i);  
		        //System.out.println(subHeader.getFileName()+"=="+subHeader.isDirectory());
		        if (subHeader.getFileName().startsWith(rDir)  
		                && !subHeader.getFileName().equals(rDir)) {  
		            removeHeaderNames.add(subHeader.getFileName());  
		        }  
		    }  
		    // ����ɾ��ָ��Ŀ¼�µ��������ļ�, ���ɾ��ָ��Ŀ¼(��ʱ��Ϊ��Ŀ¼)  
		    for(String headerNameString : removeHeaderNames) {  
		        zipFile.removeFile(headerNameString);  
		    }  
	   }catch(Exception e){
		   Logger.error(e.getStackTrace());
	   }
	   
	   return bRet;
	}  
   
   // ɾ������Ϊ�����ļ�.
   public static boolean removeFilesFromZipArchive(ZipFile zipFile,  List<String> files) throws ZipException {
	   boolean bRet = false;
	   try{
		    // ����ɾ��ָ��Ŀ¼�µ��������ļ�, ���ɾ��ָ��Ŀ¼(��ʱ��Ϊ��Ŀ¼)  
		    for(String headerNameString : files) {  
		    	try{
		    		zipFile.removeFile(headerNameString);  
		    	}catch(Exception e){
		    		;
		    	}
		    	bRet = true;
		    }
		    
	   }catch(Exception e){
		   Logger.error(e.getStackTrace());
	   }
	   
	   return bRet;
	}  
   
   // ɾ�������������ַ��ļ�.
   // pattern�Ǵ����ʽ���磺*.xml
   public static boolean removePathFilePatternFromZip(ZipFile zipFile, String filePattern) throws ZipException {
	   if (filePattern.indexOf(File.separator)!=-1){
		   String removeDir=filePattern.substring(0, filePattern.indexOf(File.separator)+1);
		   String pat=filePattern.substring( filePattern.indexOf(File.separator)+1);
		   if(pat.equals("*")){
			   return removeDirFromZipArchive(zipFile, removeDir); 
		   }else if(removeDir.equals("."+File.separator)){
			   return removeFilePatternFromZip(zipFile, pat); 
		   }else{
			   return removeFilePatternFromPathZip(zipFile, removeDir, pat);
		   }
	   }else{
		   String pat = ImUtils.replaceSpecialString(filePattern);
		   return removeFilePatternFromZip(zipFile, pat);
	   }
   }
   
   //�Է���pattern�������ļ�����ɾ��
   private static boolean removeFilePatternFromZip(ZipFile zipFile, String pattern) throws ZipException {
	   boolean bRet = false;
	   try{
		    // ����ѹ���ļ������е�FileHeader, ��ָ��ɾ��Ŀ¼�µ����ļ�����������  
		    String subName = "", subOrg="";
		    List<FileHeader> headersList = zipFile.getFileHeaders();  
		    List<String> removeHeaderNames = new ArrayList<String>();  
		    
		    for(int i=0, len = headersList.size(); i<len; i++) {  
		        FileHeader subHeader = (FileHeader) headersList.get(i);  
		        subOrg = subName = subHeader.getFileName();	
		        int start = subName.indexOf("/",1);
		        if (start>0){
		        	subName = subName.substring(start+1);	
		        }
	        	//System.out.println(subName);
	        	if (subName.matches(pattern)){
	        		removeHeaderNames.add(subOrg);  
	        	}		          
		    }  

		    for(String headerNameString : removeHeaderNames) {  
		        zipFile.removeFile(headerNameString);  
		    }
	   }catch(Exception e){
		   Logger.error(e.getStackTrace());
	   }
	   
	   return bRet;
	}  
   
   ////�Ը���Ŀ¼removeDir�µķ���pattern�������ļ�����ɾ��
   public static boolean removeFilePatternFromPathZip(ZipFile zipFile, String removeDir, String pattern) throws ZipException {
	   boolean bRet = false;
	   try{
		   // ��Ҫɾ����Ŀ¼����·���ָ���  
		   if (!removeDir.endsWith(File.separator)) removeDir += File.separator;  
		   String rDir = removeDir.replace(File.separator, "/");
		   rDir = rDir.replace("//", "/");
		    // ����ѹ���ļ������е�FileHeader, ��ָ��ɾ��Ŀ¼�µ����ļ�����������  
		    String subName = "";
		    List<FileHeader> headersList = zipFile.getFileHeaders();  
		    List<String> removeHeaderNames = new ArrayList<String>();  

		    for(int i=0, len = headersList.size(); i<len; i++) {  
		        FileHeader subHeader = (FileHeader) headersList.get(i);  
		        subName = subHeader.getFileName();
		        //System.out.println("sub="+subName);
		        if(rDir.equals("/")){ //��Ŀ¼�µ��ļ�����Ч
		        	if (subName.indexOf("/")==-1){
			        	if (subName.matches(pattern)){
			        		removeHeaderNames.add(subName);  
			        	}
		        	}
		        }else if (subName.startsWith(rDir) && !subName.equals(rDir)) {
		        	String sub = subName.replaceFirst(rDir, "");
		        	
		        	if (sub.matches(pattern)){
		        		removeHeaderNames.add(subName);  
		        	}
		        }  
		    }  
		    // ����ɾ��ָ��Ŀ¼�µ��������ļ�, ���ɾ��ָ��Ŀ¼(��ʱ��Ϊ��Ŀ¼)  
		    for(String headerNameString : removeHeaderNames) {  
		        zipFile.removeFile(headerNameString);  
		    }
	   }catch(Exception e){
		   Logger.error(e.getStackTrace());
	   }
	   
	   return bRet;
	}  
   
   //�Կ����ݵ�zip�ļ�����.
   public static ZipFile resetZipFile(String sfile) throws ZipException{
	   if(!isValidZipFile(sfile)){
			File f = new File(sfile);
			if(f.exists()){
				f.delete();
			}
	   }
	   return new ZipFile(sfile);
   }
   
   
   //����Ƿ�Ϊ��Ч��zip�ļ�
   private static boolean isValidZipFile(String sfile ){
		boolean bRet = false;
		try {
			ZipFile zfile = new ZipFile(sfile);
			zfile.getFileHeaders();
			bRet = true;
			zfile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return bRet;
	}
   
   public static void main(String[] args) {  
     
     try {  
         //File[] files = unzip("d:\\tmp\\zdata����.zip", "111");
    	 //File[] files = unzip("d:\\tmp\\һ������100.zip", "222");
    	 //File[] files = unzip("d:\\tmp\\zdata\\zdat.zip",null);
//    	 File[] files = unzip("d:\\tmp\\zdata\\emp.zip");
//         for (int i = 0; i < files.length; i++) {  
//             System.out.println(files[i]);  
//         }  
    	 if (1==2){
    		 zip("d:\\tmp\\zdata\\dir", "d:\\tmp\\zdata\\emp_1007.zip"); 
    	 }else if(1==12){
    		 ArrayList<String> files=new ArrayList<String>();
    		 files.add("dir");
    		 unzip(new ZipFile("d:/tmp/zdata/java_1004.zip"), "d:/tmp/zout");
    	 }else if(1==1){
    		 ArrayList<String> files=new ArrayList<String>();
    		 files.add("dir");
    		 ZipFile zipFile = new ZipFile("d:/tmp/zdata/emp_1002.zip");
    		 List<FileHeader> headersList = zipFile.getFileHeaders();  
    		 unzipDir(zipFile,files,"d:/tmp/zout4");
    	 }else if(1==1){
	    	 String zfile = "d:/tmp/zdata/emp_1006.zip";
	    	
	    	 String pwd=null;
	    	 String code=null;
	    	 ArrayList<File> files=new ArrayList<File>();
	    	 files.add(new File("d:/tmp/zdata/aat.xlsx"));
	    	 files.add(new File("d:/tmp/zdata/cout.xlsx"));
	    	//ImZipUtil.removeFilesFromZipArchive(zfile, files, pwd, code);
	    	 zips(files, "ddd2", zfile, pwd, code);
    	 }
    	 
    	 
     } catch (Exception e) {  
         e.printStackTrace();  
     }  
   }  
}  