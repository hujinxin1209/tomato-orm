package test.entity;

import javax.persistence.Column;

import com.tomato.orm.Model;

public class User  extends Model<User>{//继承Model成为一个Model
	@Column(name = "id")
	private Long id;//主键名称约定为id,后期可以使用注解来标识
	@Column(name = "user_name")
	private String username;
	@Column(name = "age")
	private Integer age;
	@Column(name = "password")
	private String password;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}