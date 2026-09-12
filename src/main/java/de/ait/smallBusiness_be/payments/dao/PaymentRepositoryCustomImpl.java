package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        applySorting(pageable, cb, root, query);

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
    public Page<Payment> filterByPaymentsFields(Pageable pageable, Long id, Long customerId, String customerName, Long saleId, Long purchaseId, LocalDate startDate, LocalDate endDate, Long documentId, String documentNumber, BigDecimal amount, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        List<Predicate> predicates = buildFilterPredicates(cb, root, id, customerId, customerName, saleId, purchaseId, documentId, documentNumber, amount, startDate, endDate, searchQuery);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        applySorting(pageable, cb, root, query);

        TypedQuery<Payment> typedQuery = entityManager.createQuery(query);

        // Пагинация
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет общего количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payment> countRoot = countQuery.from(Payment.class);
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, id, customerId, customerName, saleId, purchaseId, documentId, documentNumber, amount, startDate, endDate, searchQuery);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    @Override
    public Page<Payment> findAllWithSorting(Pageable pageable) {

        CriteriaBuilder cb =
                entityManager.getCriteriaBuilder();

        CriteriaQuery<Payment> query =
                cb.createQuery(Payment.class);

        Root<Payment> root =
                query.from(Payment.class);

        applySorting(
                pageable,
                cb,
                root,
                query
        );

        TypedQuery<Payment> typed =
                entityManager.createQuery(query);

        typed.setFirstResult(
                (int) pageable.getOffset()
        );

        typed.setMaxResults(
                pageable.getPageSize()
        );

        CriteriaQuery<Long> countQuery =
                cb.createQuery(Long.class);

        Root<Payment> countRoot =
                countQuery.from(Payment.class);

        countQuery.select(
                cb.count(countRoot)
        );

        Long count =
                entityManager
                        .createQuery(countQuery)
                        .getSingleResult();

        return new PageImpl<>(
                typed.getResultList(),
                pageable,
                count
        );
    }

    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Payment> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";

            // Поиск по ID
            try {
                Long id = Long.parseLong(searchQuery);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {
            }

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
            Long saleId,
            Long purchaseId,
            Long documentId,
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

        if (saleId != null) {
            predicates.add(cb.equal(root.get("sale").get("id"), saleId));
        }

        if (purchaseId != null) {
            predicates.add(cb.equal(root.get("purchase").get("id"), purchaseId));
        }

        if (documentId != null) {
            predicates.add(cb.equal(root.get("document").get("id"), documentId));
        }

        if (documentNumber != null && !documentNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("documentNumber")), "%" + documentNumber.toLowerCase() + "%"));
        }

        if (amount != null) {
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
            Predicate documentNamePredicate = cb.like(cb.lower(root.get("document").get("name")), lowerSearch);
            Predicate documentNumberPredicate = cb.like(cb.lower(root.get("documentNumber")), lowerSearch);

            predicates.add(cb.or(customerNamePredicate, documentNamePredicate, documentNumberPredicate));
        }

        return predicates;
    }

    private void applySorting(
            Pageable pageable,
            CriteriaBuilder cb,
            Root<Payment> root,
            CriteriaQuery<Payment> query
    ) {

        List<Order> orders = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {

            Expression<?> expression =
                    switch (sortOrder.getProperty()) {

                        case "id" -> root.get("id");

                        case "paymentDate" -> root.get("paymentDate");

                        case "customerName" -> root.join("customer")
                                .get("name");

                        case "amount" -> root.get("amount");

                        case "documentName" -> root.join("document")
                                .get("name");

                        case "documentNumber" -> root.get("documentNumber");

                        case "saleId" -> root.join(
                                "sale",
                                JoinType.LEFT
                        ).get("id");

                        case "purchaseId" -> root.join(
                                "purchase",
                                JoinType.LEFT
                        ).get("id");

                        default -> null;
                    };

            if (expression == null) {
                continue;
            }

            if (sortOrder.isAscending()) {
                orders.add(
                        cb.asc(expression)
                );
            } else {
                orders.add(
                        cb.desc(expression)
                );
            }
        }

        if (orders.isEmpty()) {
            orders.add(
                    cb.desc(
                            root.get("paymentDate")
                    )
            );

            orders.add(
                    cb.desc(
                            root.get("id")
                    )
            );
        }

        query.orderBy(orders);
    }
}

