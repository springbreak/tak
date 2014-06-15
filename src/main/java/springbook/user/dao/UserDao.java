package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import springbook.user.domain.User;

public class UserDao {
	@Autowired
	private DataSource dataSource;

<<<<<<< HEAD
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void add(final User user) throws SQLException {
		this.jdbcTemplate.update(
				"INSERT INTO users(id, name, password) VALUES(?, ?, ?);",
				user.getId(), user.getName(), user.getPassword());

=======
	public void add(final User user) throws SQLException {
		jdbcContextWithStatementStrategy(new StatementStrategy() {
			public PreparedStatement makePreparedStatement(Connection c)
					throws SQLException {
				PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
				ps.setString(1,  user.getId());
				ps.setString(2,  user.getName());
				ps.setString(3,  user.getPassword());
				
				return ps;
				}
				}
		);
>>>>>>> origin/master
	}

	public User get(String id) throws ClassNotFoundException, SQLException {
		return this.jdbcTemplate.queryForObject(
				"select * from users where id = ?", new Object[] { id },
				new RowMapper<User>() {
					public User mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						User user = new User();
						user.setId(rs.getString("id"));
						user.setName(rs.getString("name"));
						user.setPassword(rs.getString("password"));
						return user;
					}
				});
	}
<<<<<<< HEAD

	public void deleteAll() throws SQLException {
		this.jdbcTemplate.update("delete from users");
	}

	@SuppressWarnings("deprecation")
	public int getCount() throws SQLException {
		return this.jdbcTemplate.queryForInt("select count(*) from users");
=======
	
	public void deleteAll() throws SQLException{
		jdbcContextWithStatementStrategy(
				new StatementStrategy() {
					public PreparedStatement makePreparedStatement(Connection c)
							throws SQLException {
						return c.prepareStatement("delete from users");
					}
				}
				);
	}
			
		
	private void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException{
		Connection c = null;
		PreparedStatement ps = null;
		
		try{
			c = dataSource.getConnection();
			ps = stmt.makePreparedStatement(c);
			ps.executeUpdate();
		}catch(SQLException e){
			throw e;
		}finally{
			if(ps != null){try{ps.close();}catch(SQLException e){}}
			if(c != null){try{c.close();}catch(SQLException e){}}
			
		}
>>>>>>> origin/master
	}

	public List<User> getAll(){
		return this.jdbcTemplate.query("select * from users order by id",
				new RowMapper<User>(){
					public User mapRow(ResultSet rs, int rowNum)
							throws SQLException{
						User user = new User();
						user.setId(rs.getString("id"));
						user.setName(rs.getString("name"));
						user.setPassword(rs.getString("password"));
						return user;
			}
		});
	}
}
