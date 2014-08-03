package springbook.user.service;

import java.util.List;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserServiceImpl implements UserService {
	UserDao userDao;
	private MailSender mailSender;
	
	public void setUserDao(UserDao userDao){
		this.userDao = userDao;
	}
	
	
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}


	
	public void upgradeLevels(){
		List<User> users = userDao.getAll();
		for(User user : users){
			if(canUpgradeLevel(user)){
				upgradeLevel(user);
			}
		}
	}

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
		sendUpgradeEmail(user);
	}
	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;

	private boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		switch (currentLevel) {
		case BASIC:
			return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
		case SILVER:
			return (user.getRecommend()>=MIN_RECOMMEND_FOR_GOLD);
		case GOLD: return false;
		default: throw new IllegalArgumentException("Unknown Level: "+ 
		currentLevel);
		}
	}

	public void add(User user) {
		if(user.getLevel() == null ) user.setLevel(Level.BASIC);
		userDao.add(user);
	}
	
	private void sendUpgradeEmail(User user){
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("njir@naver.com");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());

		this.mailSender.send(mailMessage);
	}
}
