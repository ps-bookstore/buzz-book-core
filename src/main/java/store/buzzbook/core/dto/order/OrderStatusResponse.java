package store.buzzbook.core.dto.order;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderStatusResponse {
	private int id;
	private String name;
	private ZonedDateTime updateDate;
}
