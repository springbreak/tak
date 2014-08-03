package springbook.user.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {
	@Autowired
	UserService userService;
	@Autowired
	UserDao userDao;
	@Autowired
	DataSource dataSource;
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	MailSender mailSender;
	@Autowired
	UserServiceImpl userServiceImpl;

	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}

	List<User> users;

	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("joytouch", "가나다", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "njir@naver.com"), 
				new User("abcd", "나다라", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0,	"njir@naver.com"), 
				new User("madnite1", "다라마", "p3", Level.SILVER,	60, MIN_RECOMMEND_FOR_GOLD - 1, "njir@naver.com"), 
				new User("ddfdf", "라라라", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD,	"njir@naver.com"), 
				new User("egfg", "마마마", "p5", Level.GOLD,	100, Integer.MAX_VALUE, "njir@naver.com"));
	}

	@Test
	public void upgradeLevels() throws Exception{
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		
		checkUserAndLevel(updated.get(0), "abcd", Level.SILVER);
		checkUserAndLevel(updated.get(1), "ddfdf", Level.GOLD);
		
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));

	}

	@Test
	public void upgradeAllOrNothing() throws Exception {
		TestUserService testUserService = new TestUserService(users.get(3)
				.getId());
		testUserService.setUserDao(userDao);
		testUserService.setMailSender(mailSender);

		UserServiceTx txUserService = new UserServiceTx();
		txUserService.setTransactionManager(transactionManager);
		txUserService.setUserService(testUserService);

		userDao.deleteAll();
		for (User user : users)
			userDao.add(user);

		try {
			txUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (Exception e) {
		}
		checkLevelUpgraded(users.get(1), false);

		testUserService.setMailSender(mailSender);
	}

	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();

		public List<String> getRequests() {
			return requests;
		}

		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}

		public void send(SimpleMailMessage[] mailMessage) throws MailException {
		}
	}
	
	static class MockUserDao implements UserDao{
		private List<User> users;
		private List<User> updated = new ArrayList<User>();
		
		private MockUserDao(List<User> users){
			this.users = users;
		}
		
		public List<User> getUpdated(){
			return this.updated;
		}
		
		public List<User> getAll(){
			return this.users;
		}
		
		public void update(User user){
			updated.add(user);
		}
		
		public void add(User user) { 
			throw new UnsupportedOperationException();
		}
		
		public User get(String id) {
			throw new UnsupportedOperationException();
		}

		public void deleteAll() {
			throw new UnsupportedOperationException();
		}

		public int getCount() {
			throw new UnsupportedOperationException();
		}
		
	}
}
