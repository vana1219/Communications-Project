package Testing;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
//import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	TestAdmin.class, 
	TestChatBox.class,
	TestUser.class,
	TestMessage.class,
	TestServer.class
})
public class AllTests {
}
