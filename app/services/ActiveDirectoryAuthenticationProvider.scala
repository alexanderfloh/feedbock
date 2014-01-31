package services

import play.Application
import play.api.Play
import play.api.Logger
import com4j.COM4J
import com4j.Com4jObject
import com4j.ComException
import com4j.ExecutionException
import com4j.Variant
import com4j.typelibs.activeDirectory.IADs
import com4j.typelibs.activeDirectory.IADsGroup
import com4j.typelibs.activeDirectory.IADsOpenDSObject
import com4j.typelibs.activeDirectory.IADsUser
import com4j.typelibs.ado20.ClassFactory
import com4j.typelibs.ado20._Command
import com4j.typelibs.ado20._Connection
import com4j.typelibs.ado20._Recordset
import com4j.util.ComObjectCollector
import java.io.IOException
import java.util.ArrayList
import java.util.List
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions

case class User(val userName: String)

case class GrantedAuthority(auth: String)


/**
 * {@link AuthenticationProvider} with Active Directory, plus {@link UserDetailsService}
 *
 * @author Kohsuke Kawaguchi
 */
class ActiveDirectoryAuthenticationProvider(defaultNamingContext: String, con: _Connection) {

    /**
     * Converts a value of the "distinguished name" attribute of some AD object
     * and returns the "LDAP://..." URL to connect to it vis {@link IADsOpenDSObject#openDSObject(String, String, String, int)}
     *
     * AFAICT, MSDN doesn't document exactly describe how a value of the DN attribute is escaped,
     * but in my experiment with Windows 2008, it escapes <tt>,+\#<>;"=</tt> but not <tt>/</tt>
     *
     * This method must escape '/' since it needs to be escaped in LDAP:// URL, but we also need
     * to avoid double-escaping what's already escaped.
     *
     * @see <a href="http://www.rlmueller.net/CharactersEscaped.htm">source</a>
     */
    def dnToLdapUrl(dn: String) = "LDAP://"+dn.replace("/","\\/")

    def retrieveUser(username: String, password: String) : Option[User] = {
        val dn = getDnOfUserOrGroup(username)

        val col = new ComObjectCollector()
        COM4J.addListener(col);
        try {
            // now we got the DN of the user
             val dso = COM4J.getObject(classOf[IADsOpenDSObject], "LDAP:", null)

            // turns out we don't need DN for authentication
            // we can bind with the user name
            // dso.openDSObject("LDAP://"+context,args[0],args[1],1);

            // to do bind with DN as the user name, the flag must be 0
            var usr: IADsUser = null;
            try {
                usr = dso.openDSObject(dnToLdapUrl(dn), dn, password, ADS_READONLY_SERVER).queryInterface(classOf[IADsUser])                
            } catch { 
              case e:ComException => { 
                // this is failing
                val msg = f"Incorrect password for $username%s DN=${dn}%s: error=${e.getHRESULT}%08X"
                Logger.debug("Login failure: "+msg,e);
                None
              }
            }
            if (usr == null) {   // the user name was in fact a group
                Logger.warn(s"user not found: $username")
                return None
            }

            val groups = new ListBuffer[GrantedAuthority]()
            JavaConversions.iterableAsScalaIterable(usr.groups())
            	.filter(_ == null) // according to JENKINS-17357 in some environment the collection contains null
            	.foreach { g =>
              val grp = g.queryInterface(classOf[IADsGroup])
              groups.append(GrantedAuthority(grp.name().substring(3)))
            }
            
            //groups.add(SecurityRealm.AUTHENTICATED_AUTHORITY);

            Logger.info("Login successful: "+username+" dn="+dn);

//            return new ActiveDirectoryUserDetail(
//                username, password,
//                !isAccountDisabled(usr),
//                true, true, true,
//                groups.toArray(new GrantedAuthority[groups.size()]),
//                    getFullName(usr), getEmailAddress(usr), getTelephoneNumber(usr)
//            ).updateUserInfo();
            Some(User(username))
        } catch {
          case e:RuntimeException => {
            Logger.warn("exc: ", e)
            None
          } 
        } finally {
            col.disposeAll()
            COM4J.removeListener(col)
        }
    }
//
//    @Override
//    protected boolean canRetrieveUserByName() {
//        return true;
//    }
//
//    private String getTelephoneNumber(IADsUser usr) {
//        try {
//            Object t = usr.telephoneNumber();
//            return t==null ? null : t.toString();
//        } catch (ComException e) {
//            if (e.getHRESULT()==0x8000500D) // see http://support.microsoft.com/kb/243440
//                return null;
//            throw e;
//        }
//    }
//
//    private String getEmailAddress(IADsUser usr) {
//        try {
//            return usr.emailAddress();
//        } catch (ComException e) {
//            if (e.getHRESULT()==0x8000500D) // see http://support.microsoft.com/kb/243440
//                return null;
//            throw e;
//        }
//    }
//
//    private String getFullName(IADsUser usr) {
//        try {
//            return usr.fullName();
//        } catch (ComException e) {
//            if (e.getHRESULT()==0x8000500D) // see http://support.microsoft.com/kb/243440
//                return null;
//            throw e;
//        }
//    }
//
//    private boolean isAccountDisabled(IADsUser usr) {
//        try {
//            return usr.accountDisabled();
//        } catch (ComException e) {
//            if (e.getHRESULT()==0x8000500D)
//                /*
//                    See http://support.microsoft.com/kb/243440 and JENKINS-10086
//                    We suspect this to be caused by old directory items that do not have this value,
//                    so assume this account is enabled.
//                 */
//                return false;
//            throw e;
//        }
//    }
//
    private def getDnOfUserOrGroup(userOrGroupname: String) : String = {
		val cmd = ClassFactory.createCommand();
        cmd.activeConnection(con);

        cmd.commandText(s"<LDAP://$defaultNamingContext>;(sAMAccountName=$userOrGroupname);distinguishedName;subTree")
        val rs = cmd.execute(null, Variant.getMissing(), -1/*default*/)
        if(rs.eof())
            throw new RuntimeException(s"No such user or group: $userOrGroupname")

        val dn = rs.fields().item("distinguishedName").value().toString()
		dn
	}
//
//	public GroupDetails loadGroupByGroupname(String groupname) {
//        ActiveDirectoryGroupDetails details = groupCache.get(groupname);
//        if (details!=null)      return details;
//        throw new UsernameNotFoundException("Group not found: " + groupname);
//	}
//
//    /**
//     * {@link ActiveDirectoryGroupDetails} cache.
//     */
//    private final Cache<String,ActiveDirectoryGroupDetails,UsernameNotFoundException> groupCache = new Cache<String,ActiveDirectoryGroupDetails,UsernameNotFoundException>() {
//        @Override
//        protected ActiveDirectoryGroupDetails compute(String groupname) {
//            ComObjectCollector col = new ComObjectCollector();
//            COM4J.addListener(col);
//            try {
//                // First get the distinguishedName
//                String dn = getDnOfUserOrGroup(groupname);
//                IADsOpenDSObject dso = COM4J.getObject(IADsOpenDSObject.class, "LDAP:", null);
//                IADsGroup group = dso.openDSObject(dnToLdapUrl(dn), null, null, ADS_READONLY_SERVER)
//                        .queryInterface(IADsGroup.class);
//
//                // If not a group will return null
//                if (group == null)  return null;
//                return new ActiveDirectoryGroupDetails(groupname);
//            } catch (UsernameNotFoundException e) {
//                return null; // failed to convert group name to DN
//            } catch (ComException e) {
//                // recover gracefully since AD might behave in a way we haven't anticipated
//                LOGGER.log(Level.WARNING, "Failed to figure out details of AD group: "+groupname,e);
//                return null;
//            } finally {
//                col.disposeAll();
//                COM4J.removeListener(col);
//            }
//        }
//    };
//
//    private static final Logger LOGGER = Logger.getLogger(ActiveDirectoryAuthenticationProvider.class.getName());
//
//    /**
//     * Signify that we can connect to a read-only mirror.
//     *
//     * See http://msdn.microsoft.com/en-us/library/windows/desktop/aa772247(v=vs.85).aspx
//     */
    val ADS_READONLY_SERVER = 0x4

}
  
object ActiveDirectoryAuthenticationProvider {
  def apply() : ActiveDirectoryAuthenticationProvider= {
        try {
            val rootDSE = COM4J.getObject(classOf[IADs], "LDAP://RootDSE", null)

            val defaultNamingContext = rootDSE.get("defaultNamingContext").asInstanceOf[String];
            Logger.info("Active Directory domain is "+defaultNamingContext);

            val con = ClassFactory.createConnection();
            con.provider("ADsDSOObject");
            con.open("Active Directory Provider",""/*default*/,""/*default*/,-1/*default*/);
            new ActiveDirectoryAuthenticationProvider(defaultNamingContext, con)
        } catch {
          case e:ExecutionException =>
            throw new IOException("Failed to connect to Active Directory. Does this machine belong to Active Directory?",e)
        }
    }
  }

