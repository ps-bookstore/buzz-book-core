package store.buzzbook.core.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadOrderStatusByIdRequest {
	private int statusId;
}
