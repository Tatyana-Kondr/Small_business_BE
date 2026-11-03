package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.Production;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class ProductionRepositoryCustomImpl implements ProductionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Production> searchProduction(Pageable pageable, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Production> query = cb.createQuery(Production.class);
        Root<Production> root = query.from(Production.class);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        if (!predicates.isEmpty()) {
            query.where(cb.or(predicates.toArray(new Predicate[0])));
        }
        applySorting(pageable, cb, root, query);

        TypedQuery<Production> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Production> countRoot = countQuery.from(Production.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = buildSearchPredicates(cb, countRoot, searchQuery);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.or(countPredicates.toArray(new Predicate[0])));
        }

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    @Override
    public Page<Production> getAllProductionsByFilter(Pageable pageable, LocalDate startDate, LocalDate endDate, String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Production> query = cb.createQuery(Production.class);
        Root<Production> root = query.from(Production.class);

        List<Predicate> predicates = new ArrayList<>();

        // Фильтр по датам
        predicates.addAll(buildFilterPredicates(cb, root, startDate, endDate));

        // Поиск по строке
        List<Predicate> searchPredicates = buildSearchPredicates(cb, root, searchQuery);
        if (!searchPredicates.isEmpty()) {
            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        applySorting(pageable, cb, root, query);

        TypedQuery<Production> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Production> countRoot = countQuery.from(Production.class);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.addAll(buildFilterPredicates(cb, countRoot, startDate, endDate));

        List<Predicate> countSearchPredicates = buildSearchPredicates(cb, countRoot, searchQuery);
        if (!countSearchPredicates.isEmpty()) {
            countPredicates.add(cb.or(countSearchPredicates.toArray(new Predicate[0])));
        }

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        countQuery.select(cb.count(countRoot));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    @Override
    public Page<Production> findAllWithSorting(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Production> query = cb.createQuery(Production.class);
        Root<Production> root = query.from(Production.class);

        applySorting(pageable, cb, root, query);

        TypedQuery<Production> typedQuery = entityManager.createQuery(query);

        typedQuery.setFirstResult((int) pageable.getOffset());

        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        Root<Production> countRoot = countQuery.from(Production.class);

        countQuery.select(cb.count(countRoot));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalCount);
    }

    // Вспомогательный метод для создания предикатов для поиска
    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Production> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchQuery == null || searchQuery.isBlank()) {
            return predicates;
        }

        String search = searchQuery.trim();
        String likePattern = "%" + search.toLowerCase(Locale.ROOT) + "%";

        // ID производства и ID товара
        try {
            Long id = Long.parseLong(search);

            predicates.add(cb.equal(root.get("id"), id));
            predicates.add(cb.equal(root.get("product").get("id"), id));

        } catch (NumberFormatException ignored) {}

        // Сумма производства
        predicates.add(cb.like(cb.lower(cb.function("str", String.class, root.get("amount"))), likePattern));

        // Артикул товара
        predicates.add(cb.like(cb.lower(root.get("product").get("article")), likePattern));

        // Наименование товара
        predicates.add(cb.like(cb.lower(root.get("product").get("name")), likePattern));

        return predicates;
    }

    // Вспомогательный метод для создания предикатов для фильтрации по датам
    private List<Predicate> buildFilterPredicates(CriteriaBuilder cb, Root<Production> root, LocalDate startDate, LocalDate endDate) {
        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dateOfProduction"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dateOfProduction"), endDate));
        }

        return predicates;
    }

    private void applySorting(Pageable pageable, CriteriaBuilder cb, Root<Production> root, CriteriaQuery<?> query) {
        List<Order> orders = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();

            if (property.equals("productArticle")) {
                if (order.getDirection().isAscending()) {
                    orders.add(cb.asc(root.get("product").get("article")));
                } else {
                    orders.add(cb.desc(root.get("product").get("article")));
                }

            } else if (property.equals("productName")) {
                if (order.getDirection().isAscending()) {
                    orders.add(cb.asc(root.get("product").get("name")));
                } else {
                    orders.add(cb.desc(root.get("product").get("name")));
                }

            } else {
                if (order.getDirection().isAscending()) {
                    orders.add(cb.asc(root.get(property)));
                } else {
                    orders.add(cb.desc(root.get(property)));
                }
            }
        });

        if (orders.isEmpty()) {
            orders.add(cb.desc(root.get("dateOfProduction")));
        }
        boolean hasIdSort = pageable.getSort().stream().anyMatch(order -> order.getProperty().equals("id"));
        if (!hasIdSort) {
            orders.add(cb.desc(root.get("id")));
        }

        query.orderBy(orders);
    }
}
