package store.buzzbook.core.service.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import store.buzzbook.core.dto.order.CreateOrderDetailRequest;
import store.buzzbook.core.dto.order.CreateOrderRequest;
import store.buzzbook.core.dto.order.CreateOrderStatusRequest;
import store.buzzbook.core.dto.order.ReadOrderStatusResponse;
import store.buzzbook.core.dto.order.ReadOrderDetailResponse;
import store.buzzbook.core.dto.order.ReadOrderResponse;
import store.buzzbook.core.dto.order.UpdateOrderDetailRequest;
import store.buzzbook.core.dto.order.UpdateOrderRequest;
import store.buzzbook.core.dto.order.UpdateOrderStatusRequest;
import store.buzzbook.core.entity.order.DeliveryPolicy;
import store.buzzbook.core.entity.order.Order;
import store.buzzbook.core.entity.order.OrderDetail;
import store.buzzbook.core.entity.order.OrderStatus;
import store.buzzbook.core.entity.order.Wrapping;
import store.buzzbook.core.entity.product.Product;
import store.buzzbook.core.mapper.order.OrderDetailMapper;
import store.buzzbook.core.mapper.order.OrderMapper;
import store.buzzbook.core.mapper.order.OrderStatusMapper;
import store.buzzbook.core.repository.order.DeliveryPolicyRepository;
import store.buzzbook.core.repository.order.OrderDetailRepository;
import store.buzzbook.core.repository.order.OrderRepository;
import store.buzzbook.core.repository.order.OrderStatusRepository;
import store.buzzbook.core.repository.order.WrappingRepository;
import store.buzzbook.core.repository.product.ProductRepository;
import store.buzzbook.core.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final UserRepository userRepository;
	private final DeliveryPolicyRepository deliveryPolicyRepository;
	private final WrappingRepository wrappingRepository;
	private final ProductRepository productRepository;
	private final OrderStatusRepository orderStatusRepository;

	public Page<ReadOrderResponse> readOrders(Pageable pageable) {
		Page<Order> orders = orderRepository.findAll(pageable);
		List<ReadOrderResponse> responses = new ArrayList<>();

		for (Order order : orders) {
			List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrder_Id(order.getId());
			List<ReadOrderDetailResponse> details = new ArrayList<>();

			for (OrderDetail orderDetail : orderDetails) {
				details.add(OrderDetailMapper.toDto(orderDetail));
			}
			responses.add(OrderMapper.toDto(order, details));
		}

		return new PageImpl<>(responses, pageable, orders.getTotalElements());
	}

	public Page<ReadOrderResponse> readMyOrders(long userId, Pageable pageable) {
		userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Page<Order> orders = orderRepository.findByUser_Id(userId, pageable);
		List<ReadOrderResponse> responses = new ArrayList<>();

		for (Order order : orders) {
			List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrder_Id(order.getId());
			List<ReadOrderDetailResponse> details = new ArrayList<>();

			for (OrderDetail orderDetail : orderDetails) {
				details.add(OrderDetailMapper.toDto(orderDetail));
			}
			responses.add(OrderMapper.toDto(order, details));
		}

		return new PageImpl<>(responses, pageable, orders.getTotalElements());
	}

	public ReadOrderResponse readOrder(long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrder_Id(orderId);
		List<ReadOrderDetailResponse> details = new ArrayList<>();
		for (OrderDetail orderDetail : orderDetails) {
			details.add(OrderDetailMapper.toDto(orderDetail));
		}
		return OrderMapper.toDto(order, details);
	}

	@Transactional
	public ReadOrderResponse createOrder(CreateOrderRequest createOrderRequest) {
		DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findById(createOrderRequest
			.getDeliveryPolicyId()).orElseThrow(()-> new IllegalArgumentException("Delivery Policy not found"));

		List<CreateOrderDetailRequest> details = createOrderRequest.getDetails();

		Order order = OrderMapper.toEntity(createOrderRequest, deliveryPolicy);

		order = orderRepository.save(order);

		List<ReadOrderDetailResponse> readOrderDetailRespons = new ArrayList<>();

		for (CreateOrderDetailRequest detail : details) {
			OrderStatus orderStatus = orderStatusRepository.findById(detail.getOrderStatusId())
				.orElseThrow(()-> new IllegalArgumentException("Order Status not found"));
			Wrapping wrapping = wrappingRepository.findById(detail.getWrappingId())
				.orElseThrow(()-> new IllegalArgumentException("Wrapping not found"));
			Product product = productRepository.findById(detail.getProductId())
				.orElseThrow(()-> new IllegalArgumentException("Product not found"));
			OrderDetail orderDetail = OrderDetailMapper.toEntity(detail, order, wrapping, product, orderStatus);
			orderDetail = orderDetailRepository.save(orderDetail);
			readOrderDetailRespons.add(OrderDetailMapper.toDto(orderDetail));
		}

		return OrderMapper.toDto(order, readOrderDetailRespons);
	}

	public ReadOrderResponse updateOrder(UpdateOrderRequest updateOrderRequest) {
		Order order = orderRepository.findById(updateOrderRequest.getId())
			.orElseThrow(()-> new IllegalArgumentException("Order not found"));
		List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrder_Id(updateOrderRequest.getId());
		List<ReadOrderDetailResponse> readOrderDetailRespons = new ArrayList<>();

		for (OrderDetail orderDetail : orderDetails) {
			for (int orderStatusId : updateOrderRequest.getDetails().stream().filter(d-> orderDetail.getId() == updateOrderRequest.getId()).map(
				UpdateOrderDetailRequest::getOrderStatusId).toList()) {
				orderDetail.setOrderStatus(orderStatusRepository.findById(orderStatusId).orElseThrow(() -> new IllegalArgumentException("Order Status not found")));
				readOrderDetailRespons.add(OrderDetailMapper.toDto(orderDetailRepository.save(orderDetail)));
			}
		}

		return OrderMapper.toDto(order, readOrderDetailRespons);
	}

	public List<ReadOrderDetailResponse> readOrderDetails(long orderId) {
		List<ReadOrderDetailResponse> readOrderDetailRespons = new ArrayList<>();
		for (OrderDetail orderDetail : orderDetailRepository.findAllByOrder_Id(orderId)) {
			readOrderDetailRespons.add(OrderDetailMapper.toDto(orderDetail));
		}

		return readOrderDetailRespons;
	}

	public ReadOrderStatusResponse createOrderStatus(CreateOrderStatusRequest createOrderStatusRequest) {

		return OrderStatusMapper.toDto(orderStatusRepository.save(OrderStatus.builder().name(createOrderStatusRequest.getName()).build()));
	}

	public ReadOrderStatusResponse updateOrderStatus(UpdateOrderStatusRequest updateOrderStatusRequest) {

		return OrderStatusMapper.toDto(orderStatusRepository.save(OrderStatus.builder().id(updateOrderStatusRequest.getId()).name(updateOrderStatusRequest.getName()).build()));
	}

	public void deleteOrderStatus(int orderStatusId) {
		orderStatusRepository.delete(orderStatusRepository.findById(orderStatusId).orElseThrow(() -> new IllegalArgumentException("Order Status not found")));
	}

	public ReadOrderStatusResponse readOrderStatusByName(String orderStatusName) {
		return OrderStatusMapper.toDto(orderStatusRepository.findByName(orderStatusName));
	}

	public List<ReadOrderStatusResponse> readAllOrderStatus() {
		return orderStatusRepository.findAll().stream().map(OrderStatusMapper::toDto).toList();
	}
}
