import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserDaoTest {	
	@Autowired
	private ApplicationContext context;
	UserDao dao;
	
	@Before
	public void setUp(){
		 this.dao = this.context.getBean("userDao", UserDao.class);
	}
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		User user1 = new User("gyum", "박상철", "springno1");
		User user2 = new User("abcdf", "가나다", "springno2");
		
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		dao.add(user1);
		dao.add(user2);
		assertThat(dao.getCount(), is(2));

		User userget1 = dao.get(user1.getId());
		assertThat(userget1.getName(), is(user1.getName()));
		assertThat(userget1.getPassword(), is(user1.getPassword()));
		
		User userget2 = dao.get(user2.getId());
		assertThat(userget2.getName(), is(user2.getName()));
		assertThat(userget2.getPassword(), is(user2.getPassword()));
	}
	
	@Test
	public void count() throws SQLException, ClassNotFoundException{
		
		User user1 = new User("gyum", "박상철", "springno1");
		User user2 = new User("abcdf", "가나다", "springno2");
		User user3 = new User("bvvccv", "아자차카", "springno3");
		
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		dao.add(user1);
		assertThat(dao.getCount(), is(1));
		dao.add(user2);
		assertThat(dao.getCount(), is(2));
		dao.add(user3);
		assertThat(dao.getCount(), is(3));
		
	
	}
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws SQLException, ClassNotFoundException{
		//ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
		//UserDao dao = context.getBean("userDao", UserDao.class);

		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		dao.get("unknown_id");
	}
}
