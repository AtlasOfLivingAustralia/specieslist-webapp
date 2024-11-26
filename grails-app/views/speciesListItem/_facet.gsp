<%@ page import="au.org.ala.specieslist.QueryService" defaultCodec="html" %>
%{-- Template for diplaying a single facet for a species list. --}%
<g:set var="facetId" value="${sl.facetAsId(key: key, prefix: "facet")}"/>
<p><span class="FieldName">${key}</span></p>

<div id="${facetId}" class="subnavlist">
    <ul class="facets list-unstyled">
        <g:set var="i" value="${0}"/>
        <g:while test="${i < 4 && i < values.size()}">
            <g:set var="arr" value="${values.get(i)}"/>
            <g:if test="${isProperty}">
                <li><g:link action="list" id="${params.id}"
                            params="${[fq: sl.buildFqList(fqs: fqs, fq: "kvp ${arr[0]}:${arr[1]}"), q: (params.q) ?: null, max: params.max]}">
                    <span class="fa fa-square-o">&nbsp;</span>
                    ${arr[2] ?: arr[1]}
                    (${arr[3]})
                </g:link>
                </li>
            </g:if>
            <g:else>
                <li><g:link action="list" id="${params.id}"
                            params="${[fq: sl.buildFqList(fqs: fqs, fq: "${QueryService.MATCHED_FAMILY.equals(key) ? 'matched family' : key}:${arr[0]}"), q: (params.q) ?: null, max: params.max]}">
                    <span class="fa fa-square-o">&nbsp;</span>
                    ${arr[0]}
                    (${arr[1]})
                    </g:link>
                </li>
            </g:else>
            <% i++ %>
        </g:while>
        <g:if test="${values.size() > 4}">
            <div class="showHide">

                <a href="${sl.facetAsId(key: key, prefix: "#div")}" class="multipleFacetsLinkZ"
                   id="${sl.facetAsId(key: key, prefix: "multi")}"
                   role="button" data-toggle="modal" title="${message(code:'public.lists.facets.refine.showhide.tooltip', default:'See full list of values')}">${message(code:'public.lists.facets.refine.showhide.text', default:'choose more...')}</a>
                <!-- modal popup for "choose more" link -->
                <div id="${sl.facetAsId(key: key, prefix: "div")}" class="modal fade" tabindex="-1" role="dialog"
                     aria-labelledby="multipleFacetsLabel" aria-hidden="true"><!-- BS modal div -->
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>

                                <h3 class="multipleFacetsLabel">${message(code:'public.lists.facets.refine.box.header', default:'Refine your search')}</h3>
                            </div>

                            <div class="modal-body">
                                <table class="table table-bordered table-condensed table-striped scrollTable"
                                       style="width:100%;">
                                    <thead class="fixedHeader">
                                    <tr class="tableHead">
                                        <th class="indexCol" width="80%">${key}</th>
                                        <th style="border-right-style: none;text-align: right;">${message(code:'generic.lists.button.Count.label', default:'Count')}</th>
                                    </tr>
                                    </thead>
                                    <tbody class="scrollContent">
                                    <g:each in="${values}" var="arr">
                                        <tr>
                                            <g:if test="${isProperty}">
                                                <td><g:link action="list" id="${params.id}"
                                                            params="${[fq: sl.buildFqList(fqs: fqs, fq: "kvp ${arr[0]}:${arr[1]}"), q: params.q, max: params.max]}">${arr[2] ?: arr[1]}</g:link></td>
                                                <td style="text-align: right; border-right-style: none;">${arr[3]}</td>
                                            </g:if>
                                            <g:else>
                                                <td><g:link action="list" id="${params.id}"
                                                            params="${[fq: sl.buildFqList(fqs: fqs, fq: "${QueryService.MATCHED_FAMILY.equals(key) ? 'matched family' : key}:${arr[0]}"), q: (params.q) ?: null, max: params.max]}">${arr[0]}</g:link></td>
                                                <td style="text-align: right; border-right-style: none;">${arr[1]}</td>
                                            </g:else>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>

                            <div class="modal-footer" style="text-align: left;">
                                <button class="btn btn-default btn-sm" data-dismiss="modal" aria-hidden="true"
                                        style="float:right;">${message(code:'data-dismiss-close', default: 'Close')}</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div><!-- invisible content div for facets -->
        </g:if>
    </ul>
</div>
