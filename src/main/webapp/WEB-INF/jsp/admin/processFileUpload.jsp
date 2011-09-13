<%@page import="org.ala.web.admin.controller.UploadController"%>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="org.apache.commons.fileupload.*"%>
<%@ page import="org.apache.commons.fileupload.disk.*"%>
<%@ page import="org.apache.commons.fileupload.servlet.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.io.*"%>
<%@ page import="org.ala.web.admin.model.*"%>
<%@ page import="org.apache.commons.beanutils.*"%>
<%@ page import="org.ala.web.admin.dao.*"%>
<%@ page import="org.springframework.context.ApplicationContext"%>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.support.ClassPathXmlApplicationContext" %>
<%@ page import="org.ala.util.SpringUtils" %>
<%@ page import="org.ala.model.Document" %>

<html>
	<head>
		<meta http-equiv="Content-Type"	content="text/html; charset=windows-1252">
		<title>Process File Upload</title>
	</head>
	<body bgcolor="white">
		<h2>Upload File to BIE</h2>
		<fieldset>
			<legend>Upload Files</legend>		
	
<%
	/*
	* Author: mok011
	*
	* Spring backing bean is not support multiple files upload, so direct using Apache commons fileupload
	* inside JSP page.
	*/
	ApplicationContext appCtx = SpringUtils.getContext();	
	int infoId = 1061;
	int size = 1000;	
	if(ServletFileUpload.isMultipartContent(request)){
		UploadItem uploadItem = new UploadItem();
		Map<String, String> properties = new Hashtable<String, String>();
		List<FileItem> files = new ArrayList<FileItem>();
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1*1024*1024);
		factory.setRepository(new File("/data"));
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
	
		// Parse the request
		List /* FileItem */ items = upload.parseRequest(request);
		
		// Process the uploaded items
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
	
		    if (item.isFormField()) {
		    	String name = item.getFieldName();		    	
		        String value = item.getString();
		        properties.put(name, value);
		    } else {
		        String fileName = item.getName();
		        //remove file path....IE is included file path
		        int windex = fileName.lastIndexOf('\\');
		        int uindex = fileName.lastIndexOf('/');
		        if(windex > 0){
		        	fileName = fileName.substring(windex + 1);
		        }
		        if(uindex > 0){
		        	fileName = fileName.substring(uindex + 1);
		        }
		        if(fileName != null && !"".equals(fileName)){
					File uploadedFile = new File("/data" + fileName);
				    item.write(uploadedFile);
					out.write("File Name: " + fileName + "&nbsp;<strong><font color='blue'>Uploaded successful.</font></strong><br/>");
					files.add(item);
		        }
		    }
		}		
		/* store into respository*/
		try{
			Document doc = null;
			BeanUtilsBean bub = new BeanUtilsBean();
			bub.populate(uploadItem, properties);
			uploadItem.setFiles(files);
			if(appCtx != null){
				ImageUploadDao dao = (ImageUploadDao)appCtx.getBean("imageUploadDao");
				if(dao != null){
					doc = dao.storeDocument(infoId, uploadItem);
					if(doc == null){
						out.write("<br/><br/><strong><font color='red'>Store Document into Repository is unsuccessful!!</font></strong><br/>");
					}
				}
			}
		} 
		catch (Exception ex) {
			out.write("<br/><br/><strong><font color='red'>Store Document into Repository is unsuccessful!!</font></strong><br/>");
		}
	}
%>
		</fieldset>
		<br/>
		<a href="../">go back to menu</a>
	</body>		
</html>
