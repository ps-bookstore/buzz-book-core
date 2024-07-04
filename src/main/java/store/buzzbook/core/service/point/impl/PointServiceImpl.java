package store.buzzbook.core.service.point.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import store.buzzbook.core.common.exception.point.PointPolicyNotFoundException;
import store.buzzbook.core.common.exception.user.UserNotFoundException;
import store.buzzbook.core.dto.point.CreatePointPolicyRequest;
import store.buzzbook.core.dto.point.DeletePointPolicyRequest;
import store.buzzbook.core.dto.point.PointLogResponse;
import store.buzzbook.core.dto.point.PointPolicyResponse;
import store.buzzbook.core.dto.point.UpdatePointPolicyRequest;
import store.buzzbook.core.entity.point.PointLog;
import store.buzzbook.core.entity.point.PointPolicy;
import store.buzzbook.core.entity.user.User;
import store.buzzbook.core.repository.point.PointLogRepository;
import store.buzzbook.core.repository.point.PointPolicyRepository;
import store.buzzbook.core.repository.user.UserRepository;
import store.buzzbook.core.service.point.PointService;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

	private final PointPolicyRepository pointPolicyRepository;
	private final PointLogRepository pointLogRepository;
	private final UserRepository userRepository;

	@Override
	public PointPolicyResponse createPointPolicy(CreatePointPolicyRequest request) {
		return PointPolicyResponse.from(pointPolicyRepository.save(request.toEntity()));
	}

	@Override
	public List<PointPolicyResponse> getPointPolicies() {
		return pointPolicyRepository.findAll().stream()
			.map(PointPolicyResponse::from)
			.toList();
	}

	@Override
	public void updatePointPolicy(UpdatePointPolicyRequest request) {
		PointPolicy pointPolicy = pointPolicyRepository.findById(request.id())
			.orElseThrow(PointPolicyNotFoundException::new);

		pointPolicy.changePoint(request.point());
		pointPolicy.changeRate(request.rate());
	}

	@Override
	public void deletePointPolicy(DeletePointPolicyRequest request) {
		PointPolicy pointPolicy = pointPolicyRepository.findById(request.id())
			.orElseThrow(PointPolicyNotFoundException::new);

		pointPolicy.delete();
	}

	@Override
	public List<PointLogResponse> getPointLogs(Pageable pageable) {
		return pointLogRepository.findAll(pageable).stream().map(PointLogResponse::from).toList();
	}

	public PointLog createPointLogWithDelta(long userId, String inquiry, int deltaPoint) {
		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			throw new UserNotFoundException(userId);
		}
		PointLog lastPointLog = pointLogRepository.findLastByUserId(userId);
		PointLog newPointLog;
		if (lastPointLog == null) {
			newPointLog = PointLog.builder()
				.createdAt(LocalDateTime.now())
				.inquiry(inquiry)
				.delta(deltaPoint)
				.user(user)
				.balance(deltaPoint)
				.build();
		} else {
			newPointLog = PointLog.builder()
				.createdAt(LocalDateTime.now())
				.inquiry(inquiry)
				.delta(deltaPoint)
				.user(user)
				.balance(lastPointLog.getBalance() + deltaPoint)
				.build();
		}
		return pointLogRepository.save(newPointLog);
	}

	public PointLog createPointLogWithDelta(User user, String inquiry, int deltaPoint) {
		return createPointLogWithDelta(user.getId(), inquiry, deltaPoint);
	}
}
