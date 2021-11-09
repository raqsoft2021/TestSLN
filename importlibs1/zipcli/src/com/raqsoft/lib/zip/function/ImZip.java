package com.raqsoft.lib.zip.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.raqsoft.common.Logger;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Function;
import com.raqsoft.expression.IParam;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

	/***********************************
	 ���� 1��zip(zipfile:encoding, password; path, files)
		zipfileΪzip�ļ�����FileObject
		encodingΪ�ַ����룬ʡ����Ϊutf-8
		passwordΪ����,��ʡ��
		pathΪ�ļ����ڸ�Ŀ¼��ʡ�Ի�ΪnullʱΪzipfile�����ļ�Ŀ¼
		filesΪ�ɰ���ͨ���*��?���ļ���(/��\��ͬ)���ļ������У�Ҳ������FileObject��FileObject����
		@u��ѹ: pathΪ���·��
		@a׷�ӣ� pathΪҪѹ���ļ���·��
		@dɾ��  pathΪzip�ļ��е�·��
		@n���ݹ���Ŀ¼
		@f�г��ļ���
		@p�г�Ŀ¼��
	 *************************************************/

public class ImZip extends Function {
	private ZipFile m_zfile;
	private boolean m_bRecursive = true; 
	private ZipParameters m_parameters;
	public Node optimize(Context ctx) {
		return this;
	}

	//zip(zipfile:encoding,password; path, files)
	//�������ݲ���������map
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("zip" + mm.getMessage("function.invalidParam"));
		}

		Map<String, Object> map = new HashMap<String, Object>();
		if (param.isLeaf()){
			map.put("zip", param.getLeafExpression().calculate(ctx));
		}else{
			int nSize = param.getSubSize();		
			char rootType = param.getType();
			for(int i=0; i<nSize; i++){
				ArrayList<Expression> ls = new ArrayList<Expression>();	
				param.getSub(i).getAllLeafExpression(ls);
				if(i==0){ //;ǰ����
					map.put("zip", ls.get(0).calculate(ctx));
					if (param.getSub(i).getType()==IParam.Comma){				
						if(ls.size()==3){					
							map.put("code", ls.get(1).calculate(ctx));
							map.put("pwd", ls.get(2).calculate(ctx));
						}else if(ls.size()==2){
							map.put("pwd", ls.get(1).calculate(ctx));
						}
					}else if(param.getSub(i).getType()==IParam.Colon){
						if(ls.size()==2){
							map.put("code", ls.get(1).calculate(ctx));
						}
					}
				} else { 
					if (rootType==IParam.Comma){
						map.put("pwd", param.getSub(i).getLeafExpression().calculate(ctx));
					}else{
						//;�󲿷�								
						param.getSub(i).getAllLeafExpression(ls);	
						if (param.getSub(i).getType()==IParam.Comma){
							if (ls.get(0)!=null){
								map.put("path", ls.get(0).calculate(ctx));
							}
							map.put("files", ls.get(1).calculate(ctx));
						}else{
							map.put("path", ls.get(0).calculate(ctx));
						}
					}
				}
			}
		}
		
//		Iterator<String> it =map.keySet().iterator();
//        while(it.hasNext()){
//            //�õ�ÿһ��key
//            String key = it.next();
//            //ͨ��key��ȡ��Ӧ��value
//            Object value = map.get(key);
//            System.out.println("kv:: "+key+"=>"+value);
//        }

		return doZip(option, map);
	}	
	
	//zip����ѡ��
	private Object doZip(String opt, Map<String, Object> map){
		try {
			String sfile, path=null, code=null,pwd=null;
			
			// for zipFile
			Object o = map.get("zip");
			if (o==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("zip" + mm.getMessage("zipFile is null"));
			}
			if (o instanceof FileObject){
				sfile = ((FileObject)o).getFileName();
			}else{
				sfile =  o.toString();
			}
			// for code
			o = map.get("code");
			if (o!=null){ code = o.toString(); }
			o = map.get("pwd");			
			if (o!=null){ pwd = o.toString(); }
			o = map.get("path");			
			if (o!=null){ 
				path = o.toString(); 
				path = ImUtils.replaceAllPathSeparator(path);
			}			
			
			m_zfile = ImZipUtil.resetZipFile(sfile);
			m_parameters = ImZipUtil.setZipParam(m_zfile, code, pwd);
			
			if (opt!=null){
				if (opt.indexOf("n")!=-1){ //@n���ݹ���Ŀ¼
					m_bRecursive = false;
				}
				if (opt.indexOf("u")!=-1){ //@u��ѹ
					if (path==null){
						path = getZipParentPath();
						path = ImUtils.replaceAllPathSeparator(path);
					}
					
					return doUnzipFiles( path, map.get("files"));
				}else if (opt.indexOf("a")!=-1){ //@a׷��
					if (path==null){
						path = getZipParentPath();
						path = ImUtils.replaceAllPathSeparator(path);
					}					
					return doZipFiles( path, map.get("files"));
				}else if (opt.indexOf("d")!=-1){ //@dɾ��
					return delZipFiles(path, map.get("files"));
				}else if (opt.indexOf("f")!=-1){ //@f�г��ļ���
					return getZipFileNames(path, map.get("files"));
				}else if (opt.indexOf("p")!=-1){ //@p�г�Ŀ¼��
					return getZipDirs( path);
				}
			}
			// ȱʡ����
			if (path==null){
				path = getZipParentPath();
				path = ImUtils.replaceAllPathSeparator(path);
			}	
			return doZipFiles(path, map.get("files"));
			
		} catch (Exception e) {
			Logger.error(e.getStackTrace());
		}
		return null;
	}
	
	//��ȡzipfile����·��
	private String getZipParentPath() throws Exception{
		return m_zfile.getFile().getParentFile().getCanonicalPath();
	}
	
	//��ȡzipĿ¼
	private Object getZipDirs( String path){
		Table tbl = null;
		try {				
			File[] fs = ImZipUtil.listDirs(m_zfile, path);
			if (fs.length>0){
				tbl = new Table(new String[]{"DirName"});
				Object[] objs=new Object[1];
				for(File line : fs ){
					objs[0] = line.getPath();
					tbl.newLast(objs);
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return tbl;
	}
	
	//��ȡzip�ļ��б�
	private Object getZipFileNames( String path, Object fobjs){
		try {	
			String[] filter = null;
			if (fobjs!=null){
				filter = doFileFilter(fobjs);
			}
			File[] fs = ImZipUtil.listFiles(m_zfile, path, filter);
			if (fs==null) return null;
			
			Table tbl = new Table(new String[]{"FileName"});
			Object[] objs=new Object[1];
			for(File line : fs ){
				objs[0] = line.getPath();
				tbl.newLast(objs);
			}
			
			return tbl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//���˲�������
	private String[] doFileFilter(Object fObjs){		
		String fname = "";
		List<String> files=new ArrayList<String>();
		if (fObjs instanceof Sequence){
			Sequence sq = (Sequence)fObjs;
			FileObject fo=null;
			Object o = null;
			
			for(int i=0; i<sq.length(); i++){
				o = sq.get(i+1);
				if (o instanceof FileObject){
					fo = (FileObject)o;
					fname = fo.getFileName();
				}else{
					fname = o.toString();
				}
				fname = fname.replaceAll("/", "\\\\");
				String pat = fname.replace('.', '#');
			    pat = pat.replace("#", "\\.");
			    pat = pat.replace("*", "\\w*");
				pat = pat.replace("?", "\\w{1}");
				files.add(pat);
			}
		}else{
			fname = fObjs.toString();
			String pat = fname.replace('.', '#');
		    pat = pat.replace("#", "\\.");
		    pat = pat.replace("*", "\\w*");
			pat = pat.replace("?", "\\w{1}");
			files.add(pat);
		}
		String fs[] = new String[files.size()];
		files.toArray(fs);
		return fs;
	}
	
	/**
	 * ��ȡѹ���ļ����ļ���
	 * ���ļ��л��ļ�ƴ�ɴ�����·��
	 * ���������ַ�*,?
	 * path��ѹ���ļ����ڵ�·��
	 * fobjs��Ҫ������ļ����ļ����ʽ
	 * rFile: ���ص��ļ��б�
	 * rDir�� ���ص��ļ�Ŀ¼
	 * ***/
	
	private void getFileList(String path, Object fobjs,ArrayList<File> rFile,ArrayList<File> rDir){
		Object o = fobjs;
		List<String> filter=null;
		if (o==null && path == null){ 
			MessageManager mm = EngineMessage.get();
			throw new RQException("zip" + mm.getMessage("add src file is null"));
		}else if(o!=null){
			if (path!=null && !path.isEmpty()){
				filter = ImUtils.getPathFilter(path, o);
			}else{
				filter = ImUtils.getFilter(o);
			}
			ImUtils.getFiles(filter, rFile, rDir, m_bRecursive);
		}else if(path!=null){ //(o==null && path!=null)
			if (m_bRecursive){
				rDir.add(new File(path));
			}else{
				filter = new ArrayList<String>();
				filter.add(path);
				ImUtils.getFiles(filter, rFile, rDir, m_bRecursive);
			}
		}
	}
	
	/**
	 * ��ѹ�ļ�����
	 * path��ѹ����ļ���·������Ϊ������zfile·�����ڵ�·��һ�£��������·������path׷�ӵ�zfile·��.
	 * fobjsΪҪ��ѹ���ļ�(�б�)
	 *
	 * ***********************************************/
	private Object doUnzipFiles(String path, Object fobjs) throws Exception{
		File [] fs = null;
		Table tbl = new Table(new String[]{"File"});
		Object objs[] = new Object[1];
		
		String rootPath = getZipParentPath();
		if (!ImUtils.isRootPathFile(path)){
			path = rootPath+File.separator+path;
		}
		
		if(fobjs==null || fobjs.toString().isEmpty()){
			fs = ImZipUtil.unzip(m_zfile, path);
			for(File f:fs){
				objs[0] = f;
				tbl.newLast(objs);
			}
		}else{
			ArrayList<String> lfile = new ArrayList<String>();
			ArrayList<String> ldir = new ArrayList<String>();
			ArrayList<String> lpat = new ArrayList<String>(); //�����ַ�
			ArrayList<File> ls = new ArrayList<File>();

			ImUtils.getZipFilterList(m_zfile, null, fobjs, lfile, ldir, lpat);
		
			//2.1 ��ѹ�����ļ�			
			if (lfile.size()>0){
				fs = ImZipUtil.unzip(m_zfile, lfile, path);
				ls.addAll(Arrays.asList(fs));
			}
			
			//2.2 ��ѹ�ļ���			
			if (ldir.size()>0){
				fs = ImZipUtil.unzipDir(m_zfile, ldir, path);
				ls.addAll(Arrays.asList(fs));
			}
			//2.3 ��ѹ�����ַ��ļ���
			if (lpat.size()>0){
				fs = ImZipUtil.unzipFilter(m_zfile, lpat, path);
				ls.addAll(Arrays.asList(fs));
			}
			
			//2.4ȥ�ش���:
			HashSet<File> h = new HashSet<File>(ls);   
		    ls.clear();   
		    ls.addAll(h);   
		    Collections.sort(ls);
		    //System.out.println(ls.toString());
		    for(File f:ls){
				objs[0] = f;
				tbl.newLast(objs);
			}
		}
		return tbl;
	}
	
	/**********************************************
	 * ѹ���ļ�����Ҳ����׷���ļ�
	 * pathҪѹ���ļ���·������Ϊ������zfile·�����ڵ�·��һ��.
	 * fobjsΪҪѹ�����ļ�(�б�)
	 * 
	 * ***********************************************/
	private Object doZipFiles( String path, Object fobjs) throws Exception{
		ArrayList<File> lfile = new ArrayList<File>();
		ArrayList<File> ldir = new ArrayList<File>();
		
		String rootPath = getZipParentPath();
		if (!ImUtils.isRootPathFile(path)){
			path = rootPath+File.separator+path;
		}
		if (!(path==null || path.isEmpty())){
			rootPath = path;
		}
		rootPath = rootPath.toLowerCase();
		getFileList(path, fobjs, lfile, ldir);

		String dir = "";		
		for(File f:lfile){		
			String sfile = f.getCanonicalPath().toLowerCase();
			if (sfile.startsWith(rootPath)){
				dir = sfile.substring(0, sfile.lastIndexOf(File.separator));
				dir = dir.replace(rootPath+File.separator, "");
				dir = dir.replace(rootPath, "");
				ImZipUtil.zip(m_zfile, f, dir, m_parameters);
			}else{
				dir = ImUtils.getPathOfFile(f);
				ImZipUtil.zip(m_zfile, f, dir, m_parameters);
			}
		}
		
		if(ldir.size()>0){
			ImZipUtil.zip(m_zfile, m_parameters, null, ldir);
		}
		
		return true;
	}
	/**
	 * ɾ��ѹ���ļ�����Ҳ�����ļ���Ŀ¼
	 * path: zip�ļ���Ҫɾ���ļ���·�����Ӹ�·����ʼ���ļ���.
	 * fobjs: ΪҪѹ�����ļ�(�б�)
	 * ***********************************************/
	private Object delZipFiles(String path, Object fobjs){
		boolean bRet = false;
		try {
			//A. ɾ��Ŀ¼
			if (fobjs==null || fobjs.toString().isEmpty() || fobjs.toString().equals("*")){
				if (path!=null){
					ImZipUtil.removeDirFromZipArchive(m_zfile, path);
				}
			}else if( path==null || (path!=null &&(path.equals(".") || path.equals("./") || path.equals(".\\") 
					|| path.equals("\\") || path.equals("/"))) ){ //B. ɾ���ļ�(���ִ������ַ���������ַ����)��
				ArrayList<String> lfile = new ArrayList<String>();
				ArrayList<String> ldir = new ArrayList<String>();
				ArrayList<String> lpat = new ArrayList<String>(); //�����ַ�
				ImUtils.getZipFilterList(m_zfile, null, fobjs, lfile, ldir, lpat);
				int i = 0;
				//2.1 ɾ�������ļ�
				if (lfile.size()>0){
					ImZipUtil.removeFilesFromZipArchive(m_zfile, (List<String>)lfile);
				}
				//2.1 ɾ��Ŀ¼
				if (ldir.size()>0){
					for(String dir:ldir){
						ImZipUtil.removeDirFromZipArchive(m_zfile, dir);
					}
				}
				//2.2 ɾ�������ַ��ļ���
				for(i = 0; i<lpat.size(); i++){
					if (path==null){
						ImZipUtil.removePathFilePatternFromZip(m_zfile, lpat.get(i));
					}else{
						ImZipUtil.removeFilePatternFromPathZip(m_zfile, path, lpat.get(i));
					}
				}
			}else{ //C ɾ��Ŀ¼���ļ�
				ArrayList<String> lfile = new ArrayList<String>();
				ArrayList<String> ldir = new ArrayList<String>();
				ArrayList<String> lpat = new ArrayList<String>(); //�����ַ�
				ImUtils.getZipFilterList(m_zfile, path, fobjs, lfile, ldir, lpat);
				int i = 0;
				//2.1 ɾ�������ļ�
				if (lfile.size()>0){ //file��path�ϲ�����ɾ��.
					String file="";
					ArrayList<String> ls = new ArrayList<String>();
					for(String f:lfile){
						if (path.equals("\\")){
							file = f;
						}else{
							file = path+"/"+f;
						}
						ls.add(file);
					}
					lfile.clear();
					ImZipUtil.removeFilesFromZipArchive(m_zfile, ls);
				}
				
				//2.1 ɾ��Ŀ¼
				if (ldir.size()>0){
					for(String dir:ldir){
						ImZipUtil.removeDirFromZipArchive(m_zfile, dir);
					}
				}
				//2.2 ɾ�������ַ��ļ���
				for(i = 0; i<lpat.size(); i++){
					ImZipUtil.removeFilePatternFromPathZip(m_zfile, path, lpat.get(i));
				}				
			}
			bRet = true;
		} catch (ZipException e) {
			Logger.error(e.getStackTrace());
		}
		
		return bRet;
	}
}
