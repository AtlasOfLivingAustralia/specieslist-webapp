<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>IF RDF Properties for ${id}</title>
    </head>
    <body>
        <h1>RDF Properties for Digital Object with PID <code>${id}</code><br /></h1>
        <table>
            <!-- Table headings. -->
            <tr>
                <th>RDF Property</th>
                <th>Target</th>
            </tr>

            <!-- Dynamic table content. -->
            <s:iterator value="objProperties">
                <tr>
                    <td><s:property value="propertyName" /></td>
                    <td>
                        <a href="../${urlMapping}/${propertyValue}">${propertyValue}</a>
                    </td>
                </tr>
            </s:iterator>

        </table>
        <br />

    </div>
</body>
</html>
