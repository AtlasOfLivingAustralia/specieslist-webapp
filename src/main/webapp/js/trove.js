var TROVE = {
  s: 0,
  n: 10,
  url: "http://api.trove.nla.gov.au/result?key=fvt2q0qinduian5d&zone=book&encoding=json",
  q: "",
  totalResults: 0,
  divId: '',
  nextButtonId:'',
  previousButtonId:'',
    containerDivId:''
}

function getTroveUrl(){
  return TROVE.url + '&q=' + TROVE.q + '&s=' + TROVE.s + '&n=' + TROVE.n;
}

function setupTrove(query, containerDivId, resultsDivId, previousButtonId, nextButtonId){
    TROVE.q = query;
    TROVE.containerDivId = containerDivId
    TROVE.divId = resultsDivId
    TROVE.nextButtonId =  nextButtonId;
    TROVE.previousButtonId =  previousButtonId;
    $('#'+TROVE.nextButtonId).click(function(){ troveNextPage();});
    $('#'+TROVE.previousButtonId).click(function(){ trovePreviousPage();});
    queryTrove();
}

function troveNextPage(){
    if( (TROVE.s + TROVE.n) < TROVE.totalResults){
        TROVE.s += TROVE.n;
        queryTrove();
    }
}

function trovePreviousPage(){
    if(TROVE.s > 0){
        TROVE.s -= TROVE.n;
        queryTrove();
    }
}

function queryTrove(){
    $.ajax({
        url: getTroveUrl(),
        dataType: 'jsonp',
        data: null,
        jsonp: "callback",
        success:  function(data) {
           // console.log("Success....results: " + data.response.zone[0].records.total);
            TROVE.totalResults = data.response.zone[0].records.total;
            if(TROVE.totalResults == 0){
                $('#'+TROVE.containerDivId).css({display:'none'});
            } else {
                var buff = '<div class="results-summary">Number of matches in TROVE: ' + TROVE.totalResults +'</div>'
                $.each(data.response.zone[0].records.work, function(index, value){
                    //console.log(value.title);
                    buff += '<div class="result-box">';
                    //buff +=  '<a href="' + value.troveUrl + '">';
                    buff += '<p class="titleInfo">';
                    buff += '<span class="troveIdx">';
                    buff += '<b>'+ (index + TROVE.s + 1) +'</b>.&nbsp;';
                    buff += '</span>';
                    buff += '<span class="title"><a href="' + value.troveUrl + '">' + value.title + '</a></span></p>';
                    //buff +=  '<p class="contributors">';
                    //$.each(value.contributor, function(ci, cv){
                    //  console.log('contributor: ' + cv);
                    //  buff += cv;
                    //});
                    //buff +=  value.contributor;
                    //buff +=  '</p>';
                    //buff +=  '</a>';
                    buff +=  '</div>';
                });
                $('#'+TROVE.divId).html(buff);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            //console.log("Error....");
        }
    });
}