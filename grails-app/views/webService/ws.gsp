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
    <meta name="layout" content="ala2"/>
    <title>Species lists Webservices| Atlas of Living Australia</title>
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
            <li>Retrieve individual list - GET call to /ws/speciesList/{dataResourceUid} : Return a single JSON record with the information for the supplied list.</li>
            <li>Retrieve all lists - GET call to /ws/speciesList : Returns a list of JSON records containing the species lists.  This operation supports paging adn sorting.
            The follow parameters are supported:
                <ul>
                    <li><b>user</b> - A user name from CAS.  When this is provided and no other sort params lists provided by the supplied user appear first.</li>
                    <li><b>sort</b> - The field on which to sort. Use one of the following; listName, username, listType, count</li>
                    <li><b>order</b> - The order that the sort is performed.  Either asc or desc</li>
                    <li><b>offset</b> - The number of records to skip.  This supports paging.</li>
                    <li><b>max</b> - The maximum number of lists to return.  This supports paging.</li>
                </ul>
            </li>
            <li>Create a new Species list - POST to /ws/speciesList with the following information in a JSON body:
            <ul>
                   <li><b>listName</b> - The name for the list</li>
                   <li><b>listItems</b> - A comma separated list of species names.</li>
                   <li><b>listType</b> - The type of list; SPECIES_CHARACTERS, CONSERVATION_LIST, SENSITIVE_LIST, LOCAL_LIST, COMMON_TRAIT,
                   COMMON_HABITAT, SPATIAL_PORTAL, TEST, OTHER</li>
                   <li><b>description</b> - A description for the list.</li>
            </ul>
                At the moment this service does not accept properties for list items.  This will probably need to be implement as a different service.
            </li>
        </ul>
        <h3>Species List Item Services</h3>
        <ul>
            <li>Retrieve the items from the supplied list - GET to /ws/speciesListItems/{dataResourceUid}.  At the moment this service only returns the id, name and lsid for the item.</li>
        </ul>
        <h3>Species based services</h3>
        <ul>
            <li>Retrieve all lists on which the supplied guid appears - GET call to /ws/species/{guid}.  This service will also return all the properties
            associated with the species.</li>
        </ul>
     </div>
    </div>
</body>