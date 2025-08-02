package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.Payment;
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
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Payment> searchPayments(Pageable pageable, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        query.where(cb.or(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Payment> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payment> countRoot = countQuery.from(Payment.class);
        countQuery.select(cb.count(countRoot)).where(cb.or(buildSearchPredicates(cb, countRoot, searchQuery).toArray(new Predicate[0])));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    @Override
    public Page<Payment> filterByPaymentsFields(Pageable pageable, Long id, Long customerId, String customerName, LocalDate startDate, LocalDate endDate, String document, String documentNumber, BigDecimal amount, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        List<Predicate> predicates = buildFilterPredicates(cb, root, id, customerId, customerName, document, documentNumber, amount, startDate, endDate, searchQuery);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Payment> typedQuery = entityManager.createQuery(query);

        // Пагинация
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет общего количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payment> countRoot = countQuery.from(Payment.class);
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, id, customerId, customerName, document, documentNumber, amount, startDate, endDate, searchQuery);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }


    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Payment> root, String searchQuery) {
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
            predicates.add(cb.like(cb.lower(root.get("documentNumber")), likePattern));

            // Поиск по общей сумме (по строке)
            predicates.add(cb.like(cb.function("str", String.class, root.get("amount")), likePattern));
        }

        return predicates;
    }

    // Вспомогательный метод для создания предикатов для фильтрации
    private List<Predicate> buildFilterPredicates(
            CriteriaBuilder cb,
            Root<Payment> root,
            Long id,
            Long customerId,
            String customerName,
            String document,
            String documentNumber,
            BigDecimal amount,
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

        if (document != null && !document.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("document")), "%" + document.toLowerCase() + "%"));
        }

        if (documentNumber != null && !documentNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("documentNumber")), "%" + documentNumber.toLowerCase() + "%"));
        }

        if (amount!= null) {
            predicates.add(cb.equal(root.get("amount"), amount));
        }

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), endDate));
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerSearch = "%" + searchQuery.toLowerCase() + "%";

            Predicate customerNamePredicate = cb.like(cb.lower(root.get("customer").get("name")), lowerSearch);
            Predicate documentPredicate = cb.like(cb.lower(root.get("document")), lowerSearch);
            Predicate documentNumberPredicate = cb.like(cb.lower(root.get("documentNumber")), lowerSearch);

            predicates.add(cb.or(customerNamePredicate, documentPredicate, documentNumberPredicate));
        }

        return predicates;
    }
}

