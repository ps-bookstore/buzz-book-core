package store.buzzbook.core.elastic.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import store.buzzbook.core.elastic.document.CategoryDocument;

public interface CategoryDocumentRepository extends ElasticsearchRepository<CategoryDocument, Integer> {


}
