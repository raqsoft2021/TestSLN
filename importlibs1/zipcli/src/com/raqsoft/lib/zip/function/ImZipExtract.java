package com.raqsoft.lib.zip.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import com.raqsoft.common.Logger;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Table;
import com.raqsoft.resources.EngineMessage;

//��ѹ�ļ�
public class ImZipExtract extends ImFunction {	
	public Object doQuery(Object[] objs){
		Table tbl = new Table(new String[]{"FileName"});
		try {
			File [] fs = null;
			Object os[] = new Object[1];
			if ( m_passwd!=null){
				m_zipfile.setPassword(m_passwd.toCharArray());
			}
			if (objs.length==0){//ֻ��zipFileʱ
				fs = ImZipUtil.unzip(m_zipfile.getFile().getCanonicalPath());
				for(File f:fs){
					os[0] = f;
					tbl.newLast(os);
				}
			}else if (objs.length==1 || objs.length==2){
				ArrayList<String> lfile = new ArrayList<String>();
				ArrayList<String> ldir = new ArrayList<String>(); //�����ַ�
				ArrayList<String> lpat = new ArrayList<String>(); //�����ַ�
				ArrayList<File> ls = new ArrayList<File>();
				
				ImUtils.getZipFilterList(m_zipfile, null, objs[0], lfile, ldir, lpat);
				
				String dest = null;
				if (objs.length==1){ //��pathʱ
					File parentDir = m_zipfile.getFile().getParentFile();  
				    dest = parentDir.getAbsolutePath();
				}else{
					dest = objs[1].toString();
				}
				
				//2.1 ��ѹ�����ļ�
				if (lfile.size()>0){
					fs = ImZipUtil.unzip(m_zipfile, lfile, dest );
					ls.addAll(Arrays.asList(fs));
				}
				//2.2 ��ѹ�ļ���			
				if (ldir.size()>0){
					fs = ImZipUtil.unzipDir(m_zipfile, ldir, dest);
					ls.addAll(Arrays.asList(fs));
				}
				//2.2 ��ѹ�����ַ��ļ���
				if (lpat.size()>0){
					fs = ImZipUtil.unzipFilter(m_zipfile, lpat, dest);
					ls.addAll(Arrays.asList(fs));
				}
				
				//2.4ȥ�ش���:
				HashSet<File> h = new HashSet<File>(ls);   
			    ls.clear();   
			    ls.addAll(h);   
			    Collections.sort(ls);

			    for(File f:ls){
					os[0] = f;
					tbl.newLast(os);
				}
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("zip" + mm.getMessage("zipadd param error"));
			}
		} catch (Exception e) {
			Logger.error(e.getStackTrace());
		} 
	    
		 return tbl;
	}
}
