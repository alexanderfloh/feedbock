package services.auth

class MockAuthenticationProvider extends AuthenticationProvider {
	val users = List(("user1" , "asdf"))
  
	def retrieveUser(username: String, password:String) : Option[User] = {
	  if(users.contains((username, password))) Some(User(username))
	  else None
	}
}