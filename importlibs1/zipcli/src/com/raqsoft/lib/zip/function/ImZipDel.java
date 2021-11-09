package com.raqsoft.lib.zip.function;

import java.util.ArrayList;
import java.util.List;

import com.raqsoft.common.Logger;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.resources.EngineMessage;

//��zip�ļ���ɾ���ļ�
public class ImZipDel extends ImFunction {	
	public Object doQuery(Object[] objs){
		try {
			if (objs.length==1){
				ArrayList<String> lfile = new ArrayList<String>();
				ArrayList<String> ldir = new ArrayList<String>();
				ArrayList<String> lpat = new ArrayList<String>(); //�����ַ�
				ImUtils.getZipFilterList(m_zipfile, null, objs[0], lfile, ldir, lpat);
				int i = 0;
				//2.1 ɾ�������ļ�
				if (lfile.size()>0){
					ImZipUtil.removeFilesFromZipArchive(m_zipfile, (List<String>)lfile);
				}
				//2.1 ɾ��Ŀ¼
				if (ldir.size()>0){
					for(String dir:ldir){
						ImZipUtil.removeDirFromZipArchive(m_zipfile, dir);
					}
				}
				//2.2 ɾ�������ַ��ļ���
				for(i = 0; i<lpat.size(); i++){
					ImZipUtil.removePathFilePatternFromZip(m_zipfile, lpat.get(i));
				}
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("zip" + mm.getMessage("zipadd param error"));
			}
		} catch (Exception e) {
			Logger.error(e.getStackTrace());
		} 
	    
		 return true;
	}
	
}
