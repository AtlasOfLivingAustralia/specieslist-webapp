<%--
    Document   : downloadDiv
    Created on : Feb 25, 2011, 4:20:32 PM
    Author     : "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
--%>
%{--<%@ include file="/common/taglibs.jsp" %>--}%
%{--<%@page contentType="text/html" pageEncoding="UTF-8"%>--}%
<div id="download">
    <p id="termsOfUseDownload">
        By downloading this content you are agreeing to use it in accordance with the Atlas of Living Australia
        <a href="http://www.ala.org.au/about/terms-of-use/#TOUusingcontent">Terms of Use</a> and any Data Provider
    Terms associated with the data download.
        <br/><br/>
        Please provide the following details before downloading (* required):
    </p>
    <form id="downloadForm">
        %{--<input type="hidden" name="searchParams" id="searchParams" value="<c:out value="${searchResults.urlParameters}"/>"/>--}%
        %{--<c:choose>--}%
            %{--<c:when test="${clubView}">--}%
                %{--<input type="hidden" name="url" id="downloadUrl" value="${pageContext.request.contextPath}/proxy/download/download"/>--}%
            %{--</c:when>--}%
            %{--<c:otherwise>--}%
                %{--<input type="hidden" name="url" id="downloadUrl" value="${biocacheServiceUrl}/occurrences/download"/>--}%
            %{--</c:otherwise>--}%
        %{--</c:choose>--}%
        %{--<input type="hidden" name="url" id="downloadChecklistUrl" value="${biocacheServiceUrl}/occurrences/facets/download"/>--}%
        %{--<input type="hidden" name="url" id="downloadFieldGuideUrl" value="${pageContext.request.contextPath}/occurrences/fieldguide/download"/>--}%

        %{--<input type="hidden" name="extra" id="extraFields" value="${downloadExtraFields}"/>--}%

        <fieldset>
            <p><label for="email">Email</label>
                <input type="text" name="email" id="email" value="natasha" size="30"  />
            </p>
            <p><label for="filename">File Name</label>
                <input type="text" name="filename" id="filename" value="data" size="30"  />
            </p>
            <p><label for="reasonTypeId" style="vertical-align: top">Download Reason *</label>
                <select name="reasonTypeId" id="reasonTypeId">
                    <option value="">-- select a reason --</option>
                    %{--<c:forEach var="it" items="${LoggerReason}">--}%
                        %{--<option value="${it.id}">${it.name}</option>--}%
                    %{--</c:forEach>--}%
                </select>
            </p>
            %{--<c:set var="sourceId">--}%
                %{--<c:forEach var="it" items="${LoggerSources}">--}%
                    %{--<c:if test="${fn:toUpperCase(skin) == it.name}">${it.id}</c:if>--}%
                %{--</c:forEach>--}%
            %{--</c:set>--}%
            <br/>
            %{--<input type="hidden" name="sourceTypeId" id="sourceTypeId" value="${sourceId}"/>--}%
            <input type="submit" value="Download All Records" id="downloadSubmitButton"/>&nbsp;
            <input type="submit" value="Download Species Checklist" id="downloadCheckListSubmitButton"/>&nbsp;
            %{--<c:if test="${skin != 'avh'}">--}%
                %{--<input type="submit" value="Download Species Field Guide" id="downloadFieldGuideSubmitButton"/>&nbsp;--}%
            %{--</c:if>--}%
        <!--
            <input type="reset" value="Cancel" onClick="$.fancybox.close();"/>
            -->
            <p style="margin-top:10px;">
                <strong>Note</strong>: The field guide may take several minutes to prepare and download.
            </p>
            <div id="statusMsg" style="text-align: center; font-weight: bold; "></div>
        </fieldset>
    </form>
    <script type="text/javascript">

        $(document).ready(function() {
            // catch download submit button
            // Note the unbind().bind() syntax - due to Jquery ready being inside <body> tag.

            $(":input#downloadSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();

                if (validateForm()) {
                    var downloadUrl = generateDownloadPrefix($(":input#downloadUrl").val())+"&email="+$("#email").val()+"&sourceTypeId="+$("#sourceTypeId").val()+"&reasonTypeId="+
                            $("#reasonTypeId").val()+"&file="+$("#filename").val()+"&extra="+$(":input#extraFields").val();
                    //alert("downloadUrl = " + downloadUrl);
                    window.location.href = downloadUrl;
                    notifyDownloadStarted();
                }
            });
            // catch checklist download submit button
            $("#downloadCheckListSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();

                if (validateForm()) {
                    downloadUrl = generateDownloadPrefix($("input#downloadChecklistUrl").val())+"&facets=species_guid&lookup=true&file="+
                            $("#filename").val()+"&sourceTypeId="+$("#sourceTypeId").val()+"&reasonTypeId="+$("#reasonTypeId").val();
                    //alert("downloadUrl = " + downloadUrl);
                    window.location.href = downloadUrl;
                    notifyDownloadStarted();
                }
            });

            // catch checklist download submit button
            $("#downloadFieldGuideSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();

                if (validateForm()) {
                    var downloadUrl = generateDownloadPrefix($("input#downloadFieldGuideUrl").val())+"&facets=species_guid"+"&sourceTypeId="+
                            $("#sourceTypeId").val()+"&reasonTypeId="+$("#reasonTypeId").val();
                    window.open(downloadUrl);
                    notifyDownloadStarted();
                }
            });
        });

        function generateDownloadPrefix(downloadUrlPrefix) {
            downloadUrlPrefix = downloadUrlPrefix.replace(/\\ /g, " ");
            var searchParams = $(":input#searchParams").val();
            if (searchParams) {
                downloadUrlPrefix += searchParams;
            } else {
                // EYA page is JS driven
                downloadUrlPrefix += "?q=*:*&lat="+$('#latitude').val()+"&lon="+$('#longitude').val()+"&radius="+$('#radius').val();
            }

            return downloadUrlPrefix;
        }

        function notifyDownloadStarted() {
            $("#statusMsg").html("Download has commenced");
            window.setTimeout(function() {
                $("#statusMsg").html("");
                $.fancybox.close();
            }, 2000);
        }

        function validateForm() {
            var isValid = false;
            var reasonId = $("#reasonTypeId option:selected").val();

            if (reasonId) {
                isValid = true;
            } else {
                $("#reasonTypeId").focus();
                $("label[for='reasonTypeId']").css("color","red");
                alert("Please select a \"download reason\" from the drop-down list");
            }

            return isValid;
        }

    </script>
</div>