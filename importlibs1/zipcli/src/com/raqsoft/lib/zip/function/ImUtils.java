package com.raqsoft.lib.zip.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raqsoft.common.Logger;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Sequence;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class ImUtils {
	/**
	 * �Ƿ����ض��ַ�*��?
	 * @param src
	 *            String
	 */
	private static String m_os = System.getProperty("os.name");  

	public static boolean isSpecialCharacters(String src) {
		boolean bRet = false;
		if (src.indexOf("*") != -1 || src.indexOf("?") != -1) {
			bRet = true;
		}
		return bRet;
	}

	/**
	 * ɾ��ĳ���ļ����µ������ļ��к��ļ�
	 * 
	 * @param delpath
	 *            String
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return boolean
	 */
	public static boolean deleteFile(String delpath) throws Exception {
		try {
			File file = new File(delpath);
			// ���ҽ����˳���·������ʾ���ļ������� ��һ��Ŀ¼ʱ������ true
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File delfile = new File(delpath + "\\" + filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
					} else if (delfile.isDirectory()) {
						deleteFile(delpath + "\\" + filelist[i]);
					}
				}
				file.delete();
			}
		} catch (FileNotFoundException e) {
			Logger.error("deletefile() Exception:" + e.getMessage());
		}
		
		return true;
	}

	/**
	 * ��s���ڵ��ļ����²���
	 * 
	 * @param s
	 *            String �ļ���
	 * @return File[] �ҵ����ļ�
	 */
	public static List<File> getFiles(String s, boolean bRecursive) {
		File f = new File(s);
		String name = f.getName();
		String path = f.getParentFile().getAbsolutePath();
		return getFiles(path, name, bRecursive);
	}

	/**
	 * ��ȡ�ļ� ���Ը���������ʽ����
	 * 
	 * @param dir
	 *            String �ļ�������
	 * @param s
	 *            String �����ļ������ɴ�*. ����ģ����ѯ
	 * @return File[] �ҵ����ļ�
	 */
	public static List<File> getFiles(String dir, String pattern, boolean bRecursive) {
		// ��ʼ���ļ���
		File file = new File(dir);
		//֧�ֺ��֣��ո�Ӣ����ĸ���磺teamview - ����.java
		String s = replaceSpecialString(pattern);
		//System.out.println(pat);
		Pattern p = Pattern.compile(s);
		ArrayList<File> list = filePattern(file, p,bRecursive);

		return list;
	}

	/**
	 * @param file
	 *            File ��ʼ�ļ���
	 * @param p
	 *            Pattern ƥ������
	 * @return ArrayList ���ļ����µ��ļ���
	 */

	private static ArrayList<File> filePattern(File file, Pattern p, boolean bRecursive ) {
		if (file == null) {
			return null;
		} else if (file.isFile()) {
			Matcher fMatcher = p.matcher(file.getName());
			if (fMatcher.matches()) {
				ArrayList<File> list = new ArrayList<File>();
				list.add(file);
				return list;
			}
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				ArrayList<File> list = new ArrayList<File>();
				for (int i = 0; i < files.length; i++) {
					if (bRecursive){
						ArrayList<File> rlist = filePattern(files[i], p, true);
						if (rlist != null) {
							list.addAll(rlist);
						}
					}else{
						if (files[i].isDirectory()) continue; //skip dir
						ArrayList<File> rlist = filePattern(files[i], p, false);
						if (rlist != null) {
							list.addAll(rlist);
						}
					}
				}
				return list;
			}
		}
		return null;
	}
	
	//���ش�·�����ļ���
	private static String getFullPathFileName(String path, String src){
		String sRet = new String(src);
		if(m_os.toLowerCase().startsWith("win")){ 
			if (sRet.length()>3 && sRet.indexOf(":"+File.separator)==1){ //��Ŀ¼·����fileName
				;//skip
			}else{
				sRet = path+File.separator+sRet;
			}
		}else{
			if (sRet.startsWith(File.separator)){ //�и�Ŀ¼·����fileName
				;//skip
			}else{
				sRet = path+File.separator+sRet;
			}
		}
		return sRet;
	}
	
	//·���ָ���ת��
	public static String replaceAllPathSeparator(String src){
		String sRet = new String(src);
		if (sRet.indexOf("*")!=-1){
			sRet = sRet.replaceAll("\\*", "#");
			if(m_os.toLowerCase().startsWith("win")){  
				sRet = sRet.replaceAll("/", "\\\\");
			}else{
				sRet = sRet.replaceAll("\\\\", "/");
			}
			sRet = sRet.replaceAll("#", "\\*");
		}else{
			if(m_os.toLowerCase().startsWith("win")){  
				sRet = sRet.replaceAll("/", "\\\\");
			}else{
				sRet = sRet.replaceAll("\\\\", "/");
			}
		}
		return sRet;
	}
	
	//��ȡ����·�����ļ��б�fObjs���������С��ļ������ַ���
	public static List<String> getFilter(Object fObjs)
	{
		String fname = "";
		FileObject fo=null;
		Object o = null;
		List<String> files=new ArrayList<String>();
		if (fObjs instanceof Sequence){
			Sequence sq = (Sequence)fObjs;			
			for(int i=0; i<sq.length(); i++){
				o = sq.get(i+1);
				if (o instanceof FileObject){
					fo = (FileObject)o;
					fname = fo.getFileName();
				}else{
					fname = o.toString();
				}
				fname = replaceAllPathSeparator(fname);
				files.add(fname);
			}
		}else if(fObjs instanceof FileObject){
			fo = (FileObject)fObjs;
			fname = fo.getFileName();
			fname = replaceAllPathSeparator(fname);
			files.add(fname);		
		}else{
			fname = fObjs.toString();
			fname = replaceAllPathSeparator(fname);
			files.add(fname);
		}
		return files;		
	}
	
	//�ļ���·���ϲ�
	public static List<String> getPathFilter(String path, Object fObjs)
	{
		if (path!=null && path.endsWith(File.separator)){
			path = path.substring(0, path.length()-1);
		}
		
		String fname = "";
		FileObject fo=null;
		Object o = null;
		List<String> files=new ArrayList<String>();
		if (fObjs instanceof Sequence){
			Sequence sq = (Sequence)fObjs;			
			for(int i=0; i<sq.length(); i++){
				o = sq.get(i+1);
				if (o instanceof FileObject){
					fo = (FileObject)o;
					fname = fo.getFileName();
				}else{
					fname = o.toString();
				}
				fname = replaceAllPathSeparator(fname);
				fname = getFullPathFileName(path, fname);
				
				files.add(fname);
			}
		}else {
			if(fObjs instanceof FileObject){
				fo = (FileObject)fObjs;
				fname = fo.getFileName();
			}else{
				fname = fObjs.toString();
			}
			
			fname = replaceAllPathSeparator(fname);
			fname = getFullPathFileName(path, fname);
			files.add(fname);	
		}
		
		return files;
	}
	
	//ͨ���ļ����ʽɸѡ�������ļ��б�Ŀ¼�б�
	public static void getFiles(List<String> filter,ArrayList<File> rFile,
			ArrayList<File> rDir, boolean bRecursive){
		File f = null;
		String fname = null;
		
		for(int i=0; i<filter.size(); i++){
			fname = filter.get(i);
			if (ImUtils.isSpecialCharacters(fname)){ //���������ַ�*,?
				String val = fname.substring(fname.lastIndexOf(File.separator)+1);
				if (val.equals("*")){
					val = fname.substring(0, fname.length()-2);
					rDir.add(new File(val));
				}else{
					List<File> fs = ImUtils.getFiles(fname, bRecursive);
					if (fs.size()>0){
						rFile.addAll(fs);
					}
				}
			}else{
				f = new File(fname);
				if (f.isDirectory()){
					if (bRecursive){ //�ݹ�ʱ����Ŀ¼����������
						rDir.add(f);
					}else{
						File[] subFiles = f.listFiles();
						for(File line:subFiles){
							if (line.isDirectory()) {
								if(bRecursive){
									rDir.add(line.getParentFile());
								}
							}else{
								rFile.add(line);
							}
						}						
					}
				}else{
					rFile.add(f);
				}
			}
		}
	}
	
	public static String replaceSpecialString(String val){
		String pat = val.replace('.', '#');
		pat = pat.replace("#", "\\.");
		if(val.indexOf(".")==-1){ //������׺�ı��ʽ.
			pat = pat.replace("*", "[\\u4e00-\\u9fa5- /\\w\\.]*");
		}else{
			pat = pat.replace("*", "[\\u4e00-\\u9fa5- /\\w]*");	
		}
		
		pat = pat.replace("?", "\\w{1}");
		return pat;
	}
	
	/*
	 * ��ȡҪ�����ѹ���ļ����ļ��У���ָ��·��
	 * 
	 * *****/	
	public static void getZipFilterList(ZipFile zipFile, String path, Object fobjs,ArrayList<String> rFile,
			ArrayList<String> rDir,ArrayList<String> rPat) throws ZipException{
		String fname = null;
		List<String> filter = ImUtils.getFilter(fobjs);
		
		for(int i=0; i<filter.size(); i++){
			fname = filter.get(i);
			//System.out.println("file = "+fname);
			if (ImUtils.isSpecialCharacters(fname)){ //���������ַ�*,?
				String pat = replaceSpecialString(fname);
				rPat.add(pat);
			}else{
 		        String sfile = "";
 		        if (path==null || path.isEmpty() || path.equals("\\")){
 		        	sfile = fname;
 		       }else {
 		        	sfile = path.replaceAll(File.separator, "/") + "/"+ fname;
 		        }

			    FileHeader dirHeader = zipFile.getFileHeader(sfile);  
				if (null == dirHeader){
					rDir.add(fname);
				}else{
					rFile.add(dirHeader.getFileName());
				}
			}
		}
	}
	
	//�Ƿ�Ϊ��·��
	public static boolean isRootPathFile(String file){
		boolean bRet = false;
		String fname = file;
		String os = System.getProperty("os.name");  
		if(os.toLowerCase().startsWith("win")){  
			fname = fname.replaceAll("/", File.separator);
			if (fname.length()>3 && fname.indexOf(":"+File.separator)==1){ //��Ŀ¼·����fileName
				bRet = true;
			}
		}else{ //linux
			fname = fname.replaceAll("\\\\", File.separator);
			if (fname.startsWith(File.separator)){ //��Ŀ¼·����fileName
				bRet = true;
			}
		}
		
		return bRet;
	}
	
	//��ȡ�ļ�·����windows��ȥ���̷�
	public static String getPathOfFile(File file) throws IOException{
		String parent = file.getParentFile().getCanonicalPath();
		String os = System.getProperty("os.name");  
		
		if(os.toLowerCase().startsWith("win")){  
			return parent.substring(3);
		}else{ //linux
			return parent;
		}
	}
}
