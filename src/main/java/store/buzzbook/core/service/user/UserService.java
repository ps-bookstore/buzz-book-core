package store.buzzbook.core.service.user;

import store.buzzbook.core.dto.coupon.CreateUserCouponRequest;
import store.buzzbook.core.dto.user.ChangePasswordRequest;
import store.buzzbook.core.dto.user.DeactivateUserRequest;
import store.buzzbook.core.dto.user.LoginUserResponse;
import store.buzzbook.core.dto.user.RegisterUserRequest;
import store.buzzbook.core.dto.user.RegisterUserResponse;
import store.buzzbook.core.dto.user.UpdateUserRequest;
import store.buzzbook.core.dto.user.UserInfo;

public interface UserService {
	LoginUserResponse requestLogin(String loginId);

	UserInfo successLogin(String loginId);

	RegisterUserResponse requestRegister(RegisterUserRequest registerUserRequest);

	void deactivate(Long userId, DeactivateUserRequest deactivateUserRequest);

	void activate(String loginId);

	UserInfo updateUserInfo(Long userId, UpdateUserRequest updateUserRequest);

	UserInfo getUserInfoByUserId(Long userId);

	UserInfo getUserInfoByLoginId(String loginId);

	void addUserCoupon(CreateUserCouponRequest request);

	void updatePassword(Long userId, ChangePasswordRequest changePasswordRequest);
}
