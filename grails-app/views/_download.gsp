<%--
    Document   : downloadDiv
    Created on : Feb 25, 2011, 4:20:32 PM
    Author     : "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
--%>
%{--<%@ include file="/common/taglibs.jsp" %>--}%
%{--<%@page contentType="text/html" pageEncoding="UTF-8"%>--}%
<div id="download">
    <p id="termsOfUseDownload">
        ${message(code:'download.lists.text01_1', default:'By downloading this content you are agreeing to use it in accordance with the ')}<a href="${grailsApplication.config.termsOfUseUrl}">Terms of Use</a> ${message(code:'download.lists.text01_2', default:'and any Data Provider Terms associated with the data download.')}
        <br/><br/>
${message(code:'download.lists.text02', default:'Please provide the following details before downloading (* required):')}
    </p>
    <form id="downloadForm">

        <fieldset>
            <p><label for="email">${message(code:'download.lists.email.label', default:'Email')}: </label>
                <input type="text" name="email" id="email" value="${request.remoteUser}" size="30"  />
            </p>
            <p><label for="filename">${message(code:'download.lists.filename.label', default:'File Name')}: </label>
                <input type="text" name="filename" id="filename" value="${speciesList?.listName?.replaceAll(~/\s+/, "_")?:"data"}" size="30"  />
            </p>
            <p><label for="reasonTypeId" style="">${message(code:'download.lists.reason.label', default: 'Download Reason')}*: </label>
                <select name="reasonTypeId" id="reasonTypeId">
                    <option value="">${message(code:'download.lists.reason.select', default: '-- select a reason --')}</option>
                    <g:each in="${downloadReasons}" var="reason">
                        <option value="${reason.key}">${reason.value}</option>
                    </g:each>
                </select>
            </p>

            <br/>

            <p style="text-align: center">
            <g:if test="${grailsApplication.config.occurrenceDownload.enabled.toBoolean()}">
                <input type="submit" value="${message(code:'download.lists.button01', default:'Download All Records')}" class="actionButton btn btn-default" id="downloadSubmitButton"/>
            </g:if>
            <g:if test="${grailsApplication.config.fieldGuide.baseURL}">
                <input type="submit" value="${message(code:'download.lists.button02', default:'Download Species Field Guide')}" class="actionButton btn btn-default" id="downloadFieldGuideSubmitButton"/>
            </g:if>
            <input type="submit" value="${message(code:'download.lists.button03', default:'Download Species List')}" class="actionButton btn btn-default" id="downloadSpeciesListSubmitButton"/>
            </p>
            <g:if test="${grailsApplication.config.fieldGuide.baseURL}">
                <p style="margin-top:10px;">
                    ${message(code:'download.lists.fieldguide.info', default:'The field guide may take several minutes to prepare and download.')}
                </p>
            </g:if>
            <div id="statusMsg" style="text-align: center; font-weight: bold; "></div>
        </fieldset>
    </form>
    <asset:script type="text/javascript">

        $(document).ready(function() {
            // catch download submit button
            // Note the unbind().bind() syntax - due to Jquery ready being inside <body> tag.

            $("#downloadSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();

                if (validateForm()) {
                    downloadURL = "${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Download&email="+$("#email").val()+"&reasonTypeId="+$("#reasonTypeId").val()+"&file="+$("#filename").val();
                    window.location.href = downloadURL;
                    notifyDownloadStarted()
                }
            });

            $("#downloadSpeciesListSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();
                if(validateForm()){
                    //alert("${request.contextPath}/speciesListItem/downloadList/${params.id}${params.toQueryString()}&file="+$("#filename").val())
                    window.location.href = "${request.contextPath}/speciesListItem/downloadList/${params.id}${params.toQueryString()}&file="+$("#filename").val()
                    notifyDownloadStarted()
                }
            });

            // catch checklist download submit button
            $("#downloadFieldGuideSubmitButton").unbind("click").bind("click",function(e) {
                e.preventDefault();

                if (validateForm()) {
                    var downloadUrl = "${request.contextPath}/speciesList/fieldGuide/${params.id}${params.toQueryString()}"
                    //alert(downloadUrl)
                    window.open(downloadUrl);
                    notifyDownloadStarted()
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
            $("#statusMsg").html("${message(code:'download.lists.downloadsuccessful.message', default:'Download has commenced')}");
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
                alert("${message(code:'download.lists.downloadreasonerror.message', default:'Please select a \'download reason\' from the drop-down list!')}");
            }

            return isValid;
        }

    </asset:script>
</div>