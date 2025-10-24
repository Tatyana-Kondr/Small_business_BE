package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.Purchase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 16.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Repository
public class PurchaseRepositoryCustomImpl implements PurchaseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // Метод для поиска по нескольким полям
    @Override
    public Page<Purchase> searchPurchases(Pageable pageable, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Purchase> query = cb.createQuery(Purchase.class);
        Root<Purchase> root = query.from(Purchase.class);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        query.where(cb.or(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Purchase> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Purchase> countRoot = countQuery.from(Purchase.class);
        countQuery.select(cb.count(countRoot)).where(cb.or(buildSearchPredicates(cb, countRoot, searchQuery).toArray(new Predicate[0])));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }


    // Метод для фильтрации по полям
    @Override
    public Page<Purchase> filterByFields(Pageable pageable,
                                         Long id,
                                         Long vendorId,
                                         String vendorName,
                                         Long documentId,
                                         String documentNumber,
                                         BigDecimal total,
                                         String paymentStatus,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Purchase> query = cb.createQuery(Purchase.class);
        Root<Purchase> root = query.from(Purchase.class);

        List<Predicate> predicates = buildFilterPredicates(cb, root, id, vendorId, vendorName, documentId, documentNumber, total, paymentStatus, startDate, endDate, searchQuery);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Purchase> typedQuery = entityManager.createQuery(query);

        // Пагинация
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет общего количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Purchase> countRoot = countQuery.from(Purchase.class);
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, id, vendorId, vendorName, documentId, documentNumber, total, paymentStatus, startDate, endDate, searchQuery);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    // Вспомогательный метод для создания предикатов для поиска
    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Purchase> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";

            // Поиск по ID
            try {
                Long id = Long.parseLong(searchQuery);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {}

            // Поиск по имени поставщика
            predicates.add(cb.like(cb.lower(root.get("vendor").get("name")), likePattern));

            // Поиск по номеру документа
            predicates.add(cb.like(cb.lower(root.get("documentNumber")), likePattern));

            // Поиск по общей сумме (по строке)
            predicates.add(cb.like(cb.function("str", String.class, root.get("total")), likePattern));
        }

        return predicates;
    }

    // Вспомогательный метод для создания предикатов для фильтрации
    private List<Predicate> buildFilterPredicates(
            CriteriaBuilder cb,
            Root<Purchase> root,
            Long id,
            Long vendorId,
            String vendorName,
            Long documentId,
            String documentNumber,
            BigDecimal total,
            String paymentStatus,
            LocalDate startDate,
            LocalDate endDate,
            String searchQuery
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(cb.equal(root.get("id"), id));
        }

        if (vendorId != null) {
            predicates.add(cb.equal(root.get("vendor").get("id"), vendorId));
        }

        if (vendorName != null && !vendorName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("vendor").get("name")), "%" + vendorName.toLowerCase() + "%"));
        }

        if (documentId != null) {
            predicates.add(cb.equal(root.get("document").get("id"), documentId));
        }


        if (documentNumber != null && !documentNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("documentNumber")), "%" + documentNumber.toLowerCase() + "%"));
        }

        if (total != null) {
            predicates.add(cb.equal(root.get("total"), total));
        }

        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("paymentStatus")), "%" + paymentStatus.toLowerCase() + "%"));
        }

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("purchasingDate"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("purchasingDate"), endDate));
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerSearch = "%" + searchQuery.toLowerCase() + "%";

            Predicate vendorNamePredicate = cb.like(cb.lower(root.get("vendor").get("name")), lowerSearch);
            Predicate documentNumberPredicate = cb.like(cb.lower(root.get("documentNumber")), lowerSearch);
            Predicate paymentStatusPredicate = cb.like(cb.lower(root.get("paymentStatus")), lowerSearch);

            predicates.add(cb.or(vendorNamePredicate, documentNumberPredicate, paymentStatusPredicate));
        }

        return predicates;
    }
}