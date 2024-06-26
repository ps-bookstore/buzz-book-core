package store.buzzbook.core.service.user.implement;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.buzzbook.core.common.exception.user.DeactivateUserException;
import store.buzzbook.core.common.exception.user.GradeNotFoundException;
import store.buzzbook.core.common.exception.user.UnknownUserException;
import store.buzzbook.core.common.exception.user.UserAlreadyExistsException;
import store.buzzbook.core.common.exception.user.UserNotFoundException;
import store.buzzbook.core.common.util.ZonedDateTimeParser;
import store.buzzbook.core.dto.user.LoginUserResponse;
import store.buzzbook.core.dto.user.RegisterUserRequest;
import store.buzzbook.core.dto.user.RegisterUserResponse;
import store.buzzbook.core.dto.user.UserInfo;
import store.buzzbook.core.entity.user.Deactivation;
import store.buzzbook.core.entity.user.Grade;
import store.buzzbook.core.entity.user.GradeLog;
import store.buzzbook.core.entity.user.GradeName;
import store.buzzbook.core.entity.user.User;
import store.buzzbook.core.entity.user.UserStatus;
import store.buzzbook.core.repository.user.DeactivationRepository;
import store.buzzbook.core.repository.user.GradeLogRepository;
import store.buzzbook.core.repository.user.GradeRepository;
import store.buzzbook.core.repository.user.UserRepository;
import store.buzzbook.core.service.user.UserService;

@Service
@Slf4j

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final GradeRepository gradeRepository;
	private final DeactivationRepository deactivationRepository;
	private final GradeLogRepository gradeLogRepository;

	@Override
	public LoginUserResponse requestLogin(String loginId) {
		User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UserNotFoundException(loginId));

		boolean isDeactivate = deactivationRepository.existsById(user.getId());

		if (isDeactivate) {
			log.warn("로그인 실패 : 탈퇴한 유저의 아이디({})입니다.", user.getId());
			throw new DeactivateUserException(loginId);
		}

		return LoginUserResponse.convertFrom(user);
	}

	@Override
	public UserInfo successLogin(String loginId) {
		log.info("최근 로그인 일자 업데이트 : {} ", loginId);

		if (!userRepository.updateLoginDate(loginId)) {
			log.warn("최근 로그인 일자 변경 재시도");
			userRepository.updateLoginDate(loginId);
		}

		return getUserInfoByLoginId(loginId);
	}

	@Transactional
	@Override
	public RegisterUserResponse requestRegister(RegisterUserRequest registerUserRequest) {
		String loginId = registerUserRequest.loginId();
		String successRegister = "회원가입 성공";

		Grade grade = gradeRepository.findByName(GradeName.NORMAL)
			.orElseThrow(() -> new GradeNotFoundException(GradeName.NORMAL.name()));

		if (userRepository.existsByLoginId(loginId)) {
			log.warn("유저 아이디 {} 중복 회원가입 실패 ", loginId);
			throw new UserAlreadyExistsException(loginId);
		}

		User requestUser = registerUserRequest.toUser();
		User savedUser = userRepository.save(requestUser);

		GradeLog gradeLog = GradeLog.builder()
			.grade(grade)
			.user(savedUser)
			.changeDate(ZonedDateTimeParser.toStringDate(ZonedDateTime.now()))
			.build();

		gradeLogRepository.save(gradeLog);
		//todo 쿠폰 처리

		return RegisterUserResponse.builder()
			.name(requestUser.getName())
			.loginId(requestUser.getLoginId())
			.status(200)
			.message(successRegister).build();
	}

	@Transactional
	@Override
	public boolean deactivate(Long userId, String reason) {
		Optional<User> user = null;
		// try {
		// 	user = userRepository.getReferenceById(userId);
		// } catch (EntityNotFoundException e) {
		// 	log.warn("탈퇴 요청 중 존재하지 않는 id의 요청 발생 : {}", userId);
		// 	throw new UserNotFoundException(userId);
		// }

		//user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		user = userRepository.findById(userId);
		if (user.isEmpty()) {
			throw new UserNotFoundException(userId);
		}

		Deactivation deactivation = Deactivation.builder()
			.deactivationAt(ZonedDateTime.now())
			.reason(reason)
			.user(user.get()).build();

		Deactivation savedData = deactivationRepository.save(deactivation);

		// if (userRepository.updateStatus(userId, UserStatus.WITHDRAW)) {
		// 	log.error("계정 탈퇴 중 오류가 발생했습니다. : {} ", userId);
		// 	throw new UnknownUserException(String.format("deactivate 이후 계정 상태 변경 중 오류가 발생했습니다. : %s ", userId));
		// }
		user.get().deactivate();

		userRepository.save(user.get());

		return savedData.getUser().getId().equals(userId);
	}

	@Override
	public void activate(String loginId) {

		if (!userRepository.existsByLoginId(loginId)) {
			log.warn("존재하지 않는 계정의 활성화 요청입니다. : {}", loginId);
			throw new UserNotFoundException(loginId);
		}

		if (userRepository.updateStatus(loginId, UserStatus.ACTIVE)) {
			log.error("계정 활성화 중 오류가 발생했습니다. : {}", loginId);
			throw new UnknownUserException(String.format("계정 상태 활성화 중 오류가 발생했습니다. : %s ", loginId));
		}

	}

	@Override
	public UserInfo getUserInfoByLoginId(String loginId) {
		User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UserNotFoundException(loginId));

		return UserInfo.builder()
			.id(user.getId())
			.name(user.getName())
			.loginId(user.getLoginId())
			.birthday(ZonedDateTimeParser.toDate(user.getBirthday()))
			.isAdmin(user.isAdmin())
			.contactNumber(user.getContactNumber())
			.email(user.getEmail())
			.build();
	}

}
