package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.Production;
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


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        query.where(cb.or(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.asc(root.get("id")));

        TypedQuery<Production> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Подсчет количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Production> countRoot = countQuery.from(Production.class);
        countQuery.select(cb.count(countRoot)).where(cb.or(buildSearchPredicates(cb, countRoot, searchQuery).toArray(new Predicate[0])));

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
        query.orderBy(cb.asc(root.get("id")));

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

    // Вспомогательный метод для создания предикатов для поиска
    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<Production> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";

            // Поиск по ID
            try {
                Long id = Long.parseLong(searchQuery);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {}

            // Поиск по ID продукта
            try {
                Long productId = Long.parseLong(searchQuery);
                predicates.add(cb.equal(root.get("product").get("id"), productId));
            } catch (NumberFormatException ignored) {}

            // Поиск по amount (BigDecimal -> строка)
            predicates.add(cb.like(cb.lower(cb.function("str", String.class, root.get("amount"))), likePattern));

          // Поиск по имени продукта (product.name)
         predicates.add(cb.like(cb.lower(root.get("product").get("name")), likePattern));
        }
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
}
