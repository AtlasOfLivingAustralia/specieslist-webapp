#!/bin/sh

# sample url
#login_site=https://auth.ala.org.au/cas/login?service=http://diasbtest1.ala.org.au:8080/bie-webapp/admin/optimise
#ws_site=http://diasbtest1.ala.org.au:8080/bie-webapp/admin/optimise

login_site=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/optimise
username=your.name@csiro.au
password=yourPassword
ws_site=http://bie.ala.org.au/admin/optimise

cookies=cookies.txt

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_site}"

# authenicate with CAS server.
wget -d --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_site}"

# call optimise index ws.
wget -d --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies \
	--header="Cookie: $cookie" $ws_site
