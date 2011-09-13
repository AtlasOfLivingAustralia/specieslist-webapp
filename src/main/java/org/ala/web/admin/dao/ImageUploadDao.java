package org.ala.web.admin.dao;

import java.util.List;
import java.util.Map;

import org.ala.model.Document;
import org.ala.web.admin.model.UploadItem;
import org.ala.repository.Triple;

public interface ImageUploadDao {
	public Document storeDocument(int infoSourceId, UploadItem uploadItem) throws Exception ;
	public boolean updateDocument(Document doc, UploadItem uploadItem) throws Exception;
	public Map<String,String> readDcFile(Document doc);
	public List<Triple<String, String, String>> readRdfFile(Document doc);
}
