<%--
  Created by IntelliJ IDEA.
  User: car61w
  Date: 20/03/13
  Time: 3:41 PM
  To change this template use File | Settings | File Templates.
--%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Species lists Webservices| Atlas of Living Australia</title>
    <style type="text/css">
        code {
            color: #df4a21;
            border: none;
            background-color: transparent;
            font-size: 90%;
        }
    </style>
</head>
<body class="">
    <div id="content">
        <div class="inner">
        <h2>Species List Web Services</h2>
        <p>
        These webservices provide search and edit facilities for the species list.
        Please send any bug reports, suggestions for improvements or new services to: developers 'AT' ala.org.au
        </p>
        <h3>
         Species List Services
        </h3>
        <ul>
            <li>Retrieve individual list - GET call to <code>/ws/speciesList/{dataResourceUid}</code> returns a single JSON record with the information for the supplied list.</li>
            <li>Retrieve all lists - GET call to <code>/ws/speciesList</code> returns a list of JSON records containing the species lists.  This operation supports paging adn sorting.
            The follow parameters are supported:
                <ul>
                    <li><b>user</b> - A user name from CAS.  When this is provided and no other sort params lists provided by the supplied user appear first.</li>
                    <li><b>sort</b> - The field on which to sort. Use one of the following: <code>listName, username, listType, count</code></li>
                    <li><b>order</b> - The order that the sort is performed.  Either <code>asc</code> or <code>desc</code></li>
                    <li><b>offset</b> - The number of records to skip.  This supports paging.</li>
                    <li><b>max</b> - The maximum number of lists to return.  This supports paging.</li>
                </ul>
            </li>
            <li>Create a new Species list - POST to <code>/ws/speciesList</code> with the following information in a JSON body:
                <ul>
                       <li><b>listName</b> - The name for the list</li>
                       <li><b>listItems</b> - A comma separated list of species names.</li>
                       <li><b>listType</b> - The type of list: <code>SPECIES_CHARACTERS, CONSERVATION_LIST, SENSITIVE_LIST, LOCAL_LIST, COMMON_TRAIT,
                       COMMON_HABITAT, SPATIAL_PORTAL, TEST, OTHER</code></li>
                       <li><b>description</b> - A description for the list.</li>
                </ul>
                At the moment this service does not accept properties for list items.  This will probably need to be implement as a different service.
            </li>
        </ul>
        <h3>Species List Item Services</h3>
        <ul>
            <li>Retrieve the items from the supplied list - GET to <code>/ws/speciesListItems/{dataResourceUid}</code>.  At the moment this service only returns the id, name and lsid for the item.</li>
        </ul>
        <h3>Species based services</h3>
        <ul>
            <li>Retrieve all lists on which the supplied guid appears - GET call to <code>/ws/species/{guid}</code>.  This service will also return all the properties
            associated with the species.</li>
        </ul>
     </div>
    </div>
</body>