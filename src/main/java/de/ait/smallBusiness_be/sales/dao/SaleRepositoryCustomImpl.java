package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.Sale;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SaleRepositoryCustomImpl implements SaleRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Sale> searchSales(Pageable pageable, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Sale> query = cb.createQuery(Sale.class);
        Root<Sale> root = query.from(Sale.class);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        query.where(cb.or(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Sale> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Sale> countRoot = countQuery.from(Sale.class);
        countQuery.select(cb.count(countRoot)).where(cb.or(buildSearchPredicates(cb, countRoot, searchQuery).toArray(new Predicate[0])));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    @Override
    public Page<Sale> filterSalesByFields(Pageable pageable, Long id, Long customerId, String customerName, String invoiceNumber, BigDecimal totalAmount, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Sale> query = cb.createQuery(Sale.class);
        Root<Sale> root = query.from(Sale.class);

        List<Predicate> predicates = buildFilterPredicates(cb, root, id, customerId, customerName, invoiceNumber, totalAmount, paymentStatus, startDate, endDate, searchQuery);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Sale> typedQuery = entityManager.createQuery(query);

        // Пагинация
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет общего количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Sale> countRoot = countQuery.from(Sale.class);
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, id, customerId, customerName, invoiceNumber, totalAmount, paymentStatus, startDate, endDate, searchQuery);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    // Вспомогательный метод для создания предикатов для поиска
    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Sale> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";

            // Поиск по ID
            try {
                Long id = Long.parseLong(searchQuery);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {}

            // Поиск по имени поставщика
            predicates.add(cb.like(cb.lower(root.get("customer").get("name")), likePattern));

            // Поиск по номеру документа
            predicates.add(cb.like(cb.lower(root.get("invoiceNumber")), likePattern));

            // Поиск по общей сумме (по строке)
            predicates.add(cb.like(cb.function("str", String.class, root.get("totalAmount")), likePattern));
        }
        return predicates;
    }

    // Вспомогательный метод для создания предикатов для фильтрации
    private List<Predicate> buildFilterPredicates(
            CriteriaBuilder cb,
            Root<Sale> root,
            Long id,
            Long customerId,
            String customerName,
            String invoiceNumber,
            BigDecimal totalAmount,
            String paymentStatus,
            LocalDate startDate,
            LocalDate endDate,
            String searchQuery
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(cb.equal(root.get("id"), id));
        }

        if (customerId != null) {
            predicates.add(cb.equal(root.get("customer").get("id"), customerId));
        }

        if (customerName != null && !customerName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("customer").get("name")), "%" + customerName.toLowerCase() + "%"));
        }

        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("invoiceNumber")), "%" + invoiceNumber.toLowerCase() + "%"));
        }

        if (totalAmount != null) {
            predicates.add(cb.equal(root.get("totalAmount"), totalAmount));
        }

        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("paymentStatus")), "%" + paymentStatus.toLowerCase() + "%"));
        }

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("salesDate"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("salesDate"), endDate));
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerSearch = "%" + searchQuery.toLowerCase() + "%";

            Predicate customerNamePredicate = cb.like(cb.lower(root.get("customer").get("name")), lowerSearch);
            Predicate invoiceNumberPredicate = cb.like(cb.lower(root.get("invoiceNumber")), lowerSearch);
            Predicate paymentStatusPredicate = cb.like(cb.lower(root.get("paymentStatus")), lowerSearch);

            predicates.add(cb.or(customerNamePredicate, invoiceNumberPredicate, paymentStatusPredicate));
        }

        return predicates;
    }
}
