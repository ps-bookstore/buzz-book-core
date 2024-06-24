package store.buzzbook.core.service.product;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import store.buzzbook.core.dto.product.response.BookRequest;
import store.buzzbook.core.dto.product.response.BookResponse;
import store.buzzbook.core.entity.product.Book;
import store.buzzbook.core.entity.product.Product;
import store.buzzbook.core.entity.product.Publisher;
import store.buzzbook.core.repository.product.AuthorRepository;
import store.buzzbook.core.repository.product.BookAuthorRepository;
import store.buzzbook.core.repository.product.BookRepository;
import store.buzzbook.core.repository.product.ProductRepository;
import store.buzzbook.core.repository.product.PublisherRepository;

@RequiredArgsConstructor
@Service
public class BookService {

	private final BookAuthorRepository bookAuthorRepository;
	private final AuthorRepository authorRepository;
	private final BookRepository bookRepository;
	private final PublisherRepository publisherRepository;
	private final ProductRepository productRepository;

	public Book saveBook(Book book) {
		return bookRepository.save(book);
	}

	public Book saveBook(BookRequest bookReq) {
		Publisher publisher = publisherRepository.findByName(bookReq.getPublisher());

		if (publisher == null) {
			publisher = publisherRepository.save(new Publisher(bookReq.getPublisher()));
		}

		Book newBook = Book.builder()
			.title(bookReq.getTitle())
			.description(bookReq.getDescription())
			.isbn(bookReq.getIsbn())
			.publisher(publisher)
			.publishDate(bookReq.getPublishDate().toString())
			.build();
		return saveBook(newBook);
	}

	public List<BookResponse> getAllBooks() {
		return bookRepository.findAll().stream()
			.map(BookResponse::convertToBookResponse)
			.toList();
	}

	public List<BookResponse> getAllBooksExistProductId() {
		return bookRepository.findAllByProductIdIsNotNull().stream()
			.map(BookResponse::convertToBookResponse)
			.toList();
	}

	public BookResponse getBookById(long id) {
		Book book = bookRepository.findById(id).orElse(null);
		if (book == null) {
			throw new RuntimeException("book not found");
		}
		return BookResponse.convertToBookResponse(book);
	}

	public BookResponse getBookByProductId(int productId) {
		Book book = bookRepository.findByProductId(productId);
		if (book == null) {
			throw new RuntimeException("book not found");
		}
		return BookResponse.convertToBookResponse(book);
	}

	public List<BookResponse> getBooksByProductIdList(List<Integer> productIdList) {
		List<Book> bookList = bookRepository.findAllByProductIdIn(productIdList);
		return bookList.stream().map(BookResponse::convertToBookResponse).toList();
	}

	// public List<BookResponse> getAllBooks() {
	// 	List<Book> books = bookRepository.findAll();
	// 	return books.stream().map(this::fetchBookAuthorsAndConvertToBookResponse).toList();
	// }
	//
	// public List<BookResponse> getAllBooksExistProductId() {
	// 	List<Book> bookList = bookRepository.findAllByProductIdIsNotNull();
	// 	return bookList.stream().map(this::fetchBookAuthorsAndConvertToBookResponse).toList();
	// }
	//
	// public BookResponse getBookById(long id) {
	// 	Book book = bookRepository.findById(id).orElse(null);
	// 	return fetchBookAuthorsAndConvertToBookResponse(book);
	// }
	//
	// public BookResponse getBookByProductId(int productId) {
	// 	Book book = bookRepository.findByProductId(productId);
	// 	return fetchBookAuthorsAndConvertToBookResponse(book);
	// }
	//
	// public List<BookResponse> getBooksByProductIdList(List<Integer> productIdList) {
	// 	List<Book> bookList = bookRepository.findAllByProductIdIn(productIdList);
	// 	return bookList.stream().map(this::fetchBookAuthorsAndConvertToBookResponse).toList();
	// }
	//
	// //book으로 book_author와 author 테이블을 뒤져서 response로 매핑해주는 메소드
	// private BookResponse fetchBookAuthorsAndConvertToBookResponse(Book book) {
	// 	if (book == null) {
	// 		return null;
	// 	}
	// 	List<BookAuthor> bookAuthorList = bookAuthorRepository.findAllByBookId(book.getId());
	// 	List<Integer> bookAuthorIdList = bookAuthorList.stream().map(BookAuthor::getId).toList();
	// 	List<Author> authorList = authorRepository.findAllByIdIn(bookAuthorIdList);
	// 	return BookResponse.convertToBookResponse(book, authorList);
	// }

	public BookResponse deleteBookById(long id) {
		Book book = bookRepository.findById(id).orElseThrow();
		Product product = productRepository.findById(book.getProduct().getId()).orElseThrow();
		Product newProduct = new Product(product.getId(), 0, product.getProductName(), product.getPrice(),
			product.getForwardDate(), product.getScore(), product.getThumbnailPath(), Product.StockStatus.SOLD_OUT,
			product.getCategory());
		productRepository.save(newProduct);
		book.setProduct(null);
		bookRepository.save(book);
		return BookResponse.convertToBookResponse(book);
	}
}
