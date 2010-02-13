<%@ page contentType="text/plain; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib prefix="json" uri="http://www.atg.com/taglibs/json" 
%>
<json:array name="taxonConcepts" var="taxonConcept" items="${taxonConcepts}">
  <json:object>
    <json:property name="text">${taxonConcept.nameString}</json:property>
     <json:property name="commonName">${taxonConcept.commonName}</json:property>
    <json:property name="id" value="${taxonConcept.guid}"/>
    <json:property name="hasChildren" value="${taxonConcept.hasChildren}"/>  
    </json:object>
</json:array>