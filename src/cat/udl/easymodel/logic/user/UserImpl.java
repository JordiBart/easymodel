package cat.udl.easymodel.logic.user;

import cat.udl.easymodel.logic.types.UserType;

public class UserImpl implements User {
	private Integer id;
	private String name;
	private String encPassword;
	private UserType type;
	
	public UserImpl(Integer id ,String name, String pass, UserType type) {
		this.id=id;
		this.name=name;
		this.encPassword = pass;
		this.type=type;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public UserType getUserType() {
		return type;
	}

	@Override
	public void setType(UserType type) {
		this.type = type;
	}

	@Override
	public String getEncPassword() {
		return encPassword;
	}

	@Override
	public void setEncPassword(String encPassword) {
		this.encPassword = encPassword;
	}
}
