package jace.shim.springcamp2017.product.model;

import jace.shim.springcamp2017.core.domain.AggregateRoot;
import jace.shim.springcamp2017.core.exception.EventApplyException;
import jace.shim.springcamp2017.product.model.command.*;
import jace.shim.springcamp2017.product.model.event.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Created by jaceshim on 2017. 3. 3..
 */
@Getter
public class Product extends AggregateRoot<Long> {

	/** 상품 명 */
	private String name;
	/** 상품 가격 */
	private int price;
	/** 상품 수량 */
	private int quantity;
	/** 상품 설명 */
	private String description;
	/** 상품 등록일시 */
	private LocalDateTime created;
	/** 상품 수정일시 */
	private LocalDateTime updated;

	public Product(Long identifier) {
		super(identifier);
	}

	public Product(Long productId, String name, int price, int quantity, String description) {
		super(productId);
		applyChange(new ProductCreated(productId, name, price, quantity, description));
	}

	/**
	 * 상품 등록
	 *
	 * @param productCreateCommand
	 * @return
	 * @throws EventApplyException
	 */
	public static Product create(ProductCommand.CreateProduct productCreateCommand) {
		Long productId = createProductId();
		Product product = new Product(productId, productCreateCommand.getName(), productCreateCommand.getPrice(), productCreateCommand.getQuantity(),
			productCreateCommand.getDescription());

		return product;
	}

	/**
	 * 상품아이디 생성
	 * @return
	 */
	private static long createProductId() {
		long min = 1000000L;
		long max = 9999999L;
		return min + (long) (Math.random() * (max - min));
	}

	/**
	 * 상품생성 이벤트 반영
	 *
	 * @param event
	 */
	public void apply(ProductCreated event) {
		this.name = event.getName();
		this.price = event.getPrice();
		this.quantity = event.getQuantity();
		this.description = event.getDescription();
		this.created = event.getCreated();
	}

	/**
	 * 상품명 변경
	 *
	 * @param productChangeNameCommand
	 * @throws EventApplyException
	 */
	public void changeName(ProductCommand.ChangeName productChangeNameCommand) {
		this.name = productChangeNameCommand.getName();
		applyChange(new ProductNameChanged(this.getIdentifier(), this.getName()));
	}

	/**
	 * 상품명 변경 이벤트 반영
	 * @param event
	 */
	public void apply(ProductNameChanged event) {
		this.name = event.getName();
		this.updated = event.getUpdated();
	}

	/**
	 * 상품 가격 변경
	 * @param productChangePriceCommand
	 */
	public void changePrice(ProductCommand.ChangePrice productChangePriceCommand) {
		this.price = productChangePriceCommand.getPrice();
		applyChange(new ProductPriceChanged(this.getIdentifier(), this.getPrice()));
	}

	public void apply(ProductPriceChanged event) {
		this.price = event.getPrice();
		this.updated = event.getUpdated();
	}

	/**
	 * 상품 판매수량 증가
	 * @param productIncreaseQuantityCommand
	 * @throws EventApplyException
	 */
	public void increaseQuantity(ProductCommand.IncreaseQuantity productIncreaseQuantityCommand) {
		this.quantity = (this.getQuantity() + productIncreaseQuantityCommand.getQuantity());
		applyChange(new ProductQuantityIncreased(this.getIdentifier(), this.getQuantity()));
	}

	public void apply(ProductQuantityIncreased event) {
		this.quantity = event.getQuantity();
		this.updated = event.getUpdated();
	}

	/**
	 * 상품 판매수량 감소
	 * @param productDecreaseQuantityCommand
	 * @throws EventApplyException
	 */
	public void decreaseQuantity(ProductCommand.DecreaseQuantity productDecreaseQuantityCommand) {
		if (this.getQuantity() < productDecreaseQuantityCommand.getQuantity()) {
			throw new IllegalStateException("재고 수량이 부족합니다.");
		}

		this.quantity = (this.getQuantity() - productDecreaseQuantityCommand.getQuantity());
		applyChange(new ProductQuantityDecreased(this.getIdentifier(), this.getQuantity()));
	}

	public void apply(ProductQuantityDecreased event) {
		this.quantity = event.getQuantity();
		this.updated = event.getUpdated();
	}
}