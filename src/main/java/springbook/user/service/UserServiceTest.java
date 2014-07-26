package springbook.user.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserService.MIN_RECOMMEND_FOR_GOLD;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
	

	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}

	List<User> users;

	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("abcd", "가나다", "p1", Level.BASIC, 	MIN_LOGCOUNT_FOR_SILVER - 1, 0, "njir@naver.com"), 
				new User("bvcd", "나다라", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "njir@naver.com"), 
				new User("cdfd", "다라마", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1, "njir@naver.com"),
				new User("ddfdf", "라라라", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "njir@naver.com"), 
				new User("egfg", "마마마", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "njir@naver.com"));
	}

	 @Test
	 public void upgradeLevels(){
	 userDao.deleteAll();
	 for(User user : users) userDao.add(user);
	
	 userService.upgradeLevels();
	
	 checkLevelUpgraded(users.get(0), false);
	 checkLevelUpgraded(users.get(1), true);
	 checkLevelUpgraded(users.get(2), false);
	 checkLevelUpgraded(users.get(3), true);
	 checkLevelUpgraded(users.get(4), false);
	
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
		UserService testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(this.userDao);
		testUserService.setTransactionManager(transactionManager);
		userDao.deleteAll();

		for (User user : users)
			userDao.add(user);

		try {
			testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (Exception e) {
		}
		checkLevelUpgraded(users.get(1), false);
	}
}
