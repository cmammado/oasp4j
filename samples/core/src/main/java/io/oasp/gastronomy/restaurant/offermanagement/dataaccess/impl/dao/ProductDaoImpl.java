package io.oasp.gastronomy.restaurant.offermanagement.dataaccess.impl.dao;

import io.oasp.gastronomy.restaurant.general.dataaccess.base.dao.ApplicationMasterDataDaoImpl;
import io.oasp.gastronomy.restaurant.offermanagement.common.api.datatype.ProductSortByHitEntry;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.DrinkEntity;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.MealEntity;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.ProductEntity;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.SideDishEntity;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.dao.ProductDao;
import io.oasp.gastronomy.restaurant.offermanagement.logic.api.to.ProductFilter;
import io.oasp.gastronomy.restaurant.offermanagement.logic.api.to.ProductSearchCriteriaTo;
import io.oasp.gastronomy.restaurant.offermanagement.logic.api.to.ProductSortBy;
import io.oasp.module.jpa.common.api.to.PaginatedListTo;
import io.oasp.module.jpa.common.api.to.PaginationResultTo;
import io.oasp.module.jpa.common.api.to.PaginationTo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.alias.Alias;
import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.path.EntityPathBase;

/**
 * Implementation of {@link ProductDao}.
 *
 * @author loverbec
 */
@Named
public class ProductDaoImpl extends ApplicationMasterDataDaoImpl<ProductEntity> implements ProductDao {

  /**
   * The constructor.
   */
  public ProductDaoImpl() {

    super();
  }

  @Override
  public Class<ProductEntity> getEntityClass() {

    return ProductEntity.class;
  }

  @Override
  @Deprecated
  public List<ProductEntity> findProductsFiltered(ProductFilter productFilterBo, ProductSortBy sortBy) {

    /*
     * Basic error handling
     */
    if (productFilterBo == null) {
      return new ArrayList<>();
    }

    ProductEntity product = Alias.alias(ProductEntity.class);
    JPQLQuery query = new JPAQuery(getEntityManager()).from(Alias.$(product));
    BooleanBuilder builder = new BooleanBuilder();

    /*
     * Drinks
     */
    if (productFilterBo.getFetchDrinks()) {
      builder.or(Alias.$(product).instanceOf(DrinkEntity.class));
    }

    /*
     * Meals
     */
    if (productFilterBo.getFetchMeals()) {
      builder.or(Alias.$(product).instanceOf(MealEntity.class));
    }

    /*
     * SideDishes
     */
    if (productFilterBo.getFetchSideDishes()) {
      builder.or(Alias.$(product).instanceOf(SideDishEntity.class));
    }

    if (sortBy.getSortByEntry().equals(ProductSortByHitEntry.DESCRIPTION)) {
      if (sortBy.getOrderBy().isDesc()) {
        query.where(builder).orderBy(Alias.$(product.getDescription()).desc());
      } else {
        query.where(builder).orderBy(Alias.$(product.getDescription()).asc());
      }
    } else {
      if (sortBy.getOrderBy().isDesc()) {
        query.where(builder).orderBy(Alias.$(product.getId()).desc());
      } else {
        query.where(builder).orderBy(Alias.$(product.getId()).asc());
      }

    }

    List<ProductEntity> result = query.list(Alias.$(product));
    return result;
  }

  @Override
  public PaginatedListTo<ProductEntity> findProducts(ProductSearchCriteriaTo criteria) {

    ProductEntity product = Alias.alias(ProductEntity.class);
    EntityPathBase<ProductEntity> alias = Alias.$(product);
    JPAQuery query = new JPAQuery(getEntityManager()).from(alias);

    String name = criteria.getName();
    if (name != null) {
      query.where(Alias.$(product.getName()).eq(name));
    }

    String description = criteria.getDescription();
    if (description != null) {
      query.where(Alias.$(product.getDescription()).eq(description));
    }

    System.out.println("criteria: " + criteria.toStrink());

    // include filter for entity type

    if (!(criteria.isFetchDrinks() || criteria.isFetchMeals() || criteria.isFetchSideDishes())) {
      // no product type was selected, return empty result
      PaginationTo pagination = criteria.getPagination();

      PaginationResultTo paginationResult = new PaginationResultTo(pagination, 0L);
      List<ProductEntity> paginatedList = new ArrayList<>();

      return new PaginatedListTo<>(paginatedList, paginationResult);
    }

    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isFetchSideDishes()) {
      builder.or(Alias.$(product).instanceOf(SideDishEntity.class));
    }
    if (criteria.isFetchMeals()) {
      builder.or(Alias.$(product).instanceOf(MealEntity.class));
    }
    if (criteria.isFetchDrinks()) {
      builder.or(Alias.$(product).instanceOf(DrinkEntity.class));
    }
    query.where(builder);

    return findPaginated(criteria, query, alias);
  }
}
