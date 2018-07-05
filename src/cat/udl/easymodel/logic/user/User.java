package cat.udl.easymodel.logic.user;

import cat.udl.easymodel.logic.types.UserType;

public interface User {

	Integer getId();

	void setId(Integer id);

	String getName();

	void setName(String name);

	UserType getUserType();

	void setType(UserType type);

	String getEncPassword();

	void setEncPassword(String encPassword);
}