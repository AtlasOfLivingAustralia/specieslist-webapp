package au.org.ala.specieslist

import org.springframework.web.context.request.RequestContextHolder

class AuthService {


    def email(){
        //println(RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes)
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.email)?:null
    }

    def firstname(){
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname)?:null
    }

    def surname(){
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.lastname)?:null
    }

    def isValidUserName(String username){
        //TODO check that the username is for a current CAS user
        true
    }

}
