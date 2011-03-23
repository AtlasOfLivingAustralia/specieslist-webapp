<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<StyledLayerDescriptor version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
  xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<NamedLayer>
<Name>${namedLayer}</Name>
    <UserStyle>
        <FeatureTypeStyle>
            <Rule>
                <PolygonSymbolizer>
                    <Fill>
                        <CssParameter name="fill">
                            <ogc:Literal>#003300</ogc:Literal>
                        </CssParameter>
                        <CssParameter name="fill-opacity">
                            <ogc:Literal>1.0</ogc:Literal>
                        </CssParameter>
                    </Fill>
                    <Stroke>
                      <CssParameter name="stroke">#FFFFFF</CssParameter>
                      <CssParameter name="stroke-width">1</CssParameter>
                    </Stroke>
                </PolygonSymbolizer>
            </Rule>
            <Rule>
              <ogc:Filter xmlns:gml="http://www.opengis.net/gml">
                <ogc:Or>
                <c:forEach items="${regions}" var ="region">
                <ogc:PropertyIsEqualTo>
                  <ogc:PropertyName><string:decodeUrl>${layerParam}</string:decodeUrl></ogc:PropertyName>
                  <ogc:Literal><string:decodeUrl>${region}</string:decodeUrl></ogc:Literal>
                </ogc:PropertyIsEqualTo>
               </c:forEach>
               </ogc:Or>
              </ogc:Filter>
                <PolygonSymbolizer>
                    <Fill>
                        <CssParameter name="fill">
                            <ogc:Literal>#FF3300</ogc:Literal>
                        </CssParameter>
                        <CssParameter name="fill-opacity">
                            <ogc:Literal>1.0</ogc:Literal>
                        </CssParameter>
                    </Fill>
                </PolygonSymbolizer>
            </Rule>
        </FeatureTypeStyle>
    </UserStyle>
</NamedLayer>
</StyledLayerDescriptor>